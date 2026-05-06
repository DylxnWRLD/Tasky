package com.example.tasky.ui.create

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import android.view.MotionEvent

@SuppressLint("ClickableViewAccessibility")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateJobScreen(
    viewModel: CreateJobViewModel,
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

    //Estados para la fecha y hora
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

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

                                        setOnTouchListener { view, event ->
                                            when (event.action) {
                                                MotionEvent.ACTION_DOWN -> {
                                                    // Cuando tocas el mapa, bloquea el scroll de la pantalla
                                                    view.parent.requestDisallowInterceptTouchEvent(true)
                                                }
                                                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                                    // Cuando sueltas, regresa todo a la normalidad
                                                    view.parent.requestDisallowInterceptTouchEvent(false)
                                                }
                                            }
                                            false // Se regresa 'false' para que el mapa no pierda el toque y se pueda mover
                                        }

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
                            // Campo de Fecha
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = date,
                                    onValueChange = {},
                                    readOnly = true, // Bloquea el teclado
                                    label = { Text("Fecha") },
                                    placeholder = { Text("YYYY-MM-DD") },
                                    isError = dateTimeError,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Calendario") }
                                )
                                Spacer(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(Color.Transparent)
                                        .clickable { showDatePicker = true }
                                )
                            }

                            // Campo de Hora
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = time,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Hora") },
                                    placeholder = { Text("HH:MM") },
                                    isError = dateTimeError,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = "Reloj") }
                                )
                                Spacer(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(Color.Transparent)
                                        .clickable { showTimePicker = true }
                                )
                            }
                        }
                        if (dateTimeError) {
                            Text("Falta seleccionar la fecha o la hora", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }

                        // 8. Diálogos emergentes (Calendario y Reloj)
                        if (showDatePicker) {
                            val datePickerState = rememberDatePickerState()
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")

                                            date = sdf.format(java.util.Date(millis))
                                            dateTimeError = false
                                        }
                                        showDatePicker = false
                                    }) { Text("Aceptar") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
                                }
                            ) {
                                DatePicker(state = datePickerState)
                            }
                        }

                        if (showTimePicker) {
                            val timePickerState = rememberTimePickerState()
                            AlertDialog(
                                onDismissRequest = { showTimePicker = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        // Formatear la hora con ceros a la izquierda (ej. 09:05 en vez de 9:5)
                                        val h = timePickerState.hour.toString().padStart(2, '0')
                                        val m = timePickerState.minute.toString().padStart(2, '0')
                                        time = "$h:$m"
                                        dateTimeError = false
                                        showTimePicker = false
                                    }) { Text("Aceptar") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
                                },
                                text = {
                                    // Componente nativo del reloj
                                    TimePicker(state = timePickerState)
                                }
                            )
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

                            // Validación perrona con Expresiones Regulares
                            val dateRegex = "^\\d{4}-\\d{2}-\\d{2}\$".toRegex()
                            val timeRegex = "^\\d{2}:\\d{2}(:\\d{2})?\$".toRegex() // Acepta HH:MM y también HH:MM:SS por si acaso

                            dateTimeError = !date.trim().matches(dateRegex) || !time.trim().matches(timeRegex)


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
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        viewModel.errorMessage?.let { errorMsg ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.resetState() }) {
                        Text("OK", color = Color.White)
                    }
                }
            ) {
                Text(errorMsg)
            }
        }
    }
}