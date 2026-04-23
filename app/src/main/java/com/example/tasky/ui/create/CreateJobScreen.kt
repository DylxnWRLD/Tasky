package com.example.tasky.ui.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateJobScreen(
    onNavigateBack: () -> Unit,
    onJobCreated: (imageUri: Uri?, location: GeoPoint, title: String, category: String, payment: Double, description: String, date: String, time: String) -> Unit
) {
    val context = LocalContext.current

    // Estados de los campos (Esquema de BD)
    var title by remember { mutableStateOf("") }
    var payment by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    // Estados de error para las validaciones
    var titleError by remember { mutableStateOf(false) }
    var paymentError by remember { mutableStateOf(false) }
    var descError by remember { mutableStateOf(false) }
    var dateTimeError by remember { mutableStateOf(false) }

    // Estado para Categoría (Combo Box)
    var expanded by remember { mutableStateOf(false) }
    val categorias = listOf("Jardinería", "Limpieza", "Carpintería", "Plomería", "Mecánica", "Electricidad", "Hogar")
    var selectedCategory by remember { mutableStateOf(categorias[0]) }

    // Estados de Imagen y Mapa
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedLocation by remember { mutableStateOf(GeoPoint(19.5438, -96.9102)) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF7B8EDB))) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Encabezado estático
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 16.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                }
                Text("Publicar Trabajo", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {

                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Detalles de la chamba", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                        // 1. Imagen (Miniatura)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                                .clickable { galleryLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageUri != null) {
                                AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.Gray)
                                    Text("Añadir miniatura", color = Color.Gray)
                                }
                            }
                        }

                        // 2. Título
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it; titleError = false },
                            label = { Text("¿Qué hay que hacer?") },
                            isError = titleError,
                            supportingText = { if (titleError) Text("Ponle un título") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        // 3. Categoría (Combo Box / Dropdown)
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Categoría") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                categorias.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item) },
                                        onClick = { selectedCategory = item; expanded = false }
                                    )
                                }
                            }
                        }

                        // 4. Pago
                        OutlinedTextField(
                            value = payment,
                            onValueChange = { payment = it },
                            label = { Text("Pago estimado (MXN)") },
                            isError = paymentError,
                            supportingText = { if (paymentError) Text("Pon una cantidad válida") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            prefix = { Text("$ ") }
                        )

                        // 5. Mapa (Ubicación exacta)
                        Text("Selecciona la ubicación", fontWeight = FontWeight.Bold)
                        Surface(modifier = Modifier.fillMaxWidth().height(250.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color.LightGray)) {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { ctx ->
                                    Configuration.getInstance().userAgentValue = ctx.packageName
                                    MapView(ctx).apply {
                                        setTileSource(TileSourceFactory.MAPNIK)
                                        setMultiTouchControls(true)
                                        controller.setZoom(16.0)
                                        controller.setCenter(selectedLocation)
                                        val marker = Marker(this).apply {
                                            position = selectedLocation
                                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        }
                                        overlays.add(marker)
                                        overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                                            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                                p?.let { selectedLocation = it; marker.position = it; invalidate() }
                                                return true
                                            }
                                            override fun longPressHelper(p: GeoPoint?): Boolean = false
                                        }))
                                    }
                                }
                            )
                        }

                        // 6. Descripción
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it; descError = false },
                            label = { Text("Descripción detallada") },
                            isError = descError,
                            supportingText = { if (descError) Text("Escribe de qué trata la chamba") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            minLines = 3
                        )

                        // 7. Fecha y Hora
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = date,
                                onValueChange = { date = it; dateTimeError = false },
                                label = { Text("Fecha") },
                                isError = dateTimeError,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            OutlinedTextField(
                                value = time,
                                onValueChange = { time = it; dateTimeError = false },
                                label = { Text("Hora") },
                                isError = dateTimeError,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                        if (dateTimeError) {
                            Text("Falta la fecha o la hora", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // BOTÓN ESTÁTICO (Fuera del scroll)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            // Checar que no vengan vacíos ni con formatos pendejos
                            val pagoValidado = payment.toDoubleOrNull()

                            titleError = title.trim().isEmpty()
                            paymentError = pagoValidado == null || pagoValidado <= 0.0
                            descError = description.trim().isEmpty()
                            dateTimeError = date.trim().isEmpty() || time.trim().isEmpty()

                            // Si todo está al cien, se lanza el evento
                            if (!titleError && !paymentError && !descError && !dateTimeError) {
                                onJobCreated(imageUri, selectedLocation, title, selectedCategory, pagoValidado!!, description, date, time)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B8EDB))
                    ) {
                        Text("Publicar ahora", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}