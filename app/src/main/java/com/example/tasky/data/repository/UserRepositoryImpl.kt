package com.example.tasky.data.repository

import android.content.Context
import android.net.Uri
import com.example.tasky.data.remote.SupabaseClient
import com.example.tasky.domain.model.User
import com.example.tasky.domain.repository.UserRepository
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class UserRepositoryImpl(private val context: Context) : UserRepository {

    private val supabase = SupabaseClient.client

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
            val updates = buildJsonObject {
                name?.let { put("name", JsonPrimitive(it)) }
                location?.let { put("location", JsonPrimitive(it)) }
                experience?.let { put("experience", JsonPrimitive(it)) }
                bio?.let { put("bio", JsonPrimitive(it)) }
                profileImage?.let { put("profile_image", JsonPrimitive(it)) }

                // Para skills, crear un array JSON
                if (!skills.isNullOrEmpty()) {
                    put("skills", buildJsonArray {
                        skills.forEach { skill ->
                            add(JsonPrimitive(skill))
                        }
                    })
                }
            }

            if (updates.isNotEmpty()) {
                supabase.from("users").update(updates) {
                    filter { eq("id", userId) }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
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

    override suspend fun uploadProfileImage(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fileName = "profile_${System.currentTimeMillis()}.jpg"
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                ?: return@withContext Result.failure(Exception("No se pudo leer la imagen"))

            val bucket = supabase.storage.from("imagenes_userProfile")
            bucket.upload(fileName, bytes)
            val publicUrl = bucket.publicUrl(fileName)

            Result.success(publicUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Error al subir imagen: ${e.message}"))
        }
    }
}