package com.example.tasky.domain.usecase

import com.example.tasky.domain.repository.JobRepository

class ApplyToJobUseCase(private val repository: JobRepository) {
    suspend operator fun invoke(jobId: String): Result<Unit> = repository.applyToJob(jobId)
}

class CancelApplicationUseCase(private val repository: JobRepository) {
    suspend operator fun invoke(jobId: String): Result<Unit> = repository.cancelApplication(jobId)
}