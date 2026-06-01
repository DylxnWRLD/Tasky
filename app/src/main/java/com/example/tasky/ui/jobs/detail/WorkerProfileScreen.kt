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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    var reasonText by remember { mutableStateOf("") }

    LaunchedEffect(workerId) {
        viewModel.loadProfile(workerId, jobId)
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            android.widget.Toast.makeText(
                context,
                message,
                android.widget.Toast.LENGTH_LONG
            ).show()

            viewModel.clearMessages()
            onNavigateBack()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { errorMensaje ->
            if (!state.isCancelled) {
                android.widget.Toast.makeText(
                    context,
                    errorMensaje,
                    android.widget.Toast.LENGTH_LONG
                ).show()

                viewModel.clearMessages()
            }
        }
    }

    if (state.isCancelled) {
        AlertDialog(
            onDismissRequest = onNavigateBack,
            title = {
                Text("Postulación cancelada")
            },
            text = {
                Text(
                    state.error ?: "Este trabajador ha cancelado su postulación"
                )
            },
            confirmButton = {
                TextButton(onClick = onNavigateBack) {
                    Text("Aceptar")
                }
            }
        )
    }

    if (state.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onCancelDialog() },
            title = {
                Text("Confirmación")
            },
            text = {
                Text("¿Seguro que quieres aceptar a este postulante para tu trabajo?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmAcceptance(workerId, jobId)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00D12F)
                    )
                ) {
                    Text("Confirmar", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.onCancelDialog()
                    }
                ) {
                    Text("Cancelar", color = Color.Black)
                }
            }
        )
    }

    if (state.showRejectConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.onCancelRejectDialog()
            },
            title = {
                Text("Confirmar acción")
            },
            text = {
                Text("¿Estás seguro de que quieres rechazar a este postulante para realizar tu trabajo?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onConfirmRejectionClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Confirmar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.onCancelRejectDialog()
                    }
                ) {
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
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = if (state.showReasonScreen) {
                        "Motivo del rechazo:"
                    } else {
                        "Detalles de postulante:"
                    },
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color.White
                )
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF8692E8)
                    )
                }
            } else {
                if (state.showReasonScreen) {
                    ReasonScreenContent(
                        workerName = state.worker?.name,
                        reasonText = reasonText,
                        onReasonChange = { reasonText = it },
                        onSubmit = {
                            viewModel.submitRejection(
                                workerId = workerId,
                                jobId = jobId,
                                reason = reasonText
                            )
                        }
                    )
                } else {
                    state.worker?.let { worker ->
                        WorkerProfileContent(
                            worker = worker,
                            state = state,
                            onRejectClicked = {
                                viewModel.onRejectClicked()
                            },
                            onAcceptClicked = {
                                viewModel.onAcceptClicked()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReasonScreenContent(
    workerName: String?,
    reasonText: String,
    onReasonChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Especifica brevemente las razones por las cuales rechazas la solicitud de ${workerName ?: "este postulante"}.",
            fontSize = 16.sp,
            color = Color.DarkGray,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = reasonText,
            onValueChange = onReasonChange,
            label = {
                Text("Razones del rechazo...")
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSubmit,
            enabled = reasonText.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Confirmar y Enviar",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WorkerProfileContent(
    worker: com.example.tasky.domain.model.User,
    state: WorkerProfileState,
    onRejectClicked: () -> Unit,
    onAcceptClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
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
                    fontSize = 24.sp,
                    color = Color.Black
                )

                Row {
                    repeat(5) { index ->
                        Text(
                            text = if (index < worker.rating.toInt()) "⭐" else "☆ ",
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
            colors = CardDefaults.cardColors(
                containerColor = Color.LightGray
            ),
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                DetailSection(
                    title = "Ubicación:",
                    content = worker.location ?: "Sin ubicación"
                )

                DetailSection(
                    title = "Habilidades:",
                    content = worker.skills?.joinToString(
                        separator = "\n• ",
                        prefix = "• "
                    ) ?: "Sin habilidades"
                )

                DetailSection(
                    title = "Experiencia:",
                    content = worker.experience ?: "Sin experiencia"
                )

                DetailSection(
                    title = "Descripción:",
                    content = worker.bio ?: worker.description ?: "Sin descripción"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        WorkerActionSection(
            state = state,
            onRejectClicked = onRejectClicked,
            onAcceptClicked = onAcceptClicked
        )

        state.error?.let { message ->
            Text(
                text = message,
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WorkerActionSection(
    state: WorkerProfileState,
    onRejectClicked: () -> Unit,
    onAcceptClicked: () -> Unit
) {
    when {
        state.isCurrentWorkerRejected -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                ),
                border = BorderStroke(1.dp, Color(0xFFC62828)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Lo lamentamos",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828),
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "No fuiste seleccionado para realizar este trabajo. ¡Éxito en tus siguientes postulaciones!",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }

        state.isJobClosed && state.isCurrentWorkerAccepted -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                ),
                border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            "Trabajador Seleccionado",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )

                        Text(
                            "Aceptaste a este usuario para realizar el trabajo.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        state.isJobClosed && !state.isCurrentWorkerAccepted -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                ),
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
                    onClick = onRejectClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        "Rechazar",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onAcceptClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00D12F)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text(
                        "Aceptar",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: String
) {
    Text(
        text = title,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        color = Color.Black
    )

    Text(
        text = content,
        fontSize = 16.sp,
        color = Color.Black
    )

    Spacer(modifier = Modifier.height(16.dp))
}