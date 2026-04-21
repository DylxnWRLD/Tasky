package com.example.tasky.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    val role: String,

    @SerialName("profile_image")
    val profileImage: String? = null,

    val location: String? = null,

    val experience: String? = null,

    val bio: String? = null,

    val skills: List<String>? = null
)