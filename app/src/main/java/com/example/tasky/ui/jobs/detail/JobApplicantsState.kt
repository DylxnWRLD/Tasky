package com.example.tasky.ui.jobs.detail


import com.example.tasky.domain.model.User

data class JobApplicantsState(
    val applicants: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)