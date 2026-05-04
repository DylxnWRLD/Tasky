package com.example.tasky.ui.auth

data class ForgotPasswordState(
    val email: String = "",
    val otp: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEmailSent: Boolean = false,
    val isOtpVerified: Boolean = false,
    val isPasswordUpdated: Boolean = false
)