package com.example.tasky.ui.jobs.detail

import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.example.tasky.data.remote.SupabaseClient
import com.example.tasky.domain.repository.JobRepository
import com.example.tasky.domain.usecase.ApplyToJobUseCase
import com.example.tasky.data.repository.JobRepositoryImpl
import com.example.tasky.domain.usecase.CancelApplicationUseCase
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class JobDetailViewModel(
    private val repository: JobRepository = JobRepositoryImpl(),
    private val applyToJobUseCase: ApplyToJobUseCase = ApplyToJobUseCase(repository),
    private val cancelApplicationUseCase: CancelApplicationUseCase = CancelApplicationUseCase(repository),
    private val currentUserId: String? = SupabaseClient.client.auth.currentUserOrNull()?.id
) : ViewModel() {

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

    private fun checkCancelApplication(): CancelResult {
        val job = state.job ?: return CancelResult.ALREADY_STARTED

        return try {
            val raw = "${job.date} ${job.time}"
            println("TASKY_LOG: RAW -> $raw")

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val jobDate = sdf.parse(raw) ?: return CancelResult.ALREADY_STARTED

            val now = Date()

            println("TASKY_LOG: Job -> $jobDate")
            println("TASKY_LOG: Now -> $now")

            val diffInMinutes = (jobDate.time - now.time) / (1000.0 * 60)

            println("TASKY_LOG: Diff -> $diffInMinutes")

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
        state = state.copy(showConfirmDialog = false)
    }

    fun confirmAction() {
        val jobId = state.job?.id ?: return

        state = state.copy(
            showConfirmDialog = false,
            isActionLoading = true
        )

        viewModelScope.launch {
            if (state.isApplied) {
                executeCancel(jobId)
            } else {
                executeApply(jobId)
            }
        }
    }

    private suspend fun executeApply(jobId: String) {
        applyToJobUseCase(jobId).onSuccess {
            state = state.copy(
                isActionLoading = false,
                isApplied = true,
                job = state.job?.copy(isApplied = true),
                userMessage = "¡Listo! Te has postulado."
            )
        }.onFailure {
            state = state.copy(
                isActionLoading = false,
                userMessage = "Error al postularse."
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

    fun clearUserMessage() {
        state = state.copy(userMessage = null)
    }
}