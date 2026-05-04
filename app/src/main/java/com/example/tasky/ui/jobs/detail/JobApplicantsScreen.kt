package com.example.tasky.ui.jobs.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Importación necesaria para el LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tasky.domain.model.User

@Composable
fun JobApplicantsScreen(
    jobId: String,
    jobTitle: String,
    viewModel: JobApplicantsViewModel,
    onNavigateBack: () -> Unit
) {
    val state = viewModel.state

    LaunchedEffect(key1 = jobId) {
        viewModel.loadApplicants(jobId)
    }

    Scaffold(
        topBar = {
            // Header basado en image_c6b4bc.png
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF9BA9FF)) // Color azul claro del encabezado
                    .padding(top = 40.dp, start = 16.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.background(Color.White, CircleShape).size(36.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.Black)
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    "Postulantes:",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFCED4F5)) // Fondo lila suave
                .padding(20.dp)
        ) {
            // Información del trabajo actual
            Text(text = jobTitle, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)

            when {
                // EX-01: Fallo de consulta
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF7B8EDB))
                    }
                }

                state.errorMessage != null -> {
                    Text(
                        text = state.errorMessage!!,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                // FA-01: Sin postulantes
                state.applicants.isEmpty() -> {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Aún no hay postulantes para este trabajo",
                        color = Color.DarkGray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // FLUJO NORMAL: Mostrar lista
                else -> {
                    Text(
                        text = "${state.applicants.size} postulantes para este trabajo",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(20.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Corregido: items(lista) { item -> ... }
                        items(state.applicants) { user ->
                            ApplicantCard(user)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicantCard(user: User) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFD9D9D9), // Color gris de la tarjeta
        border = BorderStroke(2.dp, Color(0xFF7B8EDB)) // Borde azul resaltado
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen circular con borde
            Surface(
                modifier = Modifier.size(70.dp),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color.Black)
            ) {
                AsyncImage(
                    model = user.profileImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text = user.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )

                // Estrellas basadas en el rating (image_c6b4bc.png)
                Row {
                    repeat(5) { index ->
                        Text(
                            text = if (index < user.rating.toInt()) "⭐" else "☆",
                            fontSize = 14.sp
                        )
                    }
                }

                Text(
                    text = user.description ?: "Sin descripción",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}