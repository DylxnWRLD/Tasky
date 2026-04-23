package com.example.tasky.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCreateJob: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onJobClick: (String) -> Unit
) {
    val jobs = viewModel.jobs
    val isLoading = viewModel.isLoading

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Ya se está en el menú */ },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToCreateJob,
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Crear") },
                    label = { Text("Crear") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToProfile,
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { paddingValues ->
        // Fondo azulito para igualar el login
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF7B8EDB))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Texto de bienvenida en la parte azul
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 20.dp)
                ) {
                    Text(
                        text = "Hola de nuevo,",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Menú principal",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Tarjeta blanca con los bordes superiores redondeados a 40.dp
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                    color = Color(0xFFF5F5F5)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxSize()
                    ) {
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            placeholder = { Text("Buscar un trabajo") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(30.dp),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                            trailingIcon = { Icon(Icons.Default.FilterList, contentDescription = "Filtros") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val categorias = listOf("Jardinería", "Hogar", "Mecánica", "Carpintería")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categorias) { categoria ->
                                FilterChip(
                                    selected = false,
                                    onClick = { },
                                    label = { Text(categoria) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF7B8EDB))
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(jobs) { job ->
                                    JobCard(
                                        title = job.title,
                                        description = job.description ?: "Sin descripción",
                                        payment = "${job.payment} MXN",
                                        timeAgo = "Reciente",
                                        onClick = { onJobClick(job.id ?: "") }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// Las tarjetas de usuario
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobCard(
    title: String,
    description: String,
    payment: String,
    timeAgo: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Cuadro para la imagen o icono del trabajo
            Surface(
                modifier = Modifier.size(64.dp),
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(12.dp)
            ) { }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodyMedium, maxLines = 2, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Pago: $payment", style = MaterialTheme.typography.labelLarge, color = Color(0xFF7B8EDB))
                    Text("Hace: $timeAgo", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}