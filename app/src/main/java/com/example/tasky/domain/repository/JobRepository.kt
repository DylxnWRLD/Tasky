package com.example.tasky.domain.repository

interface JobRepository {
    suspend fun applyToJob(jobId: String): Result<Unit>
}