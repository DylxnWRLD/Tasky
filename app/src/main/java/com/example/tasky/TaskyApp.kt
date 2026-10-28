package com.example.tasky

import android.app.Application
import android.content.Context
import org.osmdroid.config.Configuration

/**
 * Inicialización global de osmdroid.
 *
 * OpenStreetMap bloquea las peticiones de tiles cuando el User-Agent no identifica
 * claramente a la aplicación (es el mensaje "Access blocked / App is not following
 * the tile usage policy" que aparece dibujado sobre el mapa).
 *
 * Política de uso: https://operations.osmfoundation.org/policies/tiles/
 * Motivo del bloqueo: https://wiki.openstreetmap.org/wiki/Blocked
 */
class TaskyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initOsmdroid()
    }

    private fun initOsmdroid() {
        val prefs = getSharedPreferences(OSMDROID_PREFS, Context.MODE_PRIVATE)
        Configuration.getInstance().apply {
            // Carga rutas de caché, hilos de descarga, etc. (valores por defecto).
            load(this@TaskyApp, prefs)
            // User-Agent propio: nombre/versión de la app + dato de contacto.
            // TODO: cambia el correo por uno real del equipo; OSM lo pide para
            // poder contactarte si hubiera algún problema con el uso de los tiles.
            userAgentValue = OSMDROID_USER_AGENT
        }
    }

    private companion object {
        const val OSMDROID_PREFS = "osmdroid"
        // OSM pide un User-Agent único que identifique la app y un medio de contacto.
        // TODO: cambia el correo por uno real del equipo (y, si publicas la app,
        // el nombre/versión que corresponda).
        val OSMDROID_USER_AGENT =
            "Tasky/${BuildConfig.VERSION_NAME} (contacto: tasky.mx@gmail.com)"
    }
}
