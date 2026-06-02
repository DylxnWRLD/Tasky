package com.example.tasky.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tasky.domain.model.Job

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserJobsScreen(
    viewModel: UserJobsViewModel,
    onNavigateBack: () -> Unit,
    onJobClick: (String) -> Unit
) {
    val jobs = viewModel.jobs
    val isLoading = viewModel.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Trabajos Publicados") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF7B8EDB),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF7B8EDB))
            }
        } else if (jobs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No tienes trabajos publicados.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(jobs) { job ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onJobClick(job.id ?: "") },
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 2.dp,
                        color = Color.White
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = Color(0xFFE8EAF6)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF7B8EDB)) }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = job.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "${job.category} • Reciente", fontSize = 14.sp, color = Color.Gray)
                            }
                            Text(text = "$${job.payment}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        }
                    }
                }
            }
        }
    }
}