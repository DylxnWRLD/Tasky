package com.example.tasky.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class JobDto(
    val id: String,
    val title: String,
    val category: String,
    val payment: Double,
    val description: String,
    val date: String,
    val time: String,
    val image_url: String,
    val published_ago: String
)