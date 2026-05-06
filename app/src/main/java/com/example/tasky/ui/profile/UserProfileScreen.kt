package com.example.tasky.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tasky.domain.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    state: UserProfileState,
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar perfil")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF7B8EDB),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF7B8EDB))
            }
        } else if (state.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.error ?: "Error desconocido",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { /* Recargar */ }) {
                        Text("Reintentar")
                    }
                }
            }
        } else {
            ProfileContent(
                user = state.user,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun ProfileContent(
    user: User?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF5F5F5))
    ) {
        // Header con foto y nombre
        ProfileHeader(user = user)

        // Rating
        RatingSection(rating = user?.rating ?: 5.0)

        // Información del perfil
        ProfileInfoSection(user = user)

        // Trabajos publicados
        PublishedJobsSection()

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileHeader(user: User?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF7B8EDB),
                        Color(0xFF9FA8DA)
                    )
                )
            )
            .padding(top = 32.dp, bottom = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Foto de perfil (placeholder)
            Surface(
                modifier = Modifier
                    .size(100.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.size(60.dp),
                        tint = Color(0xFF7B8EDB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nombre de usuario
            Text(
                text = user?.name ?: "Usuario",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Rol
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Text(
                    text = when (user?.role) {
                        "WORKER" -> "Trabajador"
                        "CLIENT" -> "Cliente"
                        else -> user?.role ?: "Sin rol"
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun RatingSection(rating: Double) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 5 estrellas siempre completas por defecto
            repeat(5) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Estrella",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = String.format("%.1f", rating),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )
        }
    }
}

@Composable
private fun ProfileInfoSection(user: User?) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Correo electrónico
            ProfileInfoRow(
                icon = Icons.Default.Email,
                label = "Correo electrónico",
                value = user?.email ?: "No especificado"
            )

            HorizontalDivider(color = Color(0xFFE0E0E0))

            // Ubicación
            ProfileInfoRow(
                icon = Icons.Default.LocationOn,
                label = "Ubicación",
                value = user?.location ?: "No especificado"
            )

            HorizontalDivider(color = Color(0xFFE0E0E0))

            // Experiencia
            ProfileInfoRow(
                icon = Icons.Default.WorkHistory,
                label = "Experiencia",
                value = user?.experience ?: "Sin especificar"
            )

            HorizontalDivider(color = Color(0xFFE0E0E0))

            // Habilidades
            SkillsSection(
                skills = user?.skills
            )

            HorizontalDivider(color = Color(0xFFE0E0E0))

            // Biografía
            BioSection(
                bio = user?.bio
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = Color(0xFF7B8EDB),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                color = Color(0xFF212121),
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun SkillsSection(skills: List<String>?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Default.Build,
            contentDescription = "Habilidades",
            tint = Color(0xFF7B8EDB),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "Habilidades",
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (skills.isNullOrEmpty()) {
                Text(
                    text = "Sin habilidades especificadas",
                    fontSize = 14.sp,
                    color = Color(0xFF9E9E9E),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            } else {
                // Mostrar chips de habilidades
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    skills.chunked(3).forEach { rowSkills ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowSkills.forEach { skill ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFFE8EAF6)
                                ) {
                                    Text(
                                        text = skill,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        ),
                                        fontSize = 13.sp,
                                        color = Color(0xFF3949AB)
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

@Composable
private fun BioSection(bio: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = "Biografía",
            tint = Color(0xFF7B8EDB),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "Biografía",
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = bio ?: "Sin biografía",
                fontSize = 14.sp,
                color = if (bio == null) Color(0xFF9E9E9E) else Color(0xFF424242),
                fontStyle = if (bio == null) androidx.compose.ui.text.font.FontStyle.Italic
                else androidx.compose.ui.text.font.FontStyle.Normal,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun PublishedJobsSection() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Título de la sección
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Work,
                        contentDescription = "Trabajos",
                        tint = Color(0xFF7B8EDB),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Trabajos Publicados",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                }

                // Contador de trabajos
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8EAF6)
                ) {
                    Text(
                        text = "3",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7B8EDB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de trabajos de ejemplo (solo visual)
            JobItem(
                title = "Reparación de tubería",
                category = "Plomería",
                date = "15 May 2024",
                status = "Activo",
                payment = "$500"
            )

            HorizontalDivider(
                color = Color(0xFFF0F0F0),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            JobItem(
                title = "Pintar sala de estar",
                category = "Pintura",
                date = "10 May 2024",
                status = "Completado",
                payment = "$800"
            )

            HorizontalDivider(
                color = Color(0xFFF0F0F0),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            JobItem(
                title = "Jardinería general",
                category = "Jardinería",
                date = "5 May 2024",
                status = "En progreso",
                payment = "$350"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Botón "Ver todos"
            OutlinedButton(
                onClick = { /* Funcionalidad futura */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF7B8EDB)
                )
            ) {
                Icon(
                    Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver todos los trabajos")
            }
        }
    }
}

@Composable
private fun JobItem(
    title: String,
    category: String,
    date: String,
    status: String,
    payment: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono del trabajo
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFE8EAF6)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Build,
                    contentDescription = null,
                    tint = Color(0xFF7B8EDB),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Información del trabajo
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category,
                    fontSize = 12.sp,
                    color = Color(0xFF7B8EDB)
                )
                Text(
                    text = " • ",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = date,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // Badge de estado y pago
        Column(
            horizontalAlignment = Alignment.End
        ) {
            // Badge de estado
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (status) {
                    "Activo" -> Color(0xFFE8F5E9)
                    "Completado" -> Color(0xFFE3F2FD)
                    "En progreso" -> Color(0xFFFFF3E0)
                    else -> Color(0xFFF5F5F5)
                }
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (status) {
                        "Activo" -> Color(0xFF2E7D32)
                        "Completado" -> Color(0xFF1565C0)
                        "En progreso" -> Color(0xFFE65100)
                        else -> Color(0xFF616161)
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Pago
            Text(
                text = payment,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
fun UserProfileRoute(
    viewModel: UserProfileViewModel,
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit
) {
    val state = viewModel.state

    UserProfileScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onEditProfile = onEditProfile
    )
}

@Preview(showBackground = true)
@Composable
fun UserProfilePreview() {
    MaterialTheme {
        UserProfileScreen(
            state = UserProfileState(
                user = User(
                    id = "123",
                    email = "juan@example.com",
                    name = "Juan Pérez",
                    role = "WORKER",
                    location = "Ciudad de México",
                    experience = "3 años en desarrollo Android",
                    skills = listOf("Kotlin", "Jetpack Compose", "Firebase"),
                    bio = "Desarrollador apasionado por crear apps increíbles"
                ),
                isLoading = false,
                error = null
            ),
            onNavigateBack = {},
            onEditProfile = {}
        )
    }
}