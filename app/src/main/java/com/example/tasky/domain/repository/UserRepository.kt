package com.example.tasky.domain.repository

import com.example.tasky.domain.model.User

interface UserRepository {
    suspend fun updateUserProfile(
        userId: String,
        name: String? = null,
        location: String? = null,
        experience: String? = null,
        bio: String? = null,
        skills: List<String>? = null,
        profileImage: String? = null
    ): Result<Unit>

    suspend fun getUserById(userId: String): Result<User>
}