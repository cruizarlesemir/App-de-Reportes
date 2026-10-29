package com.capunay.reportesapp.data.model

/**
 * Reporte tal como lo devuelve la API.
 * [fecha] se deja como texto para tolerar los distintos formatos que puede enviar el backend.
 */
data class Reporte(
    val id: Int = 0,
    val dni: String = "",
    val fecha: String? = null,
    val tipo: String = "",
    val direccion: String = "",
    val referencia: String? = null,
    val estado: Int = 1
) {
    val estaAtendido: Boolean
        get() = estado == 0
}

/** Cuerpo que se envía al registrar un reporte (el estado lo inicializa la API en 1). */
data class NuevoReporte(
    val dni: String,
    val fecha: String,
    val tipo: String,
    val direccion: String,
    val referencia: String? = null
)
