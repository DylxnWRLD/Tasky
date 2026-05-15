package com.example.tasky.ui.jobs.detail

import com.example.tasky.domain.model.JobApplicant

data class JobApplicantsState(
    val applicants: List<JobApplicant> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)