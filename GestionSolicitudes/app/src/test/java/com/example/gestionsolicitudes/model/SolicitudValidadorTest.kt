package com.example.gestionsolicitudes.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SolicitudValidadorTest {

    @Test
    fun titulo_vacio_es_error() {
        assertNotNull(SolicitudValidador.validarTitulo("   "))
    }

    @Test
    fun titulo_valido_no_tiene_error() {
        assertNull(SolicitudValidador.validarTitulo("Cambiar banner del sitio"))
    }

    @Test
    fun descripcion_corta_es_error() {
        assertNotNull(SolicitudValidador.validarDescripcion("muy corta"))
    }

    @Test
    fun descripcion_con_largo_minimo_es_valida() {
        val texto = "a".repeat(SolicitudValidador.DESCRIPCION_MIN)
        assertNull(SolicitudValidador.validarDescripcion(texto))
    }

    @Test
    fun categoria_vacia_es_error() {
        assertNotNull(SolicitudValidador.validarCategoria(""))
    }

    @Test
    fun servicio_es_obligatorio_en_categorias_tecnicas() {
        assertNotNull(SolicitudValidador.validarServicio("Hosting", ""))
        assertNull(SolicitudValidador.validarServicio("Hosting", "Hosting y dominio"))
    }

    @Test
    fun servicio_no_es_obligatorio_en_consulta() {
        assertNull(SolicitudValidador.validarServicio("Consulta", ""))
    }

    @Test
    fun cliente_solo_ve_sus_solicitudes() {
        val cliente = Usuario(1, "Cliente", "cliente@demo.test", "x", Perfil.CLIENTE, "Demo")
        assertEquals(true, Permisos.puedeVer(cliente, "cliente@demo.test"))
        assertEquals(false, Permisos.puedeVer(cliente, "otro@demo.test"))
    }
}
