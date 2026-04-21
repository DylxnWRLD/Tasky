package com.example.tasky.data.repository

import com.example.tasky.data.remote.SupabaseClient
import com.example.tasky.data.remote.dto.JobDto
import com.example.tasky.domain.model.Job
import com.example.tasky.domain.repository.JobRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JobRepositoryImpl : JobRepository {

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

    override suspend fun getJobById(jobId: String): Result<Job> = withContext(Dispatchers.IO) {
        try {
            val userId = client.auth.currentUserOrNull()?.id

            val jobDto = client.postgrest.from("trabajos")
                .select {
                    filter { eq("id", jobId) }
                }.decodeSingle<JobDto>()


            val isApplied = if (userId != null) {
                val countResponse = client.postgrest.from("postulaciones")
                    .select {
                        filter {
                            eq("job_id", jobId)
                            eq("worker_id", userId)
                        }
                    }
                countResponse.data != "[]"
            } else false

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
}