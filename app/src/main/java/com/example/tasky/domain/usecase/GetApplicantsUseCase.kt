package com.example.tasky.domain.usecase

import com.example.tasky.domain.model.JobApplicant
import com.example.tasky.domain.repository.JobRepository

class GetApplicantsUseCase(private val repository: JobRepository) {
    suspend operator fun invoke(jobId: String): Result<List<JobApplicant>> {
        return repository.getApplicants(jobId)
    }
}