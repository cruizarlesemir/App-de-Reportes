package com.capunay.reportesapp

import com.capunay.reportesapp.util.Fechas
import org.junit.Assert.assertEquals
import org.junit.Test

class FechasTest {

    @Test
    fun `formatea ISO sin zona`() {
        assertEquals("29/10/2026 10:15", Fechas.formatear("2026-10-29T10:15:30"))
    }

    @Test
    fun `formatea ISO con milisegundos y zona`() {
        assertEquals("29/10/2026 10:15", Fechas.formatear("2026-10-29T10:15:30.000+00:00"))
    }

    @Test
    fun `formatea formato con espacio`() {
        assertEquals("29/10/2026 10:15", Fechas.formatear("2026-10-29 10:15:30"))
    }

    @Test
    fun `mantiene texto desconocido`() {
        assertEquals("sin fecha", Fechas.formatear("sin fecha"))
    }

    @Test
    fun `convierte ISO al formato alterno`() {
        assertEquals("2026-10-29 10:15:30", Fechas.aFormatoApi("2026-10-29T10:15:30"))
    }

    @Test
    fun `fecha nula muestra guion`() {
        assertEquals("—", Fechas.formatear(null))
    }
}
