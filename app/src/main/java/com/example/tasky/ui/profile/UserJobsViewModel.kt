package com.example.tasky.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasky.data.remote.SupabaseClient
import com.example.tasky.domain.model.Job
import com.example.tasky.domain.repository.JobRepository
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class UserJobsViewModel(private val repository: JobRepository) : ViewModel() {
    var jobs by mutableStateOf<List<Job>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set

    init {
        fetchJobs()
    }

    private fun fetchJobs() {
        viewModelScope.launch {
            isLoading = true
            val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
            if (userId != null) {
                repository.getJobsByClientId(userId).onSuccess {
                    jobs = it
                }
            }
            isLoading = false
        }
    }
}