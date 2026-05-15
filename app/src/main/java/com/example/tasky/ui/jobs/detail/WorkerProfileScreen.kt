package com.example.tasky.ui.jobs.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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



@Composable
fun WorkerProfileScreen(
    workerId: String,
    jobId: String,
    viewModel: WorkerProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val state = viewModel.state
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(workerId) {
        viewModel.loadProfile(workerId, jobId)
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
            onNavigateBack()
        }
    }

    if (state.isCancelled) {
        AlertDialog(
            onDismissRequest = onNavigateBack,
            title = { Text("Postulación cancelada") },
            text = { Text("Este usuario ha cancelado su postulación") },
            confirmButton = {
                TextButton(onClick = onNavigateBack) { Text("Aceptar") }
            }
        )
    }

    if (state.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onCancelDialog() },
            title = { Text("Confirmación") },
            text = { Text("¿Seguro que quieres aceptar a este postulante para tu trabajo?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmAcceptance(workerId, jobId) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D12F))
                ) {
                    Text("Confirmar", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onCancelDialog() }) {
                    Text("Cancelar", color = Color.Black)
                }
            }
        )
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFD6D9F5))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF8692E8))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Detalles de postulante:",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color.White
                )
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF8692E8))
                }
            } else {
                state.worker?.let { worker ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(90.dp),
                                shape = CircleShape,
                                border = BorderStroke(2.dp, Color.Black)
                            ) {
                                AsyncImage(
                                    model = worker.profileImage,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = worker.name, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                Row {
                                    repeat(5) { index ->
                                        Text(
                                            text = if (index < worker.rating.toInt()) "⭐" else "☆",
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.LightGray),
                            border = BorderStroke(1.dp, Color.Black)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                DetailSection("Ubicación:", worker.location ?: "Sin ubicación")
                                DetailSection("Habilidades:", worker.skills?.joinToString("\n• ", prefix = "• ") ?: "Sin habilidades")
                                DetailSection("Experiencia:", worker.experience ?: "Sin experiencia")
                                DetailSection("Descripción:", worker.bio ?: worker.description ?: "Sin descripción")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        when {
                            state.isJobClosed && state.isCurrentWorkerAccepted -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                    border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Trabajador Seleccionado", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                            Text("Aceptaste a este usuario para realizar el trabajo.", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }

                            state.isJobClosed && !state.isCurrentWorkerAccepted -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                    border = BorderStroke(1.dp, Color.LightGray),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = "Esta vacante ya fue asignada a otro postulante.",
                                        modifier = Modifier.padding(16.dp),
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            else -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Button(
                                        onClick = { /* Lógica CU-20 Rechazar */ },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                                    ) {
                                        Text("Rechazar", color = Color.White, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel.onAcceptClicked() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D12F)),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                                    ) {
                                        Text("Aceptar", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        state.error?.let {
                            Text(
                                text = it,
                                color = Color.Red,
                                modifier = Modifier.padding(top = 16.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSection(title: String, content: String) {
    Text(text = title, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
    Text(text = content, fontSize = 16.sp)
    Spacer(modifier = Modifier.height(16.dp))
}