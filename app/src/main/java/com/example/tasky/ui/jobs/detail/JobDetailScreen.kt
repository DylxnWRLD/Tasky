package com.example.tasky.ui.jobs.detail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage

@Composable
fun JobDetailScreen(
    jobId: String,
    viewModel: JobDetailViewModel
) {
    val state = viewModel.state

    LaunchedEffect(key1 = jobId) {
        viewModel.loadJobById(jobId)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF9494FF))) {
        if (state.isLoading && state.job == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
        }

        state.job?.let { job ->
            Column {
                Text(
                    text = if (state.isOwner) "Detalles de mi Tarea" else "Detalles del Trabajo",
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = job.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.size(100.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(job.title, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                Text("Categoría: ${job.category}", color = Color.Gray)
                                Text("Pago: $${job.payment} MXN", color = Color.Gray)
                                Text("Ubicación: ${job.locationApprox}", color = Color.Gray)
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Surface(color = Color(0xFFF0F0F0), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Descripción:", fontWeight = FontWeight.Bold)
                                Text(job.description, color = Color.DarkGray)
                                Spacer(Modifier.height(12.dp))
                                Text("Fecha de realización:", fontWeight = FontWeight.Bold); Text(job.date)
                                Text("Hora: ${job.time}", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        if (state.isOwner) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { /* Navegar a editar */ },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Text("Editar", fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { /* Borrar */ },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    shape = RoundedCornerShape(28.dp)
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
                                        state.isLoading -> Color.LightGray // Estado neutro cargando
                                        state.isApplied -> Color.Red
                                        else -> Color(0xFF9494FF)
                                    }
                                ),
                                shape = RoundedCornerShape(28.dp),
                                enabled = !state.isActionLoading && !state.isLoading
                            ) {
                                if (state.isActionLoading || (state.isLoading && state.job != null)) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text(
                                        text = if (state.isApplied) "Cancelar postulación" else "Postularse al trabajo",
                                        fontSize = 16.sp, fontWeight = FontWeight.Bold
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