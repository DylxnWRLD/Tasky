package com.example.tasky.data.repository

import com.example.tasky.data.remote.SupabaseClient
import com.example.tasky.data.remote.dto.JobDto
import com.example.tasky.domain.model.Job
import com.example.tasky.domain.repository.JobRepository
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JobRepositoryImpl : JobRepository {

    private val client = SupabaseClient.client

    // ... aquí ya deberías tener tu función applyToJob ...

    override suspend fun getJobById(jobId: String): Result<Job> = withContext(Dispatchers.IO) {
        try {
            // Buscamos en la tabla 'trabajos' de Supabase
            val jobDto = client.postgrest.from("trabajos")
                .select {
                    filter {
                        eq("id", jobId)
                    }
                }.decodeSingle<JobDto>() // Traemos solo uno

            // Convertimos el DTO a nuestro modelo de dominio (Job)
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
                isApplied = false // Esto luego lo podrías validar con otra consulta
            )

            Result.success(job)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}