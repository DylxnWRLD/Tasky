package com.example.tasky.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.example.tasky.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from

// 1. Se define la estructura exacta que pide la base de datos
@Serializable
data class ReporteDto(
    @SerialName("user_id") val userId: String,
    val descripcion: String
)