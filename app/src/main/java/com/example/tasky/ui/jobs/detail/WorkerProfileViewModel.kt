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
import com.example.tasky.domain.usecase.RejectPostulantUseCase
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
    val isCurrentWorkerAccepted: Boolean = false,
    val showRejectConfirmDialog: Boolean = false,
    val showReasonScreen: Boolean = false,
    val isCurrentWorkerRejected: Boolean = false
)

class WorkerProfileViewModel(
    private val getWorkerProfileUseCase: GetWorkerProfileUseCase,
    private val acceptApplicantUseCase: AcceptApplicantUseCase,
    private val rejectPostulantUseCase: RejectPostulantUseCase
) : ViewModel() {

    var state by mutableStateOf(WorkerProfileState())
        private set

    fun loadProfile(workerId: String, jobId: String) {
        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null,
                successMessage = null,
                isCancelled = false
            )

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
                        isCurrentWorkerAccepted = postulationData.isCurrentWorkerAccepted,
                        isCurrentWorkerRejected = postulationData.isCurrentWorkerRejected
                    )
                },
                onFailure = { error ->
                    if (error.message == "postulacion_cancelada") {
                        state = state.copy(
                            isCancelled = true,
                            error = "Este trabajador ha cancelado su postulación",
                            isLoading = false
                        )
                    } else {
                        state = state.copy(
                            error = error.message ?: "Error al cargar el perfil del trabajador",
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    private suspend fun fetchPostulationsStatus(
        workerId: String,
        jobId: String
    ): PostulationStatusData = withContext(Dispatchers.IO) {
        try {
            val allPostulations = SupabaseClient.client.postgrest.from("postulaciones")
                .select(Columns.list("worker_id", "status")) {
                    filter {
                        eq("job_id", jobId)
                    }
                }.decodeList<JsonObject>()

            val isJobClosed = allPostulations.any {
                it["status"]?.toString()?.replace("\"", "") == "aceptado"
            }

            val isCurrentWorkerAccepted = allPostulations.any {
                val wId = it["worker_id"]?.toString()?.replace("\"", "")
                val status = it["status"]?.toString()?.replace("\"", "")
                wId == workerId && status == "aceptado"
            }

            val isCurrentWorkerRejected = allPostulations.any {
                val wId = it["worker_id"]?.toString()?.replace("\"", "")
                val status = it["status"]?.toString()?.replace("\"", "")
                wId == workerId && status == "rechazado"
            }

            PostulationStatusData(
                isJobClosed = isJobClosed,
                isCurrentWorkerAccepted = isCurrentWorkerAccepted,
                isCurrentWorkerRejected = isCurrentWorkerRejected
            )
        } catch (e: Exception) {
            PostulationStatusData(
                isJobClosed = false,
                isCurrentWorkerAccepted = false,
                isCurrentWorkerRejected = false
            )
        }
    }

    fun onAcceptClicked() {
        state = state.copy(showConfirmDialog = true)
    }

    fun onCancelDialog() {
        state = state.copy(showConfirmDialog = false)
    }

    // Caso de uso 19 - Aceptar trabajador
    fun confirmAcceptance(workerId: String, jobId: String) {
        viewModelScope.launch {
            state = state.copy(
                showConfirmDialog = false,
                isLoading = true,
                error = null,
                successMessage = null
            )

            acceptApplicantUseCase(workerId, jobId).fold(
                onSuccess = {
                    state = state.copy(
                        isLoading = false,
                        isJobClosed = true,
                        isCurrentWorkerAccepted = true,
                        successMessage = "Has aceptado a ${state.worker?.name} para tu trabajo"
                    )
                },
                onFailure = { error ->
                    state = state.copy(
                        isLoading = false,
                        error = error.message ?: "Error al cargar los postulantes"
                    )
                }
            )
        }
    }

    // Caso de uso 20 - Rechazar trabajador
    fun onRejectClicked() {
        state = state.copy(showRejectConfirmDialog = true)
    }

    fun onCancelRejectDialog() {
        state = state.copy(showRejectConfirmDialog = false)
    }

    fun onConfirmRejectionClick() {
        state = state.copy(
            showRejectConfirmDialog = false,
            showReasonScreen = true
        )
    }

    fun submitRejection(workerId: String, jobId: String, reason: String) {
        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null,
                successMessage = null
            )

            val result = rejectPostulantUseCase(
                workerId = workerId,
                jobId = jobId,
                reason = reason.trim()
            )

            result.onSuccess {
                state = state.copy(
                    isLoading = false,
                    showReasonScreen = false,
                    successMessage = "Has rechazado a ${state.worker?.name ?: "al postulante"} para tu trabajo"
                )
            }.onFailure { error ->
                if (error.message == "no_disponible") {
                    state = state.copy(
                        isLoading = false,
                        error = "Error: el postulante ya no se encuentra disponible",
                        isCancelled = true
                    )
                } else {
                    state = state.copy(
                        isLoading = false,
                        error = error.message ?: "Error al procesar el rechazo en el servidor"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        state = state.copy(
            successMessage = null,
            error = null
        )
    }

    private data class PostulationStatusData(
        val isJobClosed: Boolean,
        val isCurrentWorkerAccepted: Boolean,
        val isCurrentWorkerRejected: Boolean
    )
}