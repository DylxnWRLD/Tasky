package com.example.tasky.ui.auth

import com.example.tasky.domain.model.User

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)