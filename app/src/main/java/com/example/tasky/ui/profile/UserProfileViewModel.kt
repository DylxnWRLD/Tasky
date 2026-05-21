package com.example.tasky.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasky.data.remote.SupabaseClient
import com.example.tasky.domain.model.Job
import com.example.tasky.domain.model.User
import com.example.tasky.domain.repository.JobRepository
import com.example.tasky.domain.usecase.GetUserProfileUseCase
import com.example.tasky.domain.usecase.UpdateUserProfileUseCase
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val jobRepository: JobRepository,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {

    var state by mutableStateOf(UserProfileState())
        private set

    var myJobs by mutableStateOf<List<Job>>(emptyList())
        private set

    var isLoadingJobs by mutableStateOf(false)
        private set

    private var onSaveSuccessCallback: (() -> Unit)? = null
    private var onSaveErrorCallback: ((String) -> Unit)? = null

    init {
        loadUserProfile()
        loadMyJobs()
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

    private fun loadMyJobs() {
        viewModelScope.launch {
            isLoadingJobs = true
            try {
                val user = SupabaseClient.client.auth.currentUserOrNull()
                val userId = user?.id

                if (userId != null) {
                    jobRepository.getJobsByClientId(userId)
                        .onSuccess { jobs ->
                            myJobs = jobs
                        }
                        .onFailure {
                            // Manejo de error silencioso
                        }
                }
            } catch (e: Exception) {
                // Manejo de excepción silencioso
            } finally {
                isLoadingJobs = false
            }
        }
    }

    fun toggleEditing() {
        state = state.copy(isEditing = !state.isEditing)
    }

    fun updateProfileField(
        field: String,
        value: Any?
    ) {
        val currentUser = state.user ?: return

        val updatedUser = when (field) {
            "name" -> {
                val stringValue = value as? String ?: currentUser.name
                currentUser.copy(name = stringValue)
            }
            "location" -> {
                val stringValue = value as? String
                currentUser.copy(location = stringValue)
            }
            "experience" -> {
                val stringValue = value as? String
                currentUser.copy(experience = stringValue)
            }
            "bio" -> {
                val stringValue = value as? String
                currentUser.copy(bio = stringValue)
            }
            "skills" -> {
                @Suppress("UNCHECKED_CAST")
                val listValue = value as? List<String>
                currentUser.copy(skills = listValue ?: currentUser.skills)
            }
            else -> currentUser
        }
        state = state.copy(user = updatedUser)
    }

    fun saveProfile() {
        viewModelScope.launch {
            state = state.copy(isSaving = true, error = null)

            val currentUser = state.user ?: run {
                state = state.copy(isSaving = false)
                onSaveErrorCallback?.invoke("Usuario no encontrado")
                return@launch
            }

            val result = updateUserProfileUseCase(
                userId = currentUser.id,
                name = currentUser.name,
                location = currentUser.location,
                experience = currentUser.experience,
                bio = currentUser.bio,
                skills = currentUser.skills,
                profileImage = currentUser.profileImage
            )

            if (result.isSuccess) {
                val refreshedUser = getUserProfileUseCase()
                if (refreshedUser.isSuccess) {
                    state = state.copy(
                        user = refreshedUser.getOrNull(),
                        isEditing = false,
                        isSaving = false
                    )
                } else {
                    state = state.copy(
                        isEditing = false,
                        isSaving = false
                    )
                }
                onSaveSuccessCallback?.invoke()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Error al guardar el perfil"
                state = state.copy(
                    isSaving = false,
                    error = errorMsg
                )
                onSaveErrorCallback?.invoke(errorMsg)
            }
        }
    }

    fun setOnSaveSuccessCallback(callback: () -> Unit) {
        onSaveSuccessCallback = callback
    }

    fun setOnSaveErrorCallback(callback: (String) -> Unit) {
        onSaveErrorCallback = callback
    }

    fun clearError() {
        state = state.copy(error = null)
    }
}