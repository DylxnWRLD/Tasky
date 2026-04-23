package com.example.tasky.data.repository

import android.content.Context
import android.net.Uri
import com.example.tasky.data.remote.SupabaseClient
import com.example.tasky.data.remote.dto.JobDto
import com.example.tasky.data.remote.dto.JobInsertDto
import com.example.tasky.data.remote.dto.toDomain
import com.example.tasky.domain.model.Job
import com.example.tasky.domain.repository.JobRepository
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JobRepositoryImpl(private val context: Context) : JobRepository {

    private val client = SupabaseClient.client

    override suspend fun applyToJob(jobId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = client.auth.currentUserOrNull()?.id
                ?: return@withContext Result.failure(Exception("Usuario no autenticado"))

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
                .select {
                    filter { eq("id", jobId) }
                }.decodeSingle<JobDto>()

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
                    println("Error verificando postulación: ${e.message}")
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
                isApplied = isApplied
            )

            Result.success(job)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
            val fileName = "job_${System.currentTimeMillis()}.jpg"
            // Se necesitan los bytes de la imagen. Aquí un ejemplo rápido:
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                ?: throw Exception("No se pudo leer la imagen")

            val path = SupabaseClient.client.storage.from("Detalles Trabajo").upload(fileName, bytes)
            val publicUrl = SupabaseClient.client.storage.from("Detalles Trabajo").publicUrl(fileName)
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
}