package com.example.tasky.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasky.domain.model.Job
import com.example.tasky.domain.repository.JobRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: JobRepository) : ViewModel() {
    var jobs by mutableStateOf<List<Job>>(emptyList())
    var isLoading by mutableStateOf(false)

    init {
        fetchJobs()
    }

    fun fetchJobs() {
        viewModelScope.launch {
            isLoading = true
            repository.getJobs().onSuccess {
                jobs = it
            }
            isLoading = false
        }
    }
}