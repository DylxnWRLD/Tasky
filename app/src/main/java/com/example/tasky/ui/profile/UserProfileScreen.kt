package com.example.tasky.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tasky.domain.model.Job
import com.example.tasky.domain.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    state: UserProfileState,
    myJobs: List<Job>,
    isLoadingJobs: Boolean,
    isEditing: Boolean,
    isSaving: Boolean,
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit,
    onJobClick: (String) -> Unit,
    onUpdateField: (String, Any?) -> Unit,
    onSaveProfile: () -> Unit,
    onCancelEdit: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Perfil" else "Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = onEditProfile) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar perfil")
                        }
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
                myJobs = myJobs,
                isLoadingJobs = isLoadingJobs,
                isEditing = isEditing,
                isSaving = isSaving,
                onJobClick = onJobClick,
                onUpdateField = onUpdateField,
                onSaveProfile = onSaveProfile,
                onCancelEdit = onCancelEdit,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun ProfileContent(
    user: User?,
    myJobs: List<Job>,
    isLoadingJobs: Boolean,
    isEditing: Boolean,
    isSaving: Boolean,
    onJobClick: (String) -> Unit,
    onUpdateField: (String, Any?) -> Unit,
    onSaveProfile: () -> Unit,
    onCancelEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF5F5F5))
    ) {
        // Header con foto y nombre
        ProfileHeader(user = user, isEditing = isEditing, onUpdateField = onUpdateField)

        // Rating
        RatingSection(rating = user?.rating ?: 5.0)

        // Información del perfil
        if (isEditing) {
            // Formulario editable
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
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
                        EditableProfileInfoRow(
                            icon = Icons.Default.Email,
                            label = "Correo electrónico",
                            value = user?.email,
                            isEditing = false,
                            onValueChange = {}
                        )

                        HorizontalDivider(color = Color(0xFFE0E0E0))

                        EditableProfileInfoRow(
                            icon = Icons.Default.Person,
                            label = "Nombre",
                            value = user?.name,
                            isEditing = isEditing,
                            onValueChange = { newName ->
                                onUpdateField("name", newName)
                            }
                        )

                        HorizontalDivider(color = Color(0xFFE0E0E0))

                        EditableProfileInfoRow(
                            icon = Icons.Default.LocationOn,
                            label = "Ubicación",
                            value = user?.location,
                            isEditing = isEditing,
                            onValueChange = { newLocation ->
                                onUpdateField("location", newLocation)
                            }
                        )

                        HorizontalDivider(color = Color(0xFFE0E0E0))

                        EditableProfileInfoRow(
                            icon = Icons.Default.WorkHistory,
                            label = "Experiencia",
                            value = user?.experience,
                            isEditing = isEditing,
                            onValueChange = { newExperience ->
                                onUpdateField("experience", newExperience)
                            }
                        )

                        HorizontalDivider(color = Color(0xFFE0E0E0))

                        EditableSkillsSection(
                            skills = user?.skills,
                            isEditing = isEditing,
                            onSkillsChange = { newSkills ->
                                onUpdateField("skills", newSkills)
                            }
                        )

                        HorizontalDivider(color = Color(0xFFE0E0E0))

                        EditableProfileInfoRow(
                            icon = Icons.Default.Info,
                            label = "Biografía",
                            value = user?.bio,
                            isEditing = isEditing,
                            onValueChange = { newBio ->
                                onUpdateField("bio", newBio)
                            },
                            multiLine = true
                        )
                    }
                }

                // Botones de acción
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = onSaveProfile,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B8EDB)),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text("Guardar")
                        }
                    }
                }
            }
        } else {
            // Modo vista normal
            ProfileInfoSection(user = user)
        }

        // Trabajos publicados
        PublishedJobsSection(jobs = myJobs, isLoading = isLoadingJobs, onJobClick = onJobClick)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileHeader(user: User?, isEditing: Boolean, onUpdateField: (String, Any?) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
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

            // Nombre de usuario (editable solo si está en modo edición y no es el rol)
            if (isEditing) {
                OutlinedTextField(
                    value = user?.name ?: "",
                    onValueChange = { newName ->
                        onUpdateField("name", newName)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    placeholder = { Text("Tu nombre") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White
                    ),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    singleLine = true
                )
            } else {
                Text(
                    text = user?.name ?: "Usuario",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

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
private fun EditableProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String?,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    multiLine: Boolean = false
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

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = value ?: "",
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = !multiLine,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7B8EDB),
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            } else {
                Text(
                    text = value?.takeIf { it.isNotBlank() } ?: "No especificado",
                    fontSize = 16.sp,
                    color = if (value.isNullOrBlank()) Color.Gray else Color(0xFF212121),
                    fontStyle = if (value.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal
                )
            }
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
                    fontStyle = FontStyle.Italic
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
private fun EditableSkillsSection(
    skills: List<String>?,
    isEditing: Boolean,
    onSkillsChange: (List<String>) -> Unit
) {
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

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Habilidades",
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (isEditing) {
                var skillsText by remember { mutableStateOf(skills?.joinToString(", ") ?: "") }

                OutlinedTextField(
                    value = skillsText,
                    onValueChange = {
                        skillsText = it
                        onSkillsChange(
                            it.split(",")
                                .map { skill -> skill.trim() }
                                .filter { it.isNotEmpty() }
                        )
                    },
                    placeholder = { Text("Ej: Plomería, Electricidad, Carpintería", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7B8EDB),
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                Text(
                    text = "Separa las habilidades con comas",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                if (skills.isNullOrEmpty()) {
                    Text(
                        text = "Sin habilidades especificadas",
                        fontSize = 14.sp,
                        color = Color(0xFF9E9E9E),
                        fontStyle = FontStyle.Italic
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        skills.chunked(3).forEach { rowSkills ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowSkills.forEach { skill ->
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFFE8EAF6)
                                    ) {
                                        Text(
                                            text = skill,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
                fontStyle = if (bio == null) FontStyle.Italic else FontStyle.Normal,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun PublishedJobsSection(
    jobs: List<Job>,
    isLoading: Boolean,
    onJobClick: (String) -> Unit
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Work, contentDescription = "Trabajos", tint = Color(0xFF7B8EDB), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Trabajos Publicados", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                }
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFE8EAF6)) {
                    // Muestra el número real de trabajos
                    Text(text = "${jobs.size}", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7B8EDB))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lógica perrona para iterar la base de datos
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF7B8EDB))
                }
            } else if (jobs.isEmpty()) {
                Text("Aún no has publicado ni un trabajo.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
            } else {
                jobs.forEachIndexed { index, job ->
                    JobItem(
                        title = job.title,
                        category = job.category,
                        date = "Reciente", // O puedes formatear tu job.scheduledDate si lo tienes
                        status = job.status ?: "Activo",
                        payment = "$${job.payment}",
                        onClick = { onJobClick(job.id ?: "") }
                    )

                    if (index < jobs.size - 1) {
                        HorizontalDivider(
                            color = Color(0xFFF0F0F0),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { /* Funcionalidad futura */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7B8EDB))
            ) {
                Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp))
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
    payment: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable{ onClick() }
            .padding(vertical = 8.dp),
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
    onEditProfile: () -> Unit,
    onJobClick: (String) -> Unit
) {
    val state = viewModel.state

    UserProfileScreen(
        state = state,
        myJobs = viewModel.myJobs,
        isLoadingJobs = viewModel.isLoadingJobs,
        isEditing = state.isEditing,
        isSaving = state.isSaving,
        onNavigateBack = onNavigateBack,
        onEditProfile = onEditProfile,
        onJobClick = onJobClick,
        onUpdateField = { field, value ->
            viewModel.updateProfileField(field, value)
        },
        onSaveProfile = {
            viewModel.saveProfile()
        },
        onCancelEdit = {
            viewModel.toggleEditing()
            viewModel.loadUserProfile()
        }
    )
}