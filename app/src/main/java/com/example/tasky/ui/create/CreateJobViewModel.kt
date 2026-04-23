package com.example.tasky.ui.create

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasky.data.remote.SupabaseClient
import com.example.tasky.data.remote.dto.JobInsertDto
import com.example.tasky.domain.repository.JobRepository
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class CreateJobViewModel(private val repository: JobRepository) : ViewModel() {

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isSuccess by mutableStateOf(false)

    fun publicarChamba(
        imageUri: Uri?,
        location: GeoPoint,
        title: String,
        category: String,
        payment: Double,
        description: String,
        date: String,
        time: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                // 1. Sacar el ID del usuario que anda publicando
                 val user = SupabaseClient.client.auth.currentUserOrNull()
                 val userId = user?.id ?: throw Exception("No hay sesión activa")

                // 2. Si hay imagen, se sube primero para tener el link
                var finalImageUrl: String? = null
                if (imageUri != null) {
                    repository.uploadJobImage(imageUri).onSuccess { url ->
                        finalImageUrl = url
                    }.onFailure {
                        throw Exception("Falló la subida de la imagen: ${it.message}")
                    }
                }

                // 3. Armar el DTO para el insert
                val nuevoJob = JobInsertDto(
                    clientId = userId,
                    title = title,
                    category = category,
                    payment = payment,
                    description = description,
                    locationApprox = "${location.latitude},${location.longitude}",
                    scheduledDate = date,
                    scheduledTime = time,
                    imageUrl = finalImageUrl,
                    status = "abierto"
                )

                repository.insertJob(nuevoJob).onSuccess {
                    isSuccess = true
                    onSuccess()
                }.onFailure {
                    throw Exception("No se pudo guardar el registro: ${it.message}")
                }

            } catch (e: Exception) {
                errorMessage = e.message ?: "Error desconocido"
            } finally {
                isLoading = false
            }
        }
    }

    fun resetState() {
        isSuccess = false
        errorMessage = null
    }
}