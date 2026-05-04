package com.example.tasky.ui.jobs.detail

import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.example.tasky.domain.usecase.GetApplicantsUseCase
import kotlinx.coroutines.launch

class JobApplicantsViewModel(
    private val getApplicantsUseCase: GetApplicantsUseCase
) : ViewModel() {

    var state by mutableStateOf(JobApplicantsState())
        private set

    fun loadApplicants(jobId: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)

            getApplicantsUseCase(jobId).fold(
                onSuccess = { list ->
                    // Flujo Normal y FA-01: Si la lista está vacía, se maneja en la UI
                    state = state.copy(applicants = list, isLoading = false)
                },
                onFailure = { error ->
                    // Ex-01: Fallo de consulta
                    state = state.copy(
                        errorMessage = "Error al cargar los postulantes",
                        isLoading = false
                    )
                }
            )
        }
    }
}