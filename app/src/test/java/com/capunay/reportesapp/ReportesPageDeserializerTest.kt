package com.capunay.reportesapp

import com.capunay.reportesapp.data.model.ReportesPage
import com.capunay.reportesapp.data.model.ReportesPageDeserializer
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportesPageDeserializerTest {

    private val gson = GsonBuilder()
        .registerTypeAdapter(ReportesPage::class.java, ReportesPageDeserializer())
        .create()

    @Test
    fun `parsea una pagina de Spring Data`() {
        val json = """
            {
              "content": [
                {"id":1,"dni":"12345678","fecha":"2026-10-29T10:15:30","tipo":"MALEZA",
                 "direccion":"Av. Siempre Viva 123","referencia":null,"estado":1}
              ],
              "totalPages": 3,
              "totalElements": 12,
              "last": false
            }
        """.trimIndent()

        val pagina = gson.fromJson(json, ReportesPage::class.java)

        assertEquals(1, pagina.items.size)
        assertEquals("12345678", pagina.items.first().dni)
        assertEquals(3, pagina.totalPages)
        assertEquals(12, pagina.totalElements)
        assertFalse(pagina.last!!)
    }

    @Test
    fun `parsea un array simple`() {
        val json = """
            [
              {"id":1,"dni":"11111111","tipo":"DOMICILIARIA","direccion":"Calle 1","estado":1},
              {"id":2,"dni":"22222222","tipo":"DESMONTE","direccion":"Calle 2","estado":0}
            ]
        """.trimIndent()

        val pagina = gson.fromJson(json, ReportesPage::class.java)

        assertEquals(2, pagina.items.size)
        assertTrue(pagina.items.last().estaAtendido)
        assertEquals(null, pagina.totalPages)
    }

    @Test
    fun `parsea respuesta envuelta en data`() {
        val json = """
            {"data":[{"id":7,"dni":"87654321","tipo":"MALEZA","direccion":"Jr. Lima 456","estado":1}]}
        """.trimIndent()

        val pagina = gson.fromJson(json, ReportesPage::class.java)

        assertEquals(1, pagina.items.size)
        assertEquals(7, pagina.items.first().id)
    }

    @Test
    fun `parsea un unico reporte devuelto por la busqueda`() {
        val json = """
            {"id":5,"dni":"55555555","tipo":"DOMICILIARIA","direccion":"Psj. Sol 10","estado":1}
        """.trimIndent()

        val pagina = gson.fromJson(json, ReportesPage::class.java)

        assertEquals(1, pagina.items.size)
        assertEquals("55555555", pagina.items.first().dni)
    }

    @Test
    fun `parsea respuesta real del servicio en Render`() {
        val json = """
            {"content":[],"offset":0,"limit":5,"totalElements":0,"totalPages":0,"hasNext":false}
        """.trimIndent()

        val pagina = gson.fromJson(json, ReportesPage::class.java)

        assertTrue(pagina.items.isEmpty())
        assertEquals(0, pagina.totalElements)
        assertFalse(pagina.hasNext!!)
    }

    @Test
    fun `parsea respuesta con hasNext verdadero`() {
        val json = """
            {
              "content":[{"id":1,"dni":"12345678","fecha":"2026-10-29T10:15:30","tipo":"MALEZA",
                          "direccion":"Av. Siempre Viva 123","referencia":null,"estado":1}],
              "offset":0,"limit":5,"totalElements":8,"totalPages":2,"hasNext":true
            }
        """.trimIndent()

        val pagina = gson.fromJson(json, ReportesPage::class.java)

        assertEquals(1, pagina.items.size)
        assertTrue(pagina.hasNext!!)
        assertEquals(8, pagina.totalElements)
    }

    @Test
    fun `objeto vacio no rompe la app`() {
        val pagina = gson.fromJson("{}", ReportesPage::class.java)
        assertTrue(pagina.items.isEmpty())
    }
}
