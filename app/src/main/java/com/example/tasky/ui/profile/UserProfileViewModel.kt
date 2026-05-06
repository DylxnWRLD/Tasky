package com.example.tasky.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasky.domain.usecase.GetUserProfileUseCase
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase
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

    fun clearError() {
        state = state.copy(error = null)
    }
}