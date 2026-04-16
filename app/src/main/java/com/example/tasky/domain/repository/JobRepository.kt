package com.example.tasky.domain.repository

import com.example.tasky.domain.model.Job

interface JobRepository {
    suspend fun applyToJob(jobId: String): Result<Unit>
    suspend fun getJobById(jobId: String): Result<Job>
}