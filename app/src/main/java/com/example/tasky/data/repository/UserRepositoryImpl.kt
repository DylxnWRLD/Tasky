package com.example.tasky.data.repository

import com.example.tasky.data.remote.SupabaseClient
import com.example.tasky.domain.model.User
import com.example.tasky.domain.repository.UserRepository
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class UserRepositoryImpl : UserRepository {

    private val supabase = SupabaseClient.client

    @Serializable
    data class UserUpdate(
        val name: String? = null,
        val location: String? = null,
        val experience: String? = null,
        val bio: String? = null,
        val skills: List<String>? = null,
        @SerialName("profile_image")
        val profileImage: String? = null
    )

    override suspend fun updateUserProfile(
        userId: String,
        name: String?,
        location: String?,
        experience: String?,
        bio: String?,
        skills: List<String>?,
        profileImage: String?
    ): Result<Unit> {
        return try {
            // Crear un mapa con tipos específicos (String, no Any?)
            val updates = mutableMapOf<String, String?>()

            name?.let { updates["name"] = it }
            location?.let { updates["location"] = it }
            experience?.let { updates["experience"] = it }
            bio?.let { updates["bio"] = it }
            profileImage?.let { updates["profile_image"] = it }

            // Manejar skills por separado ya que es una lista
            if (skills != null) {
                supabase.from("users").update(
                    mapOf("skills" to skills)
                ) {
                    filter { eq("id", userId) }
                }
            }

            // Actualizar el resto de campos
            if (updates.isNotEmpty()) {
                supabase.from("users").update(updates) {
                    filter { eq("id", userId) }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserById(userId: String): Result<User> {
        return try {
            val user = supabase
                .from("users")
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingle<User>()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}