package com.example.tasky.domain.usecase

import com.example.tasky.domain.repository.AuthRepository

class RegisterUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        name: String,
        role: String
    ): Result<Unit> {
        return repository.register(email, password, name, role)
    }
}