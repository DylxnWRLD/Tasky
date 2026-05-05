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
    val rating: Double? = 0.0,
    val location: String? = null,
    val experience: String? = null,
    val bio: String? = null,
    val skills: List<String>? = null
)

fun UserDto.toDomain(): User {

    return User(
        id = this.id,
        name = this.name,
        email = this.email,
        role = "trabajador",

        profileImage = this.profileImage,
        description = this.description,
        rating = this.rating ?: 0.0,
        location = this.location,
        experience = this.experience,
        bio = this.bio,
        skills = this.skills
    )
}
