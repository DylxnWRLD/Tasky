package com.example.tasky.domain.usecase

import com.example.tasky.domain.model.User
import com.example.tasky.domain.repository.AuthRepository

class GetUserProfileUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User> {
        return authRepository.getCurrentUser()
    }
}