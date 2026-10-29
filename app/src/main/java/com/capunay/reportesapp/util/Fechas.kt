package com.capunay.reportesapp.util

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Fechas {

    private val patronesEntrada = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss.SSS",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    )

    /** Formatea la fecha que devuelve la API sin importar el formato exacto. */
    fun formatear(raw: String?): String {
        if (raw.isNullOrBlank()) return "—"

        for (patron in patronesEntrada) {
            val formato = SimpleDateFormat(patron, Locale.US).apply { isLenient = false }
            val posicion = ParsePosition(0)
            val fecha: Date? = formato.parse(raw, posicion)
            if (fecha != null && posicion.index == raw.length) {
                val patronSalida = if (patron == "yyyy-MM-dd") "dd/MM/yyyy" else "dd/MM/yyyy HH:mm"
                val formatoSalida = SimpleDateFormat(patronSalida, Locale("es", "PE"))
                // Si la API envía zona horaria, mostramos la hora tal como la envía (en UTC).
                if (patron.contains("X")) {
                    formatoSalida.timeZone = TimeZone.getTimeZone("UTC")
                }
                return formatoSalida.format(fecha)
            }
        }
        return raw
    }

    /** Fecha/hora actual en formato ISO local, que Spring Boot acepta en LocalDateTime. */
    fun ahoraIso(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())

    /** Convierte la fecha ISO al formato "yyyy-MM-dd HH:mm:ss" (por si el backend lo exige). */
    fun aFormatoApi(fecha: String): String {
        val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val valor = runCatching { iso.parse(fecha) }.getOrNull() ?: return fecha
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(valor)
    }
}
