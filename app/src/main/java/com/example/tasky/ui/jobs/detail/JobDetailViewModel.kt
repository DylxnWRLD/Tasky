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

    fun loadJobById(jobId: String) {
        if (state.job?.id == jobId && !state.isLoading) return
        state = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            repository.getJobById(jobId).onSuccess { job ->
                state = state.copy(
                    job = job,
                    isLoading = false,
                    isOwner = job.clientId == currentUserId,
                    isApplied = job.isApplied
                )
            }.onFailure { error ->
                state = state.copy(isLoading = false, errorMessage = "Error: ${error.localizedMessage}")
            }
        }
    }

    private fun canCancelApplication(): Boolean {
        val job = state.job ?: return false
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val jobDate = sdf.parse("${job.date} ${job.time}") ?: return false

            val now = Calendar.getInstance().time

            val diffInMs = jobDate.time - now.time
            val diffInMinutes = diffInMs / (1000 * 60)

            diffInMinutes >= 60
        } catch (e: Exception) {
            false
        }
    }

    fun onMainActionClick() {
        if (state.isApplied) {
            if (canCancelApplication()) {
                state = state.copy(showConfirmDialog = true)
            } else {
                state = state.copy(
                    userMessage = "No puedes cancelar, faltan menos de 60 minutos."
                )
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
        state = state.copy(showConfirmDialog = false, isActionLoading = true)

        viewModelScope.launch {
            if (state.isApplied) executeCancel(jobId) else executeApply(jobId)
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
            state = state.copy(isActionLoading = false, userMessage = "Error al postularse.")
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
            state = state.copy(isActionLoading = false, userMessage = "No se pudo cancelar.")
        }
    }

    fun clearUserMessage() {
        state = state.copy(userMessage = null)
    }
}