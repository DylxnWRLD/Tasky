package com.example.tasky.ui.jobs.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
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

@Composable
fun WorkerProfileScreen(
    workerId: String,
    jobId: String,
    viewModel: WorkerProfileViewModel,
    onNavigateBack: () -> Unit
) {

    val state = viewModel.state

    LaunchedEffect(workerId) {
        viewModel.loadProfile(workerId, jobId)
    }

    if (state.isCancelled) {

        AlertDialog(
            onDismissRequest = onNavigateBack,
            title = {
                Text("Postulación cancelada")
            },
            text = {
                Text("Este usuario ha cancelado su postulación")
            },
            confirmButton = {
                TextButton(
                    onClick = onNavigateBack
                ) {
                    Text("Aceptar")
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

            // HEADER
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
                    modifier = Modifier.padding(start = 10.dp),
                    text = "Detalles de postulante:",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = Color.White
                )
            }

            // Carga
            if (state.isLoading) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

            } else {

                state.worker?.let { worker ->

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {

                        // Foto de perfil y el nombre del trabajador
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Surface(
                                modifier = Modifier.size(90.dp),
                                shape = CircleShape,
                                border = BorderStroke(2.dp, Color.Black)
                            ) {

                                AsyncImage(
                                    model = worker.profileImage,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {

                                Text(
                                    text = worker.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )

                                Row {
                                    repeat(5) { index ->
                                        Text(
                                            text = if (index < worker.rating.toInt()) {
                                                "⭐"
                                            } else {
                                                "☆"
                                            },
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // TARJETA PRINCIPAL
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),

                            colors = CardDefaults.cardColors(
                                containerColor = Color.LightGray
                            ),

                            border = BorderStroke(
                                1.dp,
                                Color.Black
                            )
                        ) {

                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {

                                // UBICACION
                                Text(
                                    text = "Ubicacion:",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                )

                                Text(
                                    text = worker.location
                                        ?: "Sin ubicacion",
                                    fontSize = 16.sp
                                )

                                Spacer(
                                    modifier = Modifier.height(20.dp)
                                )

                                // HABILIDADES
                                Text(
                                    text = "Habilidades:",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                )

                                if (
                                    worker.skills.isNullOrEmpty()
                                ) {

                                    Text(
                                        text = "Sin habilidades registradas",
                                        fontSize = 16.sp
                                    )

                                } else {

                                    worker.skills.forEach { skill ->

                                        Text(
                                            text = "• $skill",
                                            fontSize = 16.sp
                                        )
                                    }
                                }

                                Spacer(
                                    modifier = Modifier.height(20.dp)
                                )

                                // EXPERIENCIA
                                Text(
                                    text = "Experiencia:",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                )

                                Text(
                                    text = worker.experience
                                        ?: "Sin experiencia registrada",
                                    fontSize = 16.sp
                                )

                                Spacer(
                                    modifier = Modifier.height(20.dp)
                                )

                                // DESCRIPCION
                                Text(
                                    text = "Descripción:",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                )

                                Text(
                                    text = worker.bio
                                        ?: worker.description
                                        ?: "Sin descripción",
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(24.dp)
                        )

                        // Botones
                        Row(
                            modifier = Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.SpaceEvenly
                        ) {

                            Button(
                                onClick = { },

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red
                                ),

                                shape = RoundedCornerShape(16.dp)
                            ) {

                                Text(
                                    text = "Rechazar",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { },

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00D12F)
                                ),

                                shape = RoundedCornerShape(16.dp)
                            ) {

                                Text(
                                    text = "Aceptar",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Mensaje de error
                        state.error?.let {

                            Spacer(
                                modifier = Modifier.height(16.dp)
                            )

                            Text(
                                text = it,
                                color = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
}