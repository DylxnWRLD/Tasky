package com.example.tasky.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordState,
    onEmailChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSendResetCode: () -> Unit,
    onVerifyOtp: () -> Unit,
    onUpdatePassword: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF7B8EDB))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // HEADER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tasky",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }

            // CARD
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when {
                        // Pantalla de actualización de contraseña
                        state.isOtpVerified -> {
                            PasswordUpdateContent(
                                state = state,
                                onNewPasswordChange = onNewPasswordChange,
                                onConfirmPasswordChange = onConfirmPasswordChange,
                                onUpdatePassword = onUpdatePassword
                            )
                        }
                        // Pantalla de verificación de código
                        state.isEmailSent -> {
                            OtpVerificationContent(
                                state = state,
                                email = state.email,
                                onOtpChange = onOtpChange,
                                onVerifyOtp = onVerifyOtp,
                                onSendResetCode = onSendResetCode
                            )
                        }
                        // Pantalla inicial - solicitar email
                        else -> {
                            EmailRequestContent(
                                state = state,
                                onEmailChange = onEmailChange,
                                onSendResetCode = onSendResetCode,
                                onNavigateToLogin = onNavigateToLogin
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmailRequestContent(
    state: ForgotPasswordState,
    onEmailChange: (String) -> Unit,
    onSendResetCode: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Recuperar contraseña",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Ingresa tu correo electrónico y te enviaremos un código de recuperación",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Text("Correo electrónico")
        TextField(
            value = state.email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                capitalization = KeyboardCapitalization.None
            ),
            singleLine = true,
            placeholder = { Text("ejemplo@correo.com") }
        )

        Button(
            onClick = onSendResetCode,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            enabled = !state.isLoading
        ) {
            Text("Enviar código de recuperación")
        }

        TextButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al inicio de sesión")
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        state.error?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun OtpVerificationContent(
    state: ForgotPasswordState,
    email: String,
    onOtpChange: (String) -> Unit,
    onVerifyOtp: () -> Unit,
    onSendResetCode: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Verificar código",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Hemos enviado un código de 8 dígitos a $email",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Text("Código de verificación")
        TextField(
            value = state.otp,
            onValueChange = { value ->
                // Solo permitir números y máximo 6 dígitos
                if (value.all { it.isDigit() } && value.length <= 8) {
                    onOtpChange(value)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            placeholder = { Text("00000000") },
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                letterSpacing = 6.sp
            )
        )

        Button(
            onClick = onVerifyOtp,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            enabled = !state.isLoading && state.otp.length == 8
        ) {
            Text("Verificar código")
        }

        TextButton(
            onClick = onSendResetCode,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            Text("Reenviar código")
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        state.error?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun PasswordUpdateContent(
    state: ForgotPasswordState,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onUpdatePassword: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Nueva contraseña",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Ingresa tu nueva contraseña",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Text("Nueva contraseña")
        TextField(
            value = state.newPassword,
            onValueChange = onNewPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                capitalization = KeyboardCapitalization.None
            ),
            singleLine = true
        )

        Text("Confirmar contraseña")
        TextField(
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                capitalization = KeyboardCapitalization.None
            ),
            singleLine = true
        )

        Button(
            onClick = onUpdatePassword,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            enabled = !state.isLoading
        ) {
            Text("Cambiar contraseña")
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        state.error?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun ForgotPasswordRoute(
    viewModel: ForgotPasswordViewModel,
    onNavigateToLogin: () -> Unit,
    onPasswordUpdated: () -> Unit
) {
    val state = viewModel.state

    LaunchedEffect(state.isPasswordUpdated) {
        if (state.isPasswordUpdated) {
            onPasswordUpdated()
        }
    }

    ForgotPasswordScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onOtpChange = viewModel::onOtpChange,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSendResetCode = viewModel::sendResetCode,
        onVerifyOtp = viewModel::verifyOtp,
        onUpdatePassword = viewModel::updatePassword,
        onNavigateToLogin = onNavigateToLogin
    )
}