package com.example.gestionsolicitudes.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CodigoYMensajeTest {

    @Test
    fun extrae_codigo_desde_contenido_del_qr() {
        assertEquals("SOL-20261015-4821", CodigoSolicitud.extraer("SOL-20261015-4821"))
        assertEquals("SOL-20261015-4821", CodigoSolicitud.extraer("  sol-20261015-4821 "))
        assertEquals(
            "SOL-20261015-4821",
            CodigoSolicitud.extraer("https://seguimiento.demo.test/solicitudes/SOL-20261015-4821")
        )
    }

    @Test
    fun qr_ajeno_no_se_reconoce() {
        assertNull(CodigoSolicitud.extraer("https://ejemplo.test/otra-cosa"))
    }

    @Test
    fun mensaje_incluye_codigo_titulo_categoria_y_seguimiento() {
        val mensaje = MensajeSeguimiento.armar("SOL-20261015-4821", "Cambiar banner", "Mantenimiento web")
        assertTrue(mensaje.contains("SOL-20261015-4821"))
        assertTrue(mensaje.contains("Cambiar banner"))
        assertTrue(mensaje.contains("Mantenimiento web"))
        assertTrue(mensaje.contains(MensajeSeguimiento.URL_BASE + "SOL-20261015-4821"))
    }
}
