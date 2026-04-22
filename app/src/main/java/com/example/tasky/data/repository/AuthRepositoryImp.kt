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
        name: String,
        role: String
    ): Result<Unit> {
        return try {
            // Registrar usuario en Auth
            supabase.auth.signUpWith(Email) {
                this.email = email.trim()
                this.password = password.trim()
            }

            // Obtener el ID del usuario recién creado
            val userId = supabase.auth.currentUserOrNull()?.id
            if (userId == null) {
                println("DEBUG_TASKY: No se pudo obtener el ID del usuario después del registro")
                return Result.failure(Exception("Error al obtener el ID del usuario"))
            }

            println("DEBUG_TASKY: Usuario registrado con ID: $userId")


            try {
                supabase.from("users").update(
                    mapOf(
                        "name" to name.trim(),
                        "role" to role
                    )
                ) {
                    filter { eq("id", userId) }
                }
                println("DEBUG_TASKY: Usuario actualizado exitosamente")
            } catch (e: Exception) {
                println("DEBUG_TASKY: Error actualizando usuario -> ${e.localizedMessage}")

            }

            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG_TASKY: Error en registro -> ${e.localizedMessage}")
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