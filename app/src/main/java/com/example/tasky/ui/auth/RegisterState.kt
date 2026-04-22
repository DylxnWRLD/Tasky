package com.example.tasky.ui.auth

data class RegisterState(
    val email: String = "",
    val name: String = "",
    val password: String = "",
    val role: String = "cliente",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)