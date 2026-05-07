package com.example.tasky.ui.create

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasky.data.remote.SupabaseClient
import com.example.tasky.data.remote.dto.JobInsertDto
import com.example.tasky.domain.model.Job
import com.example.tasky.domain.repository.JobRepository
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class CreateJobViewModel(private val repository: JobRepository) : ViewModel() {

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isSuccess by mutableStateOf(false)
    var jobToEdit by mutableStateOf<Job?>(null)

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

    // --- FUNCIÓN PARA CARGAR LOS DATOS ANTES DE EDITAR ---
    fun cargarChamba(jobId: String) {
        viewModelScope.launch {
            isLoading = true
            repository.getJobById(jobId)
                .onSuccess { job ->
                    jobToEdit = job
                }
                .onFailure { error ->
                    errorMessage = "Error al cargar la tarea: ${error.message}"
                }
            isLoading = false
        }
    }

    // Se renombra y adapta la función principal para recibir el ID opcional y el callback del Toast
    fun guardarChamba(
        jobId: String? = null,
        imageUri: Uri?,
        location: GeoPoint,
        title: String,
        category: String,
        payment: Double,
        description: String,
        date: String,
        time: String,
        onSuccess: (String) -> Unit // <-- Recibe un String para el Toast
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val user = SupabaseClient.client.auth.currentUserOrNull()
                val userId = user?.id ?: throw Exception("No hay sesión activa")

                var finalImageUrl: String? = jobToEdit?.imageUrl

                if (imageUri != null) {
                    repository.uploadJobImage(imageUri).onSuccess { finalImageUrl = it }
                }

                val jobData = JobInsertDto(
                    clientId = userId,
                    title = title,
                    category = category,
                    payment = payment,
                    description = description,
                    locationApprox = "${location.latitude},${location.longitude}",
                    scheduledDate = date,
                    scheduledTime = time,
                    imageUrl = finalImageUrl,
                    status = jobToEdit?.status ?: "abierto"
                )

                // Si trae ID, se actualiza. Si viene nulo, se inserta nuevo.
                if (jobId != null) {
                    repository.updateJob(jobId, jobData).onSuccess {
                        onSuccess("Tarea Actualizada")
                    }
                } else {
                    repository.insertJob(jobData).onSuccess {
                        onSuccess("Tarea Creada")
                    }
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