package com.example.tasky.ui.jobs.detail


import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.example.tasky.domain.usecase.ApplyToJobUseCase
import kotlinx.coroutines.launch

class JobDetailViewModel(
    private val applyToJobUseCase: ApplyToJobUseCase
) : ViewModel() {

    var state by mutableStateOf(JobDetailState())
        private set

    fun onApplyClick() {
        if (!state.isUserLoggedIn) {
            return
        }
        state = state.copy(showConfirmDialog = true)
    }

    fun onDismissDialog() {
        state = state.copy(showConfirmDialog = false)
    }

    fun confirmApplication(jobId: String) {
        state = state.copy(showConfirmDialog = false, isLoading = true)

        viewModelScope.launch {
            val result = applyToJobUseCase(jobId)

            result.onSuccess {
                state = state.copy(
                    isLoading = false,
                    userMessage = "Listo, te has postulado para este trabajo. La solicitud se encuentra en revisión",
                    job = state.job?.copy(isApplied = true)
                )
            }.onFailure {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Error: el trabajo ya no se encuentra disponible"
                )
            }
        }
    }
}