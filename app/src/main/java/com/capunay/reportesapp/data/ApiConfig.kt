package com.capunay.reportesapp.data

/**
 * Configuración de la API.
 * La URL ya apunta al servicio publicado en Render.
 */
object ApiConfig {

    // ======================= URL BASE =======================
    // Producción (Render). Termina SIEMPRE en "/".
    const val BASE_URL_RENDER = "https://reporteservice-9fdr.onrender.com/"

    // API local desde el EMULADOR de Android Studio.
    const val BASE_URL_EMULADOR = "http://10.0.2.2:8080/"

    // API local desde un CELULAR FÍSICO (misma red WiFi; IP de tu PC).
    const val BASE_URL_CELULAR = "http://192.168.1.20:8080/"

    /** <<< CAMBIA SOLO ESTA LÍNEA si quieres usar la API local >>> */
    const val BASE_URL = BASE_URL_RENDER
    // ========================================================

    // Rutas reales del servicio.
    const val RUTA_LISTA = "reportes/"                      // GET ?offset=&limit=  |  POST
    const val RUTA_DETALLE = "reportes/{id}"                // GET  |  PUT (atender)
    const val RUTA_CIUDADANO = "reportes/ciudadano/{dni}"   // GET (búsqueda por DNI)

    /** Cantidad de reportes que se piden por página (el enunciado pide 5). */
    const val TAMANO_PAGINA = 5

    /** Primer offset de la paginación (0 = desde el inicio). */
    const val OFFSET_INICIAL = 0

    /** true cuando todavía no se reemplazó la URL de ejemplo. */
    val sinConfigurar: Boolean
        get() = BASE_URL.contains("TU-APP")
}
