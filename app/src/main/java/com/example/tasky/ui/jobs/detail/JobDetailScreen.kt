package com.example.tasky.ui.jobs.detail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

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

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF7B8EDB))) {
        if (state.isLoading && state.job == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
        }

        state.job?.let { job ->
            Column(modifier = Modifier.fillMaxSize()) {

                // --- ENCABEZADO ---
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

                // --- CUERPO PRINCIPAL (TARJETA BLANCA) ---
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                    color = Color(0xFFF5F5F5)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Resumen superior (Imagen + Título + Pago)
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // 1. TARJETA DE DESCRIPCIÓN
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Descripción", fontWeight = FontWeight.Bold, color = Color.Black)
                                Spacer(Modifier.height(8.dp))
                                Text(job.description ?: "Sin descripción.", color = Color.DarkGray, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // --- NUEVA SECCIÓN DE UBICACIÓN CON MAPA ESTÁTICO ---
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color(0xFF7B8EDB),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "Ubicación exacta",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                }

                                Spacer(Modifier.height(12.dp))

                                // Convertir String de Supabase a GeoPoint de Osmdroid
                                val locationPoint = remember(job.locationApprox) {
                                    try {
                                        val coords = job.locationApprox.split(",")
                                        GeoPoint(coords[0].toDouble(), coords[1].toDouble())
                                    } catch (e: Exception) {
                                        GeoPoint(19.5438, -96.9102)
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp), // Altura para que respire la pantalla
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color.LightGray)
                                ) {
                                    AndroidView(
                                        modifier = Modifier.fillMaxSize(),
                                        factory = { ctx ->
                                            MapView(ctx).apply {
                                                setTileSource(TileSourceFactory.MAPNIK)
                                                setMultiTouchControls(false)
                                                setBuiltInZoomControls(false)

                                                controller.setZoom(17.5)
                                                controller.setCenter(locationPoint)

                                                val marker = Marker(this).apply {
                                                    position = locationPoint
                                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                                    title = job.title
                                                }
                                                overlays.add(marker)
                                            }
                                        },
                                        update = { mapView ->
                                            mapView.controller.animateTo(locationPoint)
                                        }
                                    )
                                }
                            }
                        }


                        Spacer(Modifier.height(16.dp))

                        // 3. TARJETA DE FECHA Y HORA
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
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

                        Spacer(modifier = Modifier.height(32.dp))

                        // --- BOTONES DE ACCIÓN ---
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
                                    Text("Borrar", fontWeight = FontWeight.Bold, color = Color.White)
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
                                        else -> Color(0xFF7B8EDB)
                                    }
                                ),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !state.isActionLoading && !state.isLoading
                            ) {
                                if (state.isActionLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text(
                                        text = if (state.isApplied) "Cancelar postulación" else "Postularse al trabajo",
                                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }

        // --- DIÁLOGOS Y NOTIFICACIONES ---
        if (state.showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onDismissDialog() },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmAction() }) {
                        Text("Confirmar", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDismissDialog() }) { Text("Volver") }
                },
                title = { Text("Confirmación") },
                text = {
                    Text(if (state.isApplied) "¿Estás seguro de que quieres cancelar tu postulación?" else "¿Quieres postularte para este trabajo?")
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
                delay(3000)
                viewModel.clearUserMessage()
            }
        }
    }
}