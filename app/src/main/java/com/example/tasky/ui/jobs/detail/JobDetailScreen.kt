package com.example.tasky.ui.jobs.detail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    viewModel: JobDetailViewModel
) {
    val state = viewModel.state
    val job = state.job ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF9494FF))
    ) {
        Column {
            Text(
                text = "Detalles del Trabajo",
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                            Text("Pago: ${job.payment} MXN", color = Color.Gray)
                            Text("Publicado hace: ${job.publishedAgo}", color = Color.Gray)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Surface(
                        color = Color(0xFFF0F0F0),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Descripción:", fontWeight = FontWeight.Bold)
                            Text(job.description, color = Color.DarkGray)
                            Spacer(Modifier.height(12.dp))
                            Text("Fecha:", fontWeight = FontWeight.Bold)
                            Text(job.date)
                            Spacer(Modifier.height(4.dp))
                            Text("Hora: ${job.time}", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { viewModel.onApplyClick() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (job.isApplied) Color.Red else Color(0xFF9494FF)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (job.isApplied) "Cancelar solicitud" else "Postularse para este trabajo",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        if (state.showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onDismissDialog() },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmApplication(job.id) }) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDismissDialog() }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("Confirmación") },
                text = { Text("¿Estás seguro de que quieres postularte para este trabajo?") }
            )
        }

        state.userMessage?.let { /* Mostrar Toast o Snackbar de éxito */ }
        state.errorMessage?.let { /* Mostrar Error (Ex-01) */ }
    }
}