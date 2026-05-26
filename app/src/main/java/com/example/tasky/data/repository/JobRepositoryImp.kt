package com.example.tasky.data.repository

import android.content.Context
import android.net.Uri
import com.example.tasky.data.remote.SupabaseClient
import com.example.tasky.data.remote.dto.ApplicationDto
import com.example.tasky.data.remote.dto.JobDto
import com.example.tasky.data.remote.dto.JobInsertDto
import com.example.tasky.data.remote.dto.ReporteDto
import com.example.tasky.data.remote.dto.UserDto
import com.example.tasky.data.remote.dto.toDomain
import com.example.tasky.domain.model.Job
import com.example.tasky.domain.model.JobApplicant
import com.example.tasky.domain.model.User
import com.example.tasky.domain.repository.JobRepository
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject

class JobRepositoryImpl(private val context: Context) : JobRepository {

    private val client = SupabaseClient.client

    // ---
    override suspend fun getApplicants(jobId: String): Result<List<JobApplicant>> = withContext(Dispatchers.IO) {
        try {
            val response = client.postgrest.from("postulaciones")
                .select(Columns.raw("*, users(*)")) {
                    filter {
                        eq("job_id", jobId)
                        or {
                            eq("status", "pendiente")
                            eq("status", "aceptado")
                        }
                    }
                }.decodeList<ApplicationDto>()

            val applicants = response.map { dto ->
                JobApplicant(
                    user = dto.users.toDomain(),
                    status = dto.status
                )
            }

            val sortedApplicants = applicants.sortedByDescending { it.status == "aceptado" }

            Result.success(sortedApplicants)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWorkerProfile(
        workerId: String,
        jobId: String
    ): Result<User> = withContext(Dispatchers.IO) {

        try {

            val postulacion = client.postgrest.from("postulaciones")
                .select {
                    filter {
                        eq("worker_id", workerId)
                        eq("job_id", jobId)
                    }
                }
                .decodeList<JsonObject>()

            if (postulacion.isEmpty()) {
                return@withContext Result.failure(
                    Exception("cancelada")
                )
            }

            val userDto = client.postgrest.from("users")
                .select {
                    filter {
                        eq("id", workerId)
                    }
                }
                .decodeSingle<UserDto>()

            Result.success(userDto.toDomain())

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun applyToJob(jobId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = client.auth.currentUserOrNull()?.id
                ?: return@withContext Result.failure(Exception("Usuario no autenticado"))

            val checkAccepted = client.postgrest.from("postulaciones")
                .select {
                    filter {
                        eq("job_id", jobId)
                        eq("status", "aceptado")
                    }
                }.decodeList<ApplicationDto>()

            if (checkAccepted.isNotEmpty()) {
                return@withContext Result.failure(Exception("Este trabajo ya no acepta más postulaciones."))
            }

            client.postgrest.from("postulaciones").insert(mapOf(
                "job_id" to jobId,
                "worker_id" to userId,
                "status" to "pendiente"
            ))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelApplication(jobId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = client.auth.currentUserOrNull()?.id
                ?: return@withContext Result.failure(Exception("Usuario no autenticado"))

            client.postgrest.from("postulaciones").delete {
                filter {
                    eq("job_id", jobId)
                    eq("worker_id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getJobs(): Result<List<Job>> {
        return try {
            val jobsDto = SupabaseClient.client.from("trabajos")
                .select()
                .decodeList<JobDto>()
            Result.success(jobsDto.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getJobById(jobId: String): Result<Job> = withContext(Dispatchers.IO) {
        try {
            val userId = client.auth.currentUserOrNull()?.id

            val jobDto = client.postgrest.from("trabajos")
                .select { filter { eq("id", jobId) } }
                .decodeSingle<JobDto>()

            val acceptedResponse = client.postgrest.from("postulaciones")
                .select(io.github.jan.supabase.postgrest.query.Columns.list("id", "status", "worker_id")) {
                    filter {
                        eq("job_id", jobId)
                        eq("status", "aceptado")
                    }
                }.decodeList<kotlinx.serialization.json.JsonObject>()

            val isJobClosed = acceptedResponse.isNotEmpty()
            val acceptedWorkerId = if (isJobClosed) {
                acceptedResponse.firstOrNull()?.get("worker_id")
                    ?.toString()?.replace("\"", "")
            } else null

            var isApplied = false
            if (userId != null) {
                try {
                    val response = client.postgrest.from("postulaciones")
                        .select {
                            filter {
                                eq("job_id", jobId)
                                eq("worker_id", userId)
                            }
                        }
                    val jsonArray = response.decodeAs<kotlinx.serialization.json.JsonArray>()
                    isApplied = jsonArray.isNotEmpty()
                } catch (e: Exception) {
                    isApplied = false
                }
            }

            val job = Job(
                id = jobDto.id,
                clientId = jobDto.client_id,
                title = jobDto.title,
                category = jobDto.category,
                payment = jobDto.payment,
                description = jobDto.description,
                locationApprox = jobDto.location_approx,
                date = jobDto.date,
                time = jobDto.time,
                imageUrl = jobDto.image_url,
                status = jobDto.status,
                isApplied = isApplied,
                isClosed = isJobClosed,
                acceptedWorkerId = acceptedWorkerId
            )

            Result.success(job)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateJob(jobId: String, job: JobInsertDto): Result<Unit> {
        return try {
            SupabaseClient.client.from("trabajos").update(job) {
                filter { eq("id", jobId) }
            }
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deleteJob(jobId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.postgrest.from("trabajos").delete {
                filter { eq("id", jobId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadJobImage(uri: Uri): Result<String> {
        return try {
            val fileName = "chamba_${System.currentTimeMillis()}.jpg"
            // Se necesitan los bytes de la imagen. Aquí un ejemplo rápido:
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                ?: throw Exception("No se pudo leer la imagen")

            val path = SupabaseClient.client.storage.from("imagenes-trabajo").upload(fileName, bytes)
            val publicUrl = SupabaseClient.client.storage.from("imagenes-trabajo").publicUrl(fileName)
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun insertJob(job: JobInsertDto): Result<Unit> {
        return try {
            SupabaseClient.client.from("trabajos").insert(job)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getJobsByClientId(clientId: String): Result<List<Job>> {
        return try {
            // Filtramos a huevo por la columna client_id
            val jobsDto = SupabaseClient.client.from("trabajos")
                .select {
                    filter { eq("client_id", clientId) }
                }.decodeList<JobDto>()

            Result.success(jobsDto.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptApplicant(workerId: String, jobId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = client.postgrest.from("postulaciones")
                .update(mapOf("status" to "aceptado")) {
                    filter {
                        eq("job_id", jobId)
                        eq("worker_id", workerId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelWorkerSelection(jobId: String, workerId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.postgrest.from("postulaciones")
                .update(mapOf("status" to "pendiente")) {
                    filter {
                        eq("job_id", jobId)
                        eq("worker_id", workerId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun enviarReporte(userId: String, texto: String): Result<Unit> {
        return try {
            val reporte = ReporteDto(userId = userId, descripcion = texto)
            SupabaseClient.client.from("reportes").insert(reporte)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun rejectPostulant(
        workerId: String,
        jobId: String,
        reason: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val postulacion = client.postgrest.from("postulaciones")
                .select {
                    filter {
                        eq("worker_id", workerId)
                        eq("job_id", jobId)
                    }
                }.decodeList<JsonObject>()
            if (postulacion.isEmpty()) {
                return@withContext Result.failure(
                    Exception("no_disponible")
                )
            }
            val updateData = if (!reason.isNullOrBlank()) {
                mapOf(
                    "status" to "rechazado",
                    "rejection_reason" to reason
                )
            } else {
                mapOf(
                    "status" to "rechazado"
                )
            }
            client.postgrest.from("postulaciones")
                .update(updateData) {
                    filter {
                        eq("job_id", jobId)
                        eq("worker_id", workerId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error de servidor: ${e.localizedMessage}"))
        }
    }

}