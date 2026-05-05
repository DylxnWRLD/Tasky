package com.example.tasky.ui.jobs.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasky.domain.model.User
import com.example.tasky.domain.usecase.GetWorkerProfileUseCase
import kotlinx.coroutines.launch

data class WorkerProfileState(
    val worker: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCancelled: Boolean = false
)

class WorkerProfileViewModel (
    private val getWorkerProfileUseCase: GetWorkerProfileUseCase
): ViewModel(){
    var state by mutableStateOf(WorkerProfileState())
        private set

    fun loadProfile(workerId: String, jobId: String){
        viewModelScope.launch {
            println("WorkerId: $workerId")
            println("JobId: $jobId")

            state = state.copy(isLoading = true)

            getWorkerProfileUseCase(workerId, jobId).fold(
                onSuccess = { worker ->
                    println("Usuario cargado: ${worker.name}")

                    state = state.copy(
                        worker = worker,
                        isLoading = false
                    )
                },
                onFailure = { error ->

                    println("ERROR: ${error.message}")

                    if(error.message?.contains("cancelada") == true) {
                        state = state.copy(
                            isCancelled = true,
                            isLoading = false
                        )
                    } else{
                        state = state.copy(
                            error = "Error al cargar el perfil",
                            isLoading = false
                        )
                    }
                }
            )
        }
    }
    }

