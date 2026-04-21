package com.example.tasky.ui.jobs.detail

import com.example.tasky.domain.model.Job

data class JobDetailState(
    val job: Job? = null,
    val isLoading: Boolean = false,

    val isActionLoading: Boolean = false,

    val isOwner: Boolean = false,
    val isApplied: Boolean = false,

    // UI Feedback
    val showConfirmDialog: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null,
    val isUserLoggedIn: Boolean = true
)