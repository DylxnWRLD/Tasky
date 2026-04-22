package com.example.tasky.data.remote.dto

import com.example.tasky.domain.model.Job
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JobDto(
    val id: String,

    @SerialName("client_id")
    val client_id: String,

    val title: String,
    val category: String,
    val payment: Double,
    val description: String,

    @SerialName("location_approx")
    val location_approx: String,

    @SerialName("scheduled_date")
    val date: String,

    @SerialName("scheduled_time")
    val time: String,

    val image_url: String? = null,

    val status: String
)

fun JobDto.toDomain(): Job {
    return Job(
        id = this.id,
        clientId = this.client_id,
        title = this.title,
        category = this.category,
        payment = this.payment,
        description = this.description,
        locationApprox = this.location_approx,
        date = this.date,
        time = this.time,
        imageUrl = this.image_url,
        status = this.status
    )
}