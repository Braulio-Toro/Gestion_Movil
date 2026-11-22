package com.example.gestionsolicitudes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionsolicitudes.data.repository.AuthRepository
import com.example.gestionsolicitudes.data.repository.EvidenciaRepository
import com.example.gestionsolicitudes.data.repository.NotificacionRepository
import com.example.gestionsolicitudes.data.repository.SesionRepository
import com.example.gestionsolicitudes.data.repository.SolicitudRepository
import com.example.gestionsolicitudes.model.Permisos
import com.example.gestionsolicitudes.model.SolicitudFormUiState
import com.example.gestionsolicitudes.model.SolicitudValidador
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * ViewModel del formulario (crear / consultar / editar / eliminar).
 * No conoce Context, Room ni el sistema de archivos: todo pasa por repositorios inyectados,
 * por lo que se puede probar con pruebas unitarias simples.
 */
class SolicitudFormViewModel(
    private val repositorio: SolicitudRepository,
    private val sesion: SesionRepository,
    private val evidencias: EvidenciaRepository,
    private val notificaciones: NotificacionRepository,
    private val autenticacion: AuthRepository
) : ViewModel() {

    private val _formulario = MutableStateFlow(SolicitudFormUiState())
    val estado: StateFlow<SolicitudFormUiState> = _formulario.asStateFlow()

    private var cargado = false
    private var idCargado: Long? = null

    /** Prepara el formulario: vacío para una solicitud nueva o cargado desde Room para consultar/editar. */
    fun cargar(id: Long?) {
        if (cargado && idCargado == id) return // p. ej. rotación de pantalla
        cargado = true
        idCargado = id

        if (id == null) {
            _formulario.value = SolicitudFormUiState()
            return
        }

        _formulario.value = SolicitudFormUiState(solicitudId = id, cargando = true)
        viewModelScope.launch {
            val solicitud = repositorio.obtenerPorId(id)
            val usuario = sesion.usuario.value
            if (solicitud == null || usuario == null || !Permisos.puedeVer(usuario, solicitud.creadaPor)) {
                _formulario.value = SolicitudFormUiState(
                    solicitudId = id,
                    soloLectura = true,
                    mensajeError = "No se encontró la solicitud o no tienes acceso a ella."
                )
                return@launch
            }
            _formulario.value = SolicitudFormUiState(
                solicitudId = solicitud.id,
                codigo = solicitud.codigo,
                estado = solicitud.estado,
                fechaCreacion = solicitud.fechaCreacion,
                fechaActualizacion = solicitud.fechaActualizacion,
                titulo = solicitud.titulo,
                descripcion = solicitud.descripcion,
                categoria = solicitud.categoria,
                servicio = solicitud.servicio,
                evidenciaUri = solicitud.evidenciaUri,
                prioridad = solicitud.prioridad.orEmpty(),
                tecnicoAsignado = solicitud.tecnicoAsignado.orEmpty(),
                solucion = solicitud.solucion.orEmpty(),
                tecnicosDisponibles = autenticacion.tecnicos,
                soloLectura = !Permisos.puedeEditar(usuario, solicitud.creadaPor, solicitud.estado),
                puedeAsignar = Permisos.puedeAsignar(usuario, solicitud.estado),
                puedeCambiarEstado = Permisos.puedeCambiarEstado(
                    usuario, solicitud.tecnicoAsignado, solicitud.estado
                ),
                puedeEscribirSolucion = Permisos.puedeEscribirSolucion(
                    usuario, solicitud.tecnicoAsignado, solicitud.estado
                ),
                puedeEliminar = Permisos.puedeEliminar(usuario)
            )
        }
    }

    // ---------- Campos ----------

    fun onTituloChange(valor: String) {
        _formulario.update {
            it.copy(
                titulo = valor.take(SolicitudValidador.TITULO_MAX),
                tituloError = null,
                mensajeError = null
            )
        }
    }

    fun onDescripcionChange(valor: String) {
        _formulario.update {
            it.copy(
                descripcion = valor.take(SolicitudValidador.DESCRIPCION_MAX),
                descripcionError = null,
                mensajeError = null
            )
        }
    }

    fun onCategoriaChange(valor: String) {
        _formulario.update {
            it.copy(categoria = valor, categoriaError = null, servicioError = null, mensajeError = null)
        }
    }

    fun onServicioChange(valor: String) {
        _formulario.update { it.copy(servicio = valor, servicioError = null, mensajeError = null) }
    }

    fun onEvidenciaChange(uri: String?) {
        _formulario.update { it.copy(evidenciaUri = uri, mensajeError = null) }
    }

    // ---------- Gestión: prioridad, técnico, estado y solución ----------
    // Aquí solo se actualiza el UiState; los permisos se vuelven a comprobar en guardar().

    fun onPrioridadChange(valor: String) {
        _formulario.update { it.copy(prioridad = valor, mensajeError = null) }
    }

    /** La lista desplegable muestra nombres; el estado guarda el correo del técnico elegido. */
    fun onTecnicoSeleccionado(nombre: String) {
        val tecnico = _formulario.value.tecnicosDisponibles.firstOrNull { it.nombre == nombre } ?: return
        _formulario.update { it.copy(tecnicoAsignado = tecnico.correo, mensajeError = null) }
    }

    fun onEstadoChange(valor: String) {
        _formulario.update { it.copy(estado = valor, mensajeError = null) }
    }

    fun onSolucionChange(valor: String) {
        _formulario.update {
            it.copy(solucion = valor.take(SolicitudValidador.SOLUCION_MAX), mensajeError = null)
        }
    }

    // ---------- Evidencia (cámara / galería) ----------

    /** Reserva el archivo para la cámara. Devuelve su URI o null si no se pudo crear. */
    fun prepararFoto(): String? =
        try {
            evidencias.crearArchivoParaFoto()
        } catch (e: IOException) {
            _formulario.update { it.copy(mensajeError = "No se pudo preparar el archivo para la fotografía") }
            null
        }

    /** Resultado de la cámara: si la captura fue cancelada se borra el archivo reservado. */
    fun onFotoResultado(uri: String, exito: Boolean) {
        if (exito) onEvidenciaChange(uri) else descartarFoto(uri)
    }

    fun descartarFoto(uri: String) {
        viewModelScope.launch { evidencias.eliminar(uri) }
    }

    /** Imagen elegida en la galería: se copia al almacenamiento de la app y se adjunta. */
    fun onImagenGaleria(uri: String) {
        viewModelScope.launch {
            val copia = evidencias.copiarDesdeGaleria(uri)
            if (copia != null) {
                onEvidenciaChange(copia)
            } else {
                _formulario.update { it.copy(mensajeError = "No se pudo adjuntar la imagen seleccionada") }
            }
        }
    }

    // ---------- Guardar / eliminar ----------

    /** Valida los campos y deja los errores en el UiState. */
    private fun validarFormulario(): Boolean {
        val actual = _formulario.value
        val tituloError = SolicitudValidador.validarTitulo(actual.titulo)
        val descripcionError = SolicitudValidador.validarDescripcion(actual.descripcion)
        val categoriaError = SolicitudValidador.validarCategoria(actual.categoria)
        val servicioError = SolicitudValidador.validarServicio(actual.categoria, actual.servicio)

        _formulario.update {
            it.copy(
                tituloError = tituloError,
                descripcionError = descripcionError,
                categoriaError = categoriaError,
                servicioError = servicioError
            )
        }
        return listOfNotNull(tituloError, descripcionError, categoriaError, servicioError).isEmpty()
    }

    /** Guarda en Room (crea o actualiza). Al terminar marca guardadoExitosamente. */
    fun guardar() {
        val actual = _formulario.value
        if (actual.guardando || actual.cargando || actual.soloConsulta) return
        // Los datos básicos solo se validan si este perfil puede editarlos (el técnico no)
        if (!actual.soloLectura && !validarFormulario()) return

        val usuario = sesion.usuario.value
        if (usuario == null) {
            _formulario.update { it.copy(mensajeError = "Tu sesión expiró. Vuelve a iniciar sesión.") }
            return
        }

        _formulario.update { it.copy(guardando = true, mensajeError = null) }
        viewModelScope.launch {
            try {
                val id = actual.solicitudId
                val confirmacion: String
                if (id == null) {
                    if (!Permisos.puedeCrear(usuario)) {
                        _formulario.update {
                            it.copy(guardando = false, mensajeError = "Tu perfil no puede crear solicitudes.")
                        }
                        return@launch
                    }
                    val creada = repositorio.crear(
                        titulo = actual.titulo,
                        descripcion = actual.descripcion,
                        categoria = actual.categoria,
                        servicio = actual.servicio,
                        evidenciaUri = actual.evidenciaUri,
                        creadaPor = usuario.correo
                    )
                    confirmacion = "Solicitud ${creada.codigo} registrada"
                    notificaciones.solicitudRegistrada(creada.codigo, creada.titulo)
                } else {
                    val original = repositorio.obtenerPorId(id)
                    if (original == null) {
                        _formulario.update {
                            it.copy(guardando = false, mensajeError = "La solicitud ya no existe.")
                        }
                        return@launch
                    }
                    // Permisos según lo guardado (no según lo que muestre la pantalla)
                    val puedeEditarDatos = Permisos.puedeEditar(usuario, original.creadaPor, original.estado)
                    val puedeAsignar = Permisos.puedeAsignar(usuario, original.estado)
                    val puedeCambiarEstado =
                        Permisos.puedeCambiarEstado(usuario, original.tecnicoAsignado, original.estado)
                    val puedeEscribirSolucion =
                        Permisos.puedeEscribirSolucion(usuario, original.tecnicoAsignado, original.estado)
                    if (!puedeEditarDatos && !puedeAsignar && !puedeCambiarEstado && !puedeEscribirSolucion) {
                        _formulario.update {
                            it.copy(guardando = false, mensajeError = "No tienes permiso para modificar esta solicitud.")
                        }
                        return@launch
                    }
                    // Cada campo toma el valor del formulario solo si el perfil puede cambiarlo;
                    // si no, se conserva el valor guardado.
                    val actualizada = repositorio.actualizar(
                        original = original,
                        titulo = if (puedeEditarDatos) actual.titulo else original.titulo,
                        descripcion = if (puedeEditarDatos) actual.descripcion else original.descripcion,
                        categoria = if (puedeEditarDatos) actual.categoria else original.categoria,
                        servicio = if (puedeEditarDatos) actual.servicio else original.servicio,
                        evidenciaUri = if (puedeEditarDatos) actual.evidenciaUri else original.evidenciaUri,
                        prioridad = if (puedeAsignar) actual.prioridad.ifBlank { null } else original.prioridad,
                        tecnicoAsignado = if (puedeAsignar) {
                            actual.tecnicoAsignado.ifBlank { null }
                        } else {
                            original.tecnicoAsignado
                        },
                        estado = if (puedeCambiarEstado) actual.estado else original.estado,
                        solucion = if (puedeEscribirSolucion) {
                            actual.solucion.ifBlank { null }
                        } else {
                            original.solucion
                        }
                    )
                    confirmacion = "Solicitud ${actualizada.codigo} actualizada"
                }
                _formulario.update {
                    it.copy(guardando = false, guardadoExitosamente = true, mensajeConfirmacion = confirmacion)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _formulario.update {
                    it.copy(guardando = false, mensajeError = "No se pudo guardar la solicitud. Intenta nuevamente.")
                }
            }
        }
    }

    /**
     * Elimina la solicitud abierta (D del CRUD). Solo técnico y administrador; el permiso se
     * vuelve a comprobar aquí aunque la pantalla ya oculte el botón al cliente.
     */
    fun eliminar() {
        val actual = _formulario.value
        val id = actual.solicitudId ?: return
        if (actual.eliminando || actual.guardando || actual.cargando) return

        val usuario = sesion.usuario.value
        if (usuario == null || !Permisos.puedeEliminar(usuario)) {
            _formulario.update {
                it.copy(mensajeError = "Tu perfil no tiene permiso para eliminar solicitudes.")
            }
            return
        }

        _formulario.update { it.copy(eliminando = true, mensajeError = null) }
        viewModelScope.launch {
            try {
                val solicitud = repositorio.obtenerPorId(id)
                if (solicitud == null) {
                    _formulario.update {
                        it.copy(eliminando = false, mensajeError = "La solicitud ya no existe.")
                    }
                    return@launch
                }
                repositorio.eliminar(solicitud)
                // También se borra la fotografía de evidencia guardada por la app
                solicitud.evidenciaUri?.let { evidencias.eliminar(it) }
                _formulario.update {
                    it.copy(
                        eliminando = false,
                        eliminadoExitosamente = true,
                        mensajeConfirmacion = "Solicitud ${solicitud.codigo} eliminada"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _formulario.update {
                    it.copy(eliminando = false, mensajeError = "No se pudo eliminar la solicitud. Intenta nuevamente.")
                }
            }
        }
    }
}
