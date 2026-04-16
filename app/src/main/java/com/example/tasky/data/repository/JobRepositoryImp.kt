package com.example.tasky.data.repository

import com.example.tasky.data.remote.SupabaseClient
import com.example.tasky.data.remote.dto.ApplicationDto
import com.example.tasky.domain.repository.JobRepository
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JobRepositoryImpl : JobRepository {
    private val client = SupabaseClient.client

    override suspend fun applyToJob(jobId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = client.auth.currentUserOrNull()
                ?: return@withContext Result.failure(Exception("Debe iniciar sesión"))

            val application = ApplicationDto(job_id = jobId, user_id = user.id)
            client.postgrest.from("postulaciones").insert(application)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}