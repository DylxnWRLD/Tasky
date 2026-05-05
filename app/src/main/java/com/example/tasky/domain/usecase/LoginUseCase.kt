package com.example.tasky.domain.usecase

import com.example.tasky.domain.model.User
import com.example.tasky.domain.repository.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        loginIdentifier: String,  // Puede ser email o username
        password: String
    ): Result<User> {
        // Validaciones básicas
        if (loginIdentifier.isBlank()) {
            return Result.failure(IllegalArgumentException("Todos los campos deben estar llenos"))
        }

        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("La contraseña es requerida"))
        }

        // Detectar si es email o username
        val email = if (loginIdentifier.contains("@")) {
            // Es un email, usarlo directamente
            loginIdentifier.trim()
        } else {
            // Es un username, buscar el email asociado
            val emailResult = repository.findEmailByUsername(loginIdentifier.trim())
            if (emailResult.isFailure) {
                return Result.failure(emailResult.exceptionOrNull() ?: Exception("Usuario no encontrado"))
            }
            emailResult.getOrThrow()
        }

        // Intentar login con el email
        return repository.login(email, password)
    }
}