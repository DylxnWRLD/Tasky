package com.example.tasky.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApplicationDto(
    @SerialName("job_id")
    val jobId: String,

    @SerialName("worker_id")
    val workerId: String,

    val status: String = "pendiente"
)