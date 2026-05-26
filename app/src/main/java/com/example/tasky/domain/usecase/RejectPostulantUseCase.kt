package com.example.tasky.domain.usecase

import com.example.tasky.domain.repository.JobRepository

class RejectPostulantUseCase(
    private val repository: JobRepository
) {
    suspend operator fun invoke(workerId: String, jobId: String, reason: String?): Result<Unit> {
        return repository.rejectPostulant(workerId, jobId, reason)
    }
}