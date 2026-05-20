package com.example.tasky.ui.profile

import com.example.tasky.domain.model.User

data class UserProfileState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false
)