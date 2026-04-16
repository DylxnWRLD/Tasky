package com.example.tasky.ui.jobs.detail

import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.example.tasky.domain.repository.JobRepository
import com.example.tasky.domain.usecase.ApplyToJobUseCase
import com.example.tasky.data.repository.JobRepositoryImpl
import kotlinx.coroutines.launch

class JobDetailViewModel(
    private val repository: JobRepository = JobRepositoryImpl(),
    private val applyToJobUseCase: ApplyToJobUseCase = ApplyToJobUseCase(repository)
) : ViewModel() {

    var state by mutableStateOf(JobDetailState())
        private set

    // Carga los datos al entrar a la pantalla
    fun loadJobById(jobId: String) {
        if (state.job?.id == jobId) return

        state = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            repository.getJobById(jobId).onSuccess { job ->
                state = state.copy(job = job, isLoading = false)
            }.onFailure {
                state = state.copy(isLoading = false, errorMessage = "No se encontró el trabajo")
            }
        }
    }

    fun onApplyClick() {
        state = state.copy(showConfirmDialog = true)
    }

    fun onDismissDialog() {
        state = state.copy(showConfirmDialog = false)
    }

    // Ejecuta el CU-17
    fun confirmApplication(jobId: String) {
        state = state.copy(showConfirmDialog = false, isApplying = true)
        viewModelScope.launch {
            applyToJobUseCase(jobId).onSuccess {
                state = state.copy(
                    isApplying = false,
                    userMessage = "Listo, te has postulado para este trabajo. La solicitud se encuentra en revisión",
                    job = state.job?.copy(isApplied = true) // Postcondición
                )
            }.onFailure {
                state = state.copy(
                    isApplying = false,
                    errorMessage = "Error: el trabajo ya no se encuentra disponible"
                )
            }
        }
    }
}