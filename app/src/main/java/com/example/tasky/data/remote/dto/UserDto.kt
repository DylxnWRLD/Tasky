package com.example.tasky.data.remote.dto

import com.example.tasky.domain.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("profile_image")
    val profileImage: String? = null,
    val description: String? = null,
    val rating: Double = 0.0
)

fun UserDto.toDomain(): User {
    return User(
        id = this.id,
        name = this.name,
        email = this.email,
        role = "trabajador",
        profileImage = this.profileImage,
        description = this.description ?: "Sin descripción",
        rating = this.rating,
        location = null,
        experience = null,
        bio = null,
        skills = null
    )
}
