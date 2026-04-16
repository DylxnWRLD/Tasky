package com.example.tasky.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationDto(
    val job_id: String,
    val user_id: String,
    val status: String = "en revisión"
)