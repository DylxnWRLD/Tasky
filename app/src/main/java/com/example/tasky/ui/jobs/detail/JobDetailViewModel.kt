package com.example.tasky.ui.jobs.detail

import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.example.tasky.data.remote.SupabaseClient
import com.example.tasky.domain.repository.JobRepository
import com.example.tasky.domain.usecase.ApplyToJobUseCase
import com.example.tasky.domain.usecase.CancelApplicationUseCase
import com.example.tasky.domain.usecase.ChangeJobStatusUseCase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class JobDetailViewModel(
    private val repository: JobRepository,
    private val applyToJobUseCase: ApplyToJobUseCase,
    private val changeJobStatusUseCase: ChangeJobStatusUseCase,
    private val currentUserId: String? = SupabaseClient.client.auth.currentUserOrNull()?.id
) : ViewModel() {

    private val cancelApplicationUseCase = CancelApplicationUseCase(repository)

    var state by mutableStateOf(JobDetailState())
        private set

    enum class CancelResult {
        CAN_CANCEL,
        LESS_THAN_60_MIN,
        ALREADY_STARTED
    }

    fun loadJobById(jobId: String) {
        if (state.job?.id == jobId && !state.isLoading) return
        fetchJobData(jobId)
    }

    private fun fetchJobData(jobId: String, onComplete: () -> Unit = {}) {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            repository.getJobById(jobId).onSuccess { job ->
                state = state.copy(
                    job = job,
                    isLoading = false,
                    isOwner = job.clientId == currentUserId,
                    isApplied = job.isApplied
                )
                onComplete()
            }.onFailure { error ->
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Error: ${error.localizedMessage}"
                )
            }
        }
    }

    fun checkRejectionStatus(jobId: String) {
        viewModelScope.launch {
            val userId = currentUserId ?: SupabaseClient.client.auth.currentUserOrNull()?.id

            if (userId != null) {
                try {
                    val postulation = withContext(Dispatchers.IO) {
                        SupabaseClient.client.postgrest.from("postulaciones")
                            .select(Columns.list("status")) {
                                filter {
                                    eq("job_id", jobId)
                                    eq("worker_id", userId)
                                }
                            }.decodeList<JsonObject>()
                    }

                    val statusActual = postulation.firstOrNull()?.get("status")
                        ?.toString()
                        ?.replace("\"", "")

                    if (statusActual == "rechazado") {
                        state = state.copy(isCurrentWorkerRejected = true)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Caso de uso 21 - Cambiar estado del trabajo
    fun onStatusButtonClick() {
        state = state.copy(showStatusDialog = true)
    }

    fun onStatusSelected(status: String) {
        state = state.copy(
            showStatusDialog = false,
            selectedStatus = status,
            showConfirmDialog = true
        )
    }

    fun onDismissStatusDialog() {
        state = state.copy(showStatusDialog = false)
    }

    private suspend fun executeStatusChange(jobId: String, newStatus: String) {
        try {
            val result = changeJobStatusUseCase(jobId, newStatus)

            if (result.isSuccess) {
                state = state.copy(
                    isActionLoading = false,
                    job = state.job?.copy(status = newStatus),
                    selectedStatus = null,
                    userMessage = "El estado ha sido cambiado a $newStatus"
                )
            } else {
                state = state.copy(
                    isActionLoading = false,
                    selectedStatus = null,
                    userMessage = "Error: no se ha podido cambiar el estado del trabajo."
                )
            }
        } catch (e: IOException) {
            state = state.copy(
                isActionLoading = false,
                selectedStatus = null,
                userMessage = "Error al cambiar de estado: Sin conexión a internet."
            )
        } catch (e: Exception) {
            state = state.copy(
                isActionLoading = false,
                selectedStatus = null,
                userMessage = "Error inesperado al cambiar el estado."
            )
        }
    }

    private fun checkCancelApplication(): CancelResult {
        val job = state.job ?: return CancelResult.ALREADY_STARTED

        return try {
            val raw = "${job.date} ${job.time}"
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val jobDate = sdf.parse(raw) ?: return CancelResult.ALREADY_STARTED
            val now = Date()
            val diffInMinutes = (jobDate.time - now.time) / (1000.0 * 60)

            when {
                diffInMinutes <= 0 -> CancelResult.ALREADY_STARTED
                diffInMinutes < 60 -> CancelResult.LESS_THAN_60_MIN
                else -> CancelResult.CAN_CANCEL
            }
        } catch (e: Exception) {
            e.printStackTrace()
            CancelResult.ALREADY_STARTED
        }
    }

    fun onMainActionClick() {
        val jobId = state.job?.id ?: return

        if (state.isApplied) {
            state = state.copy(isActionLoading = true)

            fetchJobData(jobId) {
                state = state.copy(isActionLoading = false)

                when (checkCancelApplication()) {
                    CancelResult.CAN_CANCEL -> {
                        state = state.copy(showConfirmDialog = true)
                    }

                    CancelResult.LESS_THAN_60_MIN -> {
                        state = state.copy(
                            userMessage = "No puedes cancelar, faltan menos de 60 minutos."
                        )
                    }

                    CancelResult.ALREADY_STARTED -> {
                        state = state.copy(
                            userMessage = "Este trabajo ya comenzó o ya pasó."
                        )
                    }
                }
            }
        } else {
            state = state.copy(showConfirmDialog = true)
        }
    }

    fun onDismissDialog() {
        state = state.copy(
            showConfirmDialog = false,
            selectedStatus = null
        )
    }

    fun confirmAction() {
        val jobId = state.job?.id ?: return

        state = state.copy(
            showConfirmDialog = false,
            isActionLoading = true
        )

        viewModelScope.launch {
            val statusAEnviar = state.selectedStatus

            if (statusAEnviar != null) {
                executeStatusChange(jobId, statusAEnviar)
            } else {
                if (state.isApplied) {
                    executeCancel(jobId)
                } else {
                    executeApply(jobId)
                }
            }
        }
    }

    // Caso de uso 17 - Postularse a un trabajo
    private suspend fun executeApply(jobId: String) {
        applyToJobUseCase(jobId).onSuccess {
            state = state.copy(
                isActionLoading = false,
                isApplied = true,
                job = state.job?.copy(isApplied = true),
                userMessage = "Listo, te has postulado para este trabajo. La solicitud se encuentra en revisión"
            )
        }.onFailure { error ->
            state = state.copy(
                isActionLoading = false,
                userMessage = error.message ?: "Error: el trabajo ya no se encuentra disponible"
            )
        }
    }

    private suspend fun executeCancel(jobId: String) {
        cancelApplicationUseCase(jobId).onSuccess {
            state = state.copy(
                isActionLoading = false,
                isApplied = false,
                job = state.job?.copy(isApplied = false),
                userMessage = "Postulación cancelada con éxito."
            )
        }.onFailure {
            state = state.copy(
                isActionLoading = false,
                userMessage = "No se pudo cancelar."
            )
        }
    }

    fun eliminarChamba(onSuccess: () -> Unit) {
        val jobId = state.job?.id ?: return
        state = state.copy(isActionLoading = true)

        viewModelScope.launch {
            repository.deleteJob(jobId).onSuccess {
                state = state.copy(isActionLoading = false)
                onSuccess()
            }.onFailure { error ->
                state = state.copy(
                    isActionLoading = false,
                    userMessage = "No se pudo borrar: ${error.message}"
                )
            }
        }
    }

    fun clearUserMessage() {
        state = state.copy(userMessage = null)
    }

    fun liberarTrabajo() {
        val jobId = state.job?.id ?: return
        val workerId = state.job?.acceptedWorkerId ?: return

        state = state.copy(isActionLoading = true)

        viewModelScope.launch {
            repository.cancelWorkerSelection(jobId, workerId).onSuccess {
                fetchJobData(jobId) {
                    state = state.copy(
                        isActionLoading = false,
                        userMessage = "El trabajo ha sido reabierto con éxito."
                    )
                }
            }.onFailure { error ->
                state = state.copy(
                    isActionLoading = false,
                    userMessage = "No se pudo reabrir: ${error.localizedMessage}"
                )
            }
        }
    }
}