package com.example.tasky.domain.usecase

import com.example.tasky.domain.repository.JobRepository

class AcceptApplicantUseCase(private val repository: JobRepository) {
    suspend operator fun invoke(workerId: String, jobId: String): Result<Unit> {
        return repository.acceptApplicant(workerId, jobId)
    }
}