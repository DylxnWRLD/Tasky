package com.example.tasky.ui.jobs.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasky.data.remote.SupabaseClient
import com.example.tasky.domain.model.User
import com.example.tasky.domain.usecase.GetWorkerProfileUseCase
import com.example.tasky.domain.usecase.AcceptApplicantUseCase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject

data class WorkerProfileState(
    val worker: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showConfirmDialog: Boolean = false,
    val isCancelled: Boolean = false,

    val isJobClosed: Boolean = false,
    val isCurrentWorkerAccepted: Boolean = false
)

class WorkerProfileViewModel (
    private val getWorkerProfileUseCase: GetWorkerProfileUseCase,
    private val acceptApplicantUseCase: AcceptApplicantUseCase
): ViewModel() {

    var state by mutableStateOf(WorkerProfileState())
        private set

    fun loadProfile(workerId: String, jobId: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)

            val profileDeferred = async { getWorkerProfileUseCase(workerId, jobId) }
            val postulationsDeferred = async { fetchPostulationsStatus(workerId, jobId) }

            val profileResult = profileDeferred.await()
            val postulationData = postulationsDeferred.await()

            profileResult.fold(
                onSuccess = { worker ->
                    state = state.copy(
                        worker = worker,
                        isLoading = false,
                        isJobClosed = postulationData.isJobClosed,
                        isCurrentWorkerAccepted = postulationData.isCurrentWorkerAccepted
                    )
                },
                onFailure = { error ->
                    if(error.message?.contains("cancelada") == true) {
                        state = state.copy(isCancelled = true, isLoading = false)
                    } else {
                        state = state.copy(error = "Error al cargar el perfil", isLoading = false)
                    }
                }
            )
        }
    }

    private suspend fun fetchPostulationsStatus(workerId: String, jobId: String): PostulationStatusData = withContext(Dispatchers.IO) {
        try {
            val acceptedResponse = SupabaseClient.client.postgrest.from("postulaciones")
                .select(Columns.list("worker_id", "status")) {
                    filter {
                        eq("job_id", jobId)
                        eq("status", "aceptado")
                    }
                }.decodeList<JsonObject>()

            val isJobClosed = acceptedResponse.isNotEmpty()

            val isCurrentWorkerAccepted = if (isJobClosed) {
                val acceptedId = acceptedResponse.firstOrNull()?.get("worker_id")
                    ?.toString()?.replace("\"", "")
                acceptedId == workerId
            } else {
                false
            }

            PostulationStatusData(isJobClosed = isJobClosed, isCurrentWorkerAccepted = isCurrentWorkerAccepted)
        } catch (e: Exception) {
            PostulationStatusData(isJobClosed = false, isCurrentWorkerAccepted = false)
        }
    }

    fun onAcceptClicked() {
        state = state.copy(showConfirmDialog = true)
    }

    fun onCancelDialog() {
        state = state.copy(showConfirmDialog = false)
    }

    fun confirmAcceptance(workerId: String, jobId: String) {
        viewModelScope.launch {
            state = state.copy(showConfirmDialog = false, isLoading = true)

            acceptApplicantUseCase(workerId, jobId).fold(
                onSuccess = {
                    state = state.copy(
                        isLoading = false,
                        isJobClosed = true,
                        isCurrentWorkerAccepted = true,
                        successMessage = "Has aceptado a ${state.worker?.name} para tu trabajo"
                    )
                },
                onFailure = {
                    state = state.copy(
                        isLoading = false,
                        error = "Error: el postulante ya no se encuentra disponible"
                    )
                }
            )
        }
    }

    fun clearMessages() {
        state = state.copy(successMessage = null, error = null)
    }

    private data class PostulationStatusData(
        val isJobClosed: Boolean,
        val isCurrentWorkerAccepted: Boolean
    )
}