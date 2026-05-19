package com.example.tasky.domain.model

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs

@Composable
fun GestorDeSacudidas(onShake: () -> Unit) {
    val contexto = LocalContext.current
    val sensorManager = contexto.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    DisposableEffect(Unit) {
        val acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var ultimaActualizacion: Long = 0
        var ultimoX = 0f
        var ultimoY = 0f
        var ultimoZ = 0f

        val umbralSacudida = 500

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val tiempoActual = System.currentTimeMillis()

                if ((tiempoActual - ultimaActualizacion) > 100) {
                    val tiempoDiferencia = tiempoActual - ultimaActualizacion
                    ultimaActualizacion = tiempoActual

                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val velocidad = abs(x + y + z - ultimoX - ultimoY - ultimoZ) / tiempoDiferencia * 10000

                    if (velocidad > umbralSacudida) {
                        onShake()
                    }

                    ultimoX = x
                    ultimoY = y
                    ultimoZ = z
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, acelerometro, SensorManager.SENSOR_DELAY_NORMAL)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
}