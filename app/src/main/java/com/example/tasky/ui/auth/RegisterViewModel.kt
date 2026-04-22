package com.example.tasky.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasky.domain.usecase.RegisterUseCase
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    var state by mutableStateOf(RegisterState())
        private set

    fun onEmailChange(value: String) {
        state = state.copy(email = value)
    }

    fun onNameChange(value: String) {
        state = state.copy(name = value)
    }

    fun onPasswordChange(value: String) {
        state = state.copy(password = value)
    }

    fun onRoleChange(value: String) {
        state = state.copy(role = value)
    }

    fun register() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null, isSuccess = false)

            val result = registerUseCase(
                email = state.email,
                password = state.password,
                name = state.name,
                role = state.role
            )

            state = if (result.isSuccess) {
                state.copy(
                    isLoading = false,
                    isSuccess = true
                )
            } else {
                state.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error en el registro"
                )
            }
        }
    }

    fun resetState() {
        state = RegisterState()
    }
}