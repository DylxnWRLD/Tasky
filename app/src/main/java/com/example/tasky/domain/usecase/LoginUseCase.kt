package com.example.tasky.domain.usecase

import com.example.tasky.domain.model.User
import com.example.tasky.domain.repository.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<User> {
        return repository.login(email, password)
    }
}