package com.example.tasky.domain.repository

import android.net.Uri
import com.example.tasky.data.remote.dto.JobInsertDto
import com.example.tasky.domain.model.Job

interface JobRepository {


    suspend fun applyToJob(jobId: String): Result<Unit>

    suspend fun cancelApplication(jobId: String): Result<Unit>

    suspend fun getJobs(): Result<List<Job>>

    suspend fun getJobById(jobId: String): Result<Job>

    suspend fun deleteJob(jobId: String): Result<Unit>

    suspend fun uploadJobImage(uri: Uri): Result<String>

    suspend fun insertJob(job: JobInsertDto): Result<Unit>
}
