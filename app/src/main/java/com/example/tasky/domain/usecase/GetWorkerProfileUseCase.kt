package com.example.tasky.domain.usecase

import com.example.tasky.domain.model.User
import com.example.tasky.domain.repository.JobRepository

class GetWorkerProfileUseCase(private val repository: JobRepository) {
    suspend operator fun invoke(workerId: String, jobId: String): Result<User> {
        return repository.getWorkerProfile(workerId, jobId)
    }
}