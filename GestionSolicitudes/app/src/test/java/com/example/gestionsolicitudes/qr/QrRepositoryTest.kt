package com.example.gestionsolicitudes.qr

import com.example.gestionsolicitudes.data.repository.QrRepository
import com.example.gestionsolicitudes.data.repository.SolicitudRepository
import com.example.gestionsolicitudes.model.EstadoSolicitud
import com.example.gestionsolicitudes.model.Perfil
import com.example.gestionsolicitudes.model.QrResult
import com.example.gestionsolicitudes.model.Solicitud
import com.example.gestionsolicitudes.model.Usuario
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest

class QrRepositoryTest : StringSpec({

    val cliente = Usuario(1, "Cliente", "cliente@demo.test", "x", Perfil.CLIENTE, "Empresa")
    val admin = Usuario(2, "Admin", "admin@demo.test", "x", Perfil.ADMINISTRADOR, "Empresa")
    val solicitud = Solicitud(
        id = 5, codigo = "SOL-20261116-1234", titulo = "t", descripcion = "d", categoria = "c",
        servicio = "s", evidenciaUri = null, estado = EstadoSolicitud.RECIBIDA,
        fechaCreacion = 1L, fechaActualizacion = 1L, creadaPor = cliente.correo
    )

    "un contenido sin formato de codigo no consulta la base de datos" {
        val solicitudes = mockk<SolicitudRepository>()
        runTest {
            QrRepository(solicitudes).procesarContenido("hola mundo", cliente) shouldBe QrResult.NoValido
            coVerify(exactly = 0) { solicitudes.buscarPorCodigo(any()) }
        }
    }

    "un codigo existente del propio cliente se encuentra" {
        val solicitudes = mockk<SolicitudRepository>()
        coEvery { solicitudes.buscarPorCodigo(solicitud.codigo) } returns solicitud
        runTest {
            QrRepository(solicitudes).procesarContenido("sol-20261116-1234", cliente) shouldBe
                QrResult.Encontrada(5, solicitud.codigo)
        }
    }

    "el cliente no accede a la solicitud de otro usuario" {
        val ajena = solicitud.copy(creadaPor = "otro@demo.test")
        val solicitudes = mockk<SolicitudRepository>()
        coEvery { solicitudes.buscarPorCodigo(ajena.codigo) } returns ajena
        runTest {
            QrRepository(solicitudes).procesarContenido(ajena.codigo, cliente) shouldBe
                QrResult.NoEncontrada(ajena.codigo)
        }
    }

    "el administrador puede abrir cualquier solicitud" {
        val ajena = solicitud.copy(creadaPor = "otro@demo.test")
        val solicitudes = mockk<SolicitudRepository>()
        coEvery { solicitudes.buscarPorCodigo(ajena.codigo) } returns ajena
        runTest {
            QrRepository(solicitudes).procesarContenido(ajena.codigo, admin) shouldBe
                QrResult.Encontrada(5, ajena.codigo)
        }
    }
})
