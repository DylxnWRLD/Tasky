package com.example.tasky.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasky.domain.usecase.ResetPasswordUseCase
import com.example.tasky.domain.usecase.VerifyOtpUseCase
import com.example.tasky.domain.usecase.UpdatePasswordUseCase
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val updatePasswordUseCase: UpdatePasswordUseCase
) : ViewModel() {

    var state by mutableStateOf(ForgotPasswordState())
        private set

    fun onEmailChange(value: String) {
        state = state.copy(email = value, error = null)
    }

    fun onOtpChange(value: String) {
        state = state.copy(otp = value, error = null)
    }

    fun onNewPasswordChange(value: String) {
        state = state.copy(newPassword = value, error = null)
    }

    fun onConfirmPasswordChange(value: String) {
        state = state.copy(confirmPassword = value, error = null)
    }

    fun sendResetCode() {
        if (state.email.isBlank()) {
            state = state.copy(error = "Ingresa tu correo electrónico")
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            val result = resetPasswordUseCase(state.email.trim())

            state = if (result.isSuccess) {
                state.copy(
                    isLoading = false,
                    isEmailSent = true,
                    error = null
                )
            } else {
                state.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error al enviar el código"
                )
            }
        }
    }

    fun verifyOtp() {
        if (state.otp.length < 8) {
            state = state.copy(error = "El código debe tener 6 dígitos")
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            val result = verifyOtpUseCase(state.email.trim(), state.otp.trim())

            state = if (result.isSuccess) {
                state.copy(
                    isLoading = false,
                    isOtpVerified = true,
                    error = null
                )
            } else {
                state.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Código inválido"
                )
            }
        }
    }

    fun updatePassword() {
        when {
            state.newPassword.length < 6 -> {
                state = state.copy(error = "La contraseña debe tener al menos 6 caracteres")
                return
            }
            state.newPassword != state.confirmPassword -> {
                state = state.copy(error = "Las contraseñas no coinciden")
                return
            }
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            val result = updatePasswordUseCase(state.newPassword)

            state = if (result.isSuccess) {
                state.copy(
                    isLoading = false,
                    isPasswordUpdated = true,
                    error = null
                )
            } else {
                state.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error al actualizar la contraseña"
                )
            }
        }
    }

    fun resetState() {
        state = ForgotPasswordState()
    }
}