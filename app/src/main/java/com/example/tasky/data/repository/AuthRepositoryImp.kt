package com.example.tasky.data.repository

import com.example.tasky.data.remote.SupabaseClient
import com.example.tasky.domain.model.User
import com.example.tasky.domain.repository.AuthRepository
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from

class AuthRepositoryImpl : AuthRepository {

    private val supabase = SupabaseClient.client

    override suspend fun register(
        email: String,
        password: String,
        name: String
    ): Result<Unit> {
        return try {
            val result = supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Error al registrar"))

            supabase.from("users").insert(
                mapOf(
                    "id" to userId,
                    "email" to email,
                    "name" to name,
                    "role" to "CLIENT"
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<User> {
        println("TASKY_LOG: Email:|${email}|")
        println("TASKY_LOG: Pass:|${password}|")
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email.trim()
                this.password = password.trim()
            }

            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Usuario no encontrado"))

            val user = supabase
                .from("users")
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingle<User>()

            Result.success(user)

        } catch (e: Exception) {
            println("DEBUG_TASKY: Error detallado -> ${e.localizedMessage}")
            Result.failure(e)
        }
    }
}