package com.example.gestionsolicitudes.viewmodel

import com.example.gestionsolicitudes.data.repository.AuthRepository
import com.example.gestionsolicitudes.data.repository.EvidenciaRepository
import com.example.gestionsolicitudes.data.repository.NotificacionRepository
import com.example.gestionsolicitudes.data.repository.SesionRepository
import com.example.gestionsolicitudes.data.repository.SolicitudRepository
import com.example.gestionsolicitudes.model.EstadoSolicitud
import com.example.gestionsolicitudes.model.Perfil
import com.example.gestionsolicitudes.model.Solicitud
import com.example.gestionsolicitudes.model.Usuario
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SolicitudFormViewModelTest : StringSpec({

    beforeSpec { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterSpec { Dispatchers.resetMain() }

    val cliente = Usuario(1, "Cliente", "cliente@demo.test", "x", Perfil.CLIENTE, "Empresa")
    val tecnico = Usuario(3, "Tecnico", "tecnico@demo.test", "x", Perfil.TECNICO, "Empresa")
    val admin = Usuario(2, "Admin", "admin@demo.test", "x", Perfil.ADMINISTRADOR, "Empresa")

    val creada = Solicitud(
        id = 7, codigo = "SOL-20261116-1234", titulo = "Sitio caido",
        descripcion = "El sitio corporativo no responde desde la manana",
        categoria = "Incidente técnico", servicio = "Sitio web corporativo",
        evidenciaUri = null, estado = EstadoSolicitud.RECIBIDA,
        fechaCreacion = 1L, fechaActualizacion = 1L, creadaPor = cliente.correo
    )

    class Entorno(usuario: Usuario?) {
        val repositorio = mockk<SolicitudRepository>()
        val evidencias = mockk<EvidenciaRepository>(relaxed = true)
        val notificaciones = mockk<NotificacionRepository>(relaxed = true)
        val sesion = SesionRepository().also { s -> usuario?.let(s::iniciar) }
        val vm = SolicitudFormViewModel(repositorio, sesion, evidencias, notificaciones, AuthRepository())
    }

    fun Entorno.llenarFormularioValido() {
        vm.cargar(null)
        vm.onTituloChange(creada.titulo)
        vm.onDescripcionChange(creada.descripcion)
        vm.onCategoriaChange(creada.categoria)
        vm.onServicioChange(creada.servicio)
    }

    "guardar con datos invalidos deja los errores en el estado y no toca el repositorio" {
        val e = Entorno(cliente)
        e.vm.cargar(null)

        e.vm.guardar()

        e.vm.estado.value.tituloError.shouldNotBeNull()
        e.vm.estado.value.guardadoExitosamente shouldBe false
        coVerify(exactly = 0) { e.repositorio.crear(any(), any(), any(), any(), any(), any()) }
    }

    "guardar una solicitud valida la crea, notifica y marca el guardado" {
        val e = Entorno(cliente)
        coEvery { e.repositorio.crear(any(), any(), any(), any(), any(), any()) } returns creada
        e.llenarFormularioValido()

        e.vm.guardar()

        e.vm.estado.value.guardadoExitosamente shouldBe true
        e.vm.estado.value.mensajeConfirmacion shouldBe "Solicitud ${creada.codigo} registrada"
        verify(exactly = 1) { e.notificaciones.solicitudRegistrada(creada.codigo, creada.titulo) }
    }

    "el perfil tecnico no puede crear solicitudes" {
        val e = Entorno(tecnico)
        e.llenarFormularioValido()

        e.vm.guardar()

        e.vm.estado.value.guardadoExitosamente shouldBe false
        e.vm.estado.value.mensajeError.shouldNotBeNull()
        coVerify(exactly = 0) { e.repositorio.crear(any(), any(), any(), any(), any(), any()) }
    }

    "una foto cancelada se descarta en el repositorio de evidencias" {
        val e = Entorno(cliente)
        e.vm.cargar(null)

        e.vm.onFotoResultado("content://evidencia/1", exito = false)

        coVerify(exactly = 1) { e.evidencias.eliminar("content://evidencia/1") }
        e.vm.estado.value.evidenciaUri shouldBe null
    }

    "una foto capturada queda adjunta como evidencia" {
        val e = Entorno(cliente)
        e.vm.cargar(null)

        e.vm.onFotoResultado("content://evidencia/1", exito = true)

        e.vm.estado.value.evidenciaUri shouldBe "content://evidencia/1"
    }

    // ---------- Prioridad, técnico asignado, estado y solución ----------

    "el administrador asigna prioridad y tecnico" {
        val tecnicoDemo = AuthRepository().tecnicos.first()
        val e = Entorno(admin)
        coEvery { e.repositorio.obtenerPorId(creada.id) } returns creada
        coEvery {
            e.repositorio.actualizar(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns creada
        e.vm.cargar(creada.id)

        e.vm.estado.value.puedeAsignar shouldBe true
        e.vm.onPrioridadChange("Muy alta")
        e.vm.onTecnicoSeleccionado(tecnicoDemo.nombre)
        e.vm.guardar()

        e.vm.estado.value.guardadoExitosamente shouldBe true
        coVerify(exactly = 1) {
            e.repositorio.actualizar(
                creada, creada.titulo, creada.descripcion, creada.categoria, creada.servicio, null,
                "Muy alta", tecnicoDemo.correo, EstadoSolicitud.RECIBIDA, null
            )
        }
    }

    "el tecnico asignado cambia el estado y escribe la solucion sin tocar los datos basicos" {
        val asignada = creada.copy(prioridad = "Alta", tecnicoAsignado = tecnico.correo)
        val solucion = "Se reinicio el servidor web y el sitio volvio a responder"
        val e = Entorno(tecnico)
        coEvery { e.repositorio.obtenerPorId(asignada.id) } returns asignada
        coEvery {
            e.repositorio.actualizar(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns asignada
        e.vm.cargar(asignada.id)

        e.vm.estado.value.puedeCambiarEstado shouldBe true
        e.vm.estado.value.puedeEscribirSolucion shouldBe true
        e.vm.estado.value.puedeAsignar shouldBe false
        e.vm.onEstadoChange(EstadoSolicitud.RESUELTA)
        e.vm.onSolucionChange(solucion)
        // Un intento de cambiar la prioridad desde la pantalla no debe llegar a Room
        e.vm.onPrioridadChange("Baja")
        e.vm.guardar()

        e.vm.estado.value.guardadoExitosamente shouldBe true
        coVerify(exactly = 1) {
            e.repositorio.actualizar(
                asignada, asignada.titulo, asignada.descripcion, asignada.categoria, asignada.servicio, null,
                "Alta", tecnico.correo, EstadoSolicitud.RESUELTA, solucion
            )
        }
    }

    "un tecnico no asignado solo consulta la solicitud y no puede guardar" {
        val e = Entorno(tecnico)
        coEvery { e.repositorio.obtenerPorId(creada.id) } returns creada
        e.vm.cargar(creada.id)

        e.vm.estado.value.soloConsulta shouldBe true
        e.vm.onEstadoChange(EstadoSolicitud.CERRADA)
        e.vm.guardar()

        e.vm.estado.value.guardadoExitosamente shouldBe false
        coVerify(exactly = 0) {
            e.repositorio.actualizar(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    "el cliente ve la solicitud pero no puede gestionarla" {
        val resuelta = creada.copy(estado = EstadoSolicitud.RESUELTA, solucion = "Listo")
        val e = Entorno(cliente)
        coEvery { e.repositorio.obtenerPorId(resuelta.id) } returns resuelta
        e.vm.cargar(resuelta.id)

        e.vm.estado.value.solucion shouldBe "Listo"
        e.vm.estado.value.puedeGestionar shouldBe false
        e.vm.estado.value.soloConsulta shouldBe true
    }

    "prepararFoto informa el error si no se puede crear el archivo" {
        val e = Entorno(cliente)
        every { e.evidencias.crearArchivoParaFoto() } throws java.io.IOException("sin espacio")
        e.vm.cargar(null)

        e.vm.prepararFoto() shouldBe null
        e.vm.estado.value.mensajeError.shouldNotBeNull()
    }
})
