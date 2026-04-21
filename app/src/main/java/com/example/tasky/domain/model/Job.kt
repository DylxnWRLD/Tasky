package com.example.tasky.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Job(
    val id: String? = null,
    @SerialName("client_id")
    val clientId: String,
    val title: String,
    val category: String,
    val payment: Double,
    val description: String,

    @SerialName("location_approx")
    val locationApprox: String,
    @SerialName("scheduled_date")
    val date: String,

    @SerialName("scheduled_time")
    val time: String,

    @SerialName("image_url")
    val imageUrl: String? = null,

    val status: String = "abierto",


    val isApplied: Boolean = false
)