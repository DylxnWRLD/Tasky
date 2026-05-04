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

    // Lista oculta para no perder los datos originales al filtrar
    private var allJobs = emptyList<Job>()

    // Lo que la vista va a dibujar
    var jobs by mutableStateOf<List<Job>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var selectedCategory by mutableStateOf("Todas")
        private set

    init {
        loadJobs()
    }

    private fun loadJobs() {
        viewModelScope.launch {
            isLoading = true
            repository.getJobs()
                .onSuccess {
                    allJobs = it
                    aplicarFiltro()
                }
                .onFailure { error = it.message }
            isLoading = false
        }
    }

    fun setCategory(categoria: String) {
        selectedCategory = categoria
        aplicarFiltro()
    }

    private fun aplicarFiltro() {
        jobs = if (selectedCategory == "Todas") {
            allJobs
        } else {
            allJobs.filter { it.category == selectedCategory }
        }
    }
}