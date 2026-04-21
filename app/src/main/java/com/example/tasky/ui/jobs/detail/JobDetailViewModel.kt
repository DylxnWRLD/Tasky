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

class JobDetailViewModel(
    private val repository: JobRepository = JobRepositoryImpl(),
    private val applyToJobUseCase: ApplyToJobUseCase = ApplyToJobUseCase(repository),
    private val cancelApplicationUseCase: CancelApplicationUseCase = CancelApplicationUseCase(repository),
    private val currentUserId: String? = SupabaseClient.client.auth.currentUserOrNull()?.id
) : ViewModel() {

    var state by mutableStateOf(JobDetailState())
        private set

    fun loadJobById(jobId: String) {
        if (state.job?.id == jobId) return

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
                // IMPRIME ESTO PARA SABER EL ERROR REAL
                println("ERROR_SUPABASE: ${error.localizedMessage}")
                error.printStackTrace()

                state = state.copy(
                    isLoading = false,
                    errorMessage = "Error: ${error.localizedMessage}" // Que el error salga en pantalla
                )
            }
        }
    }

    fun onMainActionClick() {
        if (state.isApplied) {
            confirmCancelApplication()
        } else {
            state = state.copy(showConfirmDialog = true)
        }
    }

    fun onDismissDialog() {
        state = state.copy(showConfirmDialog = false)
    }

    fun confirmApplication() {
        val jobId = state.job?.id ?: return
        state = state.copy(showConfirmDialog = false, isActionLoading = true)

        viewModelScope.launch {
            applyToJobUseCase(jobId).onSuccess {
                state = state.copy(
                    isActionLoading = false,
                    isApplied = true,
                    userMessage = "¡Listo! Te has postulado para este trabajo."
                )
            }.onFailure {
                state = state.copy(isActionLoading = false, errorMessage = "Error al postularse.")
            }
        }
    }

    private fun confirmCancelApplication() {
        val jobId = state.job?.id ?: return
        state = state.copy(isActionLoading = true)

        viewModelScope.launch {
            cancelApplicationUseCase(jobId).onSuccess {
                state = state.copy(
                    isActionLoading = false,
                    isApplied = false,
                    userMessage = "Postulación cancelada con éxito."
                )
            }.onFailure {
                state = state.copy(isActionLoading = false, errorMessage = "No se pudo cancelar.")
            }
        }
    }
}