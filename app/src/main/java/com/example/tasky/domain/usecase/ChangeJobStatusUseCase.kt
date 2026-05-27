package com.example.tasky.domain.usecase

import com.example.tasky.domain.repository.JobRepository

class ChangeJobStatusUseCase(
    private val repository: JobRepository
) {
    suspend operator fun invoke(jobId: String, status: String): Result<Unit> {
        return repository.updateJobStatus(jobId, status)
    }
}