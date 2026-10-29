package com.capunay.reportesapp.data.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Página de reportes independiente del formato del backend.
 *
 * Soporta:
 *  - Spring Data: { "content": [...], "totalPages": 3, "last": false, ... }
 *  - API propia:  { "data": [...] } / { "reportes": [...] } / { "items": [...] }
 *  - Array simple: [ {...}, {...} ]
 */
data class ReportesPage(
    val items: List<Reporte> = emptyList(),
    val totalPages: Int? = null,
    val last: Boolean? = null,
    val hasNext: Boolean? = null,
    val totalElements: Int? = null
)

class ReportesPageDeserializer : JsonDeserializer<ReportesPage> {

    private val listaType: Type = object : TypeToken<List<Reporte>>() {}.type

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ReportesPage {
        if (json == null || context == null) return ReportesPage()

        if (json.isJsonArray) {
            return ReportesPage(items = context.deserialize(json, listaType) ?: emptyList())
        }

        if (!json.isJsonObject) return ReportesPage()
        val obj = json.asJsonObject

        // Algunos endpoints de búsqueda devuelven un único reporte en vez de una lista.
        if (obj.has("dni") && !obj.get("dni").isJsonNull) {
            return ReportesPage(items = listOf(context.deserialize(json, Reporte::class.java)))
        }

        val array = CLAVES_LISTA
            .firstNotNullOfOrNull { clave -> obj.get(clave)?.takeIf { it.isJsonArray } }

        val items: List<Reporte> =
            array?.let { context.deserialize<List<Reporte>>(it, listaType) } ?: emptyList()

        return ReportesPage(
            items = items,
            totalPages = obj.intOrNull("totalPages") ?: obj.intOrNull("total_pages"),
            last = obj.booleanOrNull("last"),
            hasNext = obj.booleanOrNull("hasNext"),
            totalElements = obj.intOrNull("totalElements") ?: obj.intOrNull("total")
        )
    }

    private fun JsonObject.intOrNull(clave: String): Int? =
        get(clave)?.takeIf { it.isJsonPrimitive }?.let { runCatching { it.asInt }.getOrNull() }

    private fun JsonObject.booleanOrNull(clave: String): Boolean? =
        get(clave)?.takeIf { it.isJsonPrimitive }?.let { runCatching { it.asBoolean }.getOrNull() }

    private companion object {
        val CLAVES_LISTA = listOf("content", "data", "reportes", "items", "resultado", "results")
    }
}
