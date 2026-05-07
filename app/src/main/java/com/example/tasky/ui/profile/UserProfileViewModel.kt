package com.example.tasky.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasky.data.remote.SupabaseClient
import com.example.tasky.domain.model.Job
import com.example.tasky.domain.repository.JobRepository
import com.example.tasky.domain.usecase.GetUserProfileUseCase
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val jobRepository: JobRepository
) : ViewModel() {

    var state by mutableStateOf(UserProfileState())
        private set

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)

            val result = getUserProfileUseCase()

            state = if (result.isSuccess) {
                state.copy(
                    isLoading = false,
                    user = result.getOrNull()
                )
            } else {
                state.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    var myJobs by mutableStateOf<List<Job>>(emptyList())
        private set

    var isLoadingJobs by mutableStateOf(false)
        private set

    init {
        // Llama a tus otras funciones de carga aquí
        loadMyJobs()
    }

    private fun loadMyJobs() {
        viewModelScope.launch {
            isLoadingJobs = true
            try {
                // Sacamos la ID del cabrón que está usando la app
                val user = SupabaseClient.client.auth.currentUserOrNull()
                val userId = user?.id

                if (userId != null) {
                    jobRepository.getJobsByClientId(userId)
                        .onSuccess { jobs ->
                            myJobs = jobs
                        }
                        .onFailure {
                            // Aquí podrías meter un estado de error si quieres
                        }
                }
            } catch (e: Exception) {
                // Manejo de la excepción a la verga
            } finally {
                isLoadingJobs = false
            }
        }
    }

    fun clearError() {
        state = state.copy(error = null)
    }
}