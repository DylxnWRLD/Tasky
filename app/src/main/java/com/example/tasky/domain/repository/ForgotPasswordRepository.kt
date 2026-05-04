package com.example.tasky.domain.repository

interface ForgotPasswordRepository {
    suspend fun resetPasswordForEmail(email: String): Result<Unit>
    suspend fun verifyOtp(email: String, otp: String): Result<Unit>
    suspend fun updatePassword(newPassword: String): Result<Unit>
}