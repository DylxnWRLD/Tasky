package com.example.tasky.domain.model

data class Job(
    val id: String,
    val title: String,
    val category: String,
    val payment: Double,
    val description: String,
    val date: String,
    val time: String,
    val imageUrl: String,
    val isApplied: Boolean = false
)