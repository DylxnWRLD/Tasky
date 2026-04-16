package com.example.tasky.ui.jobs.detail

import com.example.tasky.domain.model.Job

data class JobDetailState(
    val job: Job? = null,
    val isLoading: Boolean = false,
    val showConfirmDialog: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null,
    val isUserLoggedIn: Boolean = true
)