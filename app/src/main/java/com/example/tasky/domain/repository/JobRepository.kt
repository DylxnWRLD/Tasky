package com.example.tasky.domain.repository

import android.net.Uri
import com.example.tasky.data.remote.dto.JobInsertDto
import com.example.tasky.domain.model.Job
import com.example.tasky.domain.model.JobApplicant
import com.example.tasky.domain.model.User

interface JobRepository {


    suspend fun applyToJob(jobId: String): Result<Unit>

    suspend fun cancelApplication(jobId: String): Result<Unit>

    suspend fun getJobs(): Result<List<Job>>

    suspend fun getJobById(jobId: String): Result<Job>

    suspend fun updateJob(jobId: String, job: JobInsertDto): Result<Unit>

    suspend fun deleteJob(jobId: String): Result<Unit>

    suspend fun uploadJobImage(uri: Uri): Result<String>

    suspend fun insertJob(job: JobInsertDto): Result<Unit>

    suspend fun getApplicants(jobId: String): Result<List<JobApplicant>>

    suspend fun getWorkerProfile(workerId: String, jobId: String): Result<User>

    suspend fun getJobsByClientId(clientId: String): Result<List<Job>>

    suspend fun acceptApplicant(workerId: String, jobId: String): Result<Unit>

    suspend fun cancelWorkerSelection(jobId: String, workerId: String): Result<Unit>

    suspend fun enviarReporte(userId: String, texto: String): Result<Unit>
    suspend fun rejectPostulant(workerId: String, jobId: String, reason: String?): Result<Unit>
}
