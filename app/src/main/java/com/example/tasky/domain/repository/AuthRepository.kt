package com.example.tasky.domain.repository

import com.example.tasky.domain.model.User

interface AuthRepository {
    suspend fun register(
        email: String,
        password: String,
        name: String,
        role: String = "CLIENT"
    ): Result<Unit>

    suspend fun login(
        email: String,
        password: String
    ): Result<User>
}