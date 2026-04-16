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
                "user_id" to userId,
                "status" to "en revisión"
            ))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getJobById(jobId: String): Result<Job> = withContext(Dispatchers.IO) {
        try {
            val jobDto = client.postgrest.from("trabajos")
                .select {
                    filter { eq("id", jobId) }
                }.decodeSingle<JobDto>()

            val job = Job(
                id = jobDto.id,
                title = jobDto.title,
                category = jobDto.category,
                payment = jobDto.payment,
                description = jobDto.description,
                date = jobDto.date,
                time = jobDto.time,
                imageUrl = jobDto.image_url,
                publishedAgo = jobDto.published_ago,
                isApplied = false
            )

            Result.success(job)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}