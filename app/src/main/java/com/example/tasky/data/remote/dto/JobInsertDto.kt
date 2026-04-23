package com.example.tasky.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JobInsertDto(
    @SerialName("client_id") val clientId: String,
    val title: String,
    val category: String,
    val payment: Double,
    val description: String,
    @SerialName("location_approx") val locationApprox: String,
    @SerialName("scheduled_date") val scheduledDate: String,
    @SerialName("scheduled_time") val scheduledTime: String,
    @SerialName("image_url") val imageUrl: String? = null,
    val status: String = "abierto"
)