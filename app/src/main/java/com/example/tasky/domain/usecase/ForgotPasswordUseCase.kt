package com.example.tasky.domain.usecase

import com.example.tasky.domain.repository.ForgotPasswordRepository

class ResetPasswordUseCase(
    private val repository: ForgotPasswordRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return repository.resetPasswordForEmail(email)
    }
}

class VerifyOtpUseCase(
    private val repository: ForgotPasswordRepository
) {
    suspend operator fun invoke(email: String, otp: String): Result<Unit> {
        return repository.verifyOtp(email, otp)
    }
}

class UpdatePasswordUseCase(
    private val repository: ForgotPasswordRepository
) {
    suspend operator fun invoke(newPassword: String): Result<Unit> {
        return repository.updatePassword(newPassword)
    }
}