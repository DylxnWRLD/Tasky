package com.example.tasky.ui.map

import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource

/**
 * Tile source de OpenStreetMap usada por las pantallas de Tasky.
 *
 * No se usa [org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK] porque ese
 * tile source incluye el flag [TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED]. Con ese flag
 * osmdroid ignora `Configuration.userAgentValue` y envía como User-Agent
 * "<package>/<versionCode>" (por ejemplo "com.example.tasky/1"). OpenStreetMap bloquea
 * ese User-Agent porque no identifica a la app ni da un medio de contacto, y responde con
 * la imagen "Access blocked / App is not following the tile usage policy".
 *
 * Al definir el tile source sin ese flag, se envía el User-Agent configurado en
 * [com.example.tasky.TaskyApp]: "Tasky/<version> (contacto: ...)", que sí cumple la
 * política: https://operations.osmfoundation.org/policies/tiles/
 *
 * El nombre ("TaskyOSM") es distinto a propósito: osmdroid usa el nombre como parte de la
 * clave de caché, así que se descartan los tiles de bloqueo que versiones anteriores
 * pudieron haber guardado en disco bajo el nombre "Mapnik".
 */
val TaskyOsmTileSource: XYTileSource = XYTileSource(
    "TaskyOSM",
    0,
    19,
    256,
    ".png",
    arrayOf("https://tile.openstreetmap.org/"),
    "© OpenStreetMap contributors",
    TileSourcePolicy(
        2, // máximo de descargas simultáneas permitido por la política de OSM
        TileSourcePolicy.FLAG_NO_BULK or
            TileSourcePolicy.FLAG_NO_PREVENTIVE or
            TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
    )
)
