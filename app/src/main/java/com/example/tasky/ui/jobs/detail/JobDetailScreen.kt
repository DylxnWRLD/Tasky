package com.example.tasky.ui.jobs.detail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage

@Composable
fun JobDetailScreen(
    jobId: String,
    viewModel: JobDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val state = viewModel.state

    LaunchedEffect(key1 = jobId) {
        viewModel.loadJobById(jobId)
    }

    // Fondo azulito consistente con el login y Home
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF7B8EDB))) {
        if (state.isLoading && state.job == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
        }

        state.job?.let { job ->
            Column(modifier = Modifier.fillMaxSize()) {

                // Encabezado con el botón de retroceso igual que en CreateJob
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 16.dp, end = 24.dp, bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                    Text(
                        text = if (state.isOwner) "Detalles de mi Tarea" else "Detalles del Trabajo",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Tarjeta principal con el borde curvo a 40.dp
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                    color = Color(0xFFF5F5F5) // Mismo color hueso/gris claro de las otras vistas
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            // Cuadro para la imagen (o imagen de Coil si viene de red)
                            Surface(
                                modifier = Modifier.size(80.dp),
                                color = Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                AsyncImage(
                                    model = job.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(job.title, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.Black)
                                Spacer(Modifier.height(4.dp))
                                Text("Categoría: ${job.category}", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                                Text("Pago: $${job.payment} MXN", color = Color(0xFF7B8EDB), fontWeight = FontWeight.Bold)
                                Text("Ubicación: ${job.locationApprox}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Tarjeta de descripción con el mismo redondeo de 16.dp
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Descripción", fontWeight = FontWeight.Bold, color = Color.Black)
                                Spacer(Modifier.height(8.dp))
                                Text(job.description ?: "Sin descripción.", color = Color.DarkGray, style = MaterialTheme.typography.bodyMedium)

                                Spacer(Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Fecha", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                                        Text(job.date, color = Color.Black)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Hora", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                                        Text(job.time, color = Color.Black)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Botones con las formas consistentes
                        if (state.isOwner) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(
                                    onClick = { /* Navegar a editar */ },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Color.Gray)
                                ) {
                                    Text("Editar", fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                                Button(
                                    onClick = { /* Borrar */ },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Borrar", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.onMainActionClick() },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when {
                                        state.isLoading -> Color.LightGray
                                        state.isApplied -> Color.Red
                                        else -> Color(0xFF7B8EDB) // Mismo azul del encabezado
                                    }
                                ),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !state.isActionLoading && !state.isLoading
                            ) {
                                if (state.isActionLoading || (state.isLoading && state.job != null)) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text(
                                        text = if (state.isApplied) "Cancelar postulación" else "Postularse al trabajo",
                                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onDismissDialog() },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmAction() }) {
                        Text("Confirmar", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDismissDialog() }) { Text("Cancelar") }
                },
                title = { Text("Confirmación") },
                text = {
                    Text(if (state.isApplied) "¿Estás seguro de que quieres cancelar?" else "¿Quieres postularte?")
                }
            )
        }

        state.userMessage?.let { message ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearUserMessage() }) { Text("OK", color = Color.White) }
                }
            ) { Text(message) }
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearUserMessage()
            }
        }
    }
}