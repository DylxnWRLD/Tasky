package com.example.tasky.ui.jobs.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    onNavigateBack: () -> Unit,
    onApplicantClick: (String) -> Unit
) {
    val state = viewModel.state

    LaunchedEffect(key1 = jobId) {
        viewModel.loadApplicants(jobId)
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF9BA9FF))
                    .padding(top = 40.dp, start = 16.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                ) {
                    Icon(
                        Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White,
                    )                }
                Spacer(Modifier.width(16.dp))
                Text(
                    "Postulantes:",
                    modifier = Modifier.padding(start = 20.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFCED4F5))
                .padding(20.dp)
        ) {
            Text(text = jobTitle, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)

            when {
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

                state.applicants.isEmpty() -> {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Aún no hay postulantes para este trabajo",
                        color = Color.DarkGray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

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
                        items(state.applicants) { user ->
                            Surface(
                                onClick = { onApplicantClick(user.id) },
                                color = Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                ApplicantCard(user)
                            }
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
        color = Color(0xFFD9D9D9),
        border = BorderStroke(2.dp, Color(0xFF7B8EDB))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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