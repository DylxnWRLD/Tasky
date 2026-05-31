package com.example.tasky.data.repository

import com.example.tasky.data.remote.SupabaseClient
import com.example.tasky.domain.repository.ForgotPasswordRepository
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email

class ForgotPasswordRepositoryImpl : ForgotPasswordRepository {

    private val supabase = SupabaseClient.client

    override suspend fun resetPasswordForEmail(email: String): Result<Unit> {
        return try {
            supabase.auth.resetPasswordForEmail(email.trim())
            //println("DEBUG_TASKY: Código enviado exitosamente a $email")
            Result.success(Unit)
        } catch (e: Exception) {
            //println("DEBUG_TASKY: Error enviando código -> ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun verifyOtp(email: String, otp: String): Result<Unit> {
        return try {

            supabase.auth.verifyEmailOtp(
                email = email.trim(),
                token = otp.trim(),
                type = OtpType.Email.RECOVERY
            )
            //println("DEBUG_TASKY: OTP verificado exitosamente")
            Result.success(Unit)
        } catch (e: Exception) {
            //println("DEBUG_TASKY: Error verificando OTP -> ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            supabase.auth.updateUser {
                password = newPassword.trim()
            }
            //println("DEBUG_TASKY: Contraseña actualizada exitosamente")

            supabase.auth.signOut()
            //println("DEBUG_TASKY: Sesión cerrada después de actualizar contraseña")

            Result.success(Unit)
        } catch (e: Exception) {
            //println("DEBUG_TASKY: Error actualizando contraseña -> ${e.localizedMessage}")
            Result.failure(e)
        }
    }
}