package com.example.gestionsolicitudes.ui.solicitud

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionsolicitudes.data.local.AppDatabase
import com.example.gestionsolicitudes.data.repository.SolicitudRepository
import com.example.gestionsolicitudes.model.Perfil
import com.example.gestionsolicitudes.model.Permisos
import com.example.gestionsolicitudes.model.SolicitudValidador
import com.example.gestionsolicitudes.model.Usuario
import com.example.gestionsolicitudes.util.NotificacionHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SolicitudViewModel(app: Application) : AndroidViewModel(app) {

    private val repositorio = SolicitudRepository(AppDatabase.getDatabase(app).solicitudDao())

    private val _usuario = MutableStateFlow<Usuario?>(null)
    private val _busqueda = MutableStateFlow("")
    private val _formulario = MutableStateFlow(SolicitudUiState())
    private val _mensajes = Channel<String>(Channel.BUFFERED)

    private var formularioActivo = false
    private var formularioId: Long? = null

    /** Estado del formulario (UiState + StateFlow). */
    val formulario: StateFlow<SolicitudUiState> = _formulario.asStateFlow()

    /** Mensajes de confirmación para mostrar en la lista (Snackbar). */
    val mensajes: Flow<String> = _mensajes.receiveAsFlow()

    /** Lista reactiva: se actualiza sola cuando Room registra o modifica una solicitud. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val lista: StateFlow<ListaSolicitudesUiState> = combine(
        _usuario.flatMapLatest { usuario ->
            repositorio.observarSolicitudes(usuario).map { solicitudes -> usuario to solicitudes }
        },
        _busqueda
    ) { (usuario, solicitudes), busqueda ->
        val texto = busqueda.trim()
        val filtradas = if (texto.isEmpty()) {
            solicitudes
        } else {
            solicitudes.filter {
                it.codigo.contains(texto, ignoreCase = true) ||
                    it.titulo.contains(texto, ignoreCase = true) ||
                    it.categoria.contains(texto, ignoreCase = true)
            }
        }
        ListaSolicitudesUiState(
            solicitudes = filtradas,
            busqueda = busqueda,
            cargando = false,
            usuario = usuario
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListaSolicitudesUiState())

    // ---------- Sesión ----------

    fun setUsuario(usuario: Usuario?) {
        _usuario.value = usuario
        if (usuario == null) {
            _busqueda.value = ""
            cerrarFormulario()
        }
    }

    // ---------- Lista ----------

    fun onBusquedaChange(valor: String) {
        _busqueda.value = valor
    }

    // ---------- Formulario ----------

    /** Prepara el formulario: vacío para una solicitud nueva o cargado desde Room para consultar/editar. */
    fun prepararFormulario(id: Long?) {
        if (formularioActivo && formularioId == id) return // p. ej. rotación de pantalla
        formularioActivo = true
        formularioId = id

        if (id == null) {
            _formulario.value = SolicitudUiState()
            return
        }

        _formulario.value = SolicitudUiState(solicitudId = id, cargando = true)
        viewModelScope.launch {
            val solicitud = repositorio.obtenerPorId(id)
            val usuario = _usuario.value
            if (solicitud == null || usuario == null || !Permisos.puedeVer(usuario, solicitud.creadaPor)) {
                _formulario.value = SolicitudUiState(
                    solicitudId = id,
                    soloLectura = true,
                    mensajeError = "No se encontró la solicitud o no tienes acceso a ella."
                )
                return@launch
            }
            _formulario.value = SolicitudUiState(
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
                soloLectura = !Permisos.puedeEditar(usuario, solicitud.creadaPor, solicitud.estado)
            )
        }
    }

    fun cerrarFormulario() {
        formularioActivo = false
        formularioId = null
        _formulario.value = SolicitudUiState()
    }

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

    /** Valida los campos desde el ViewModel y deja los errores en el UiState. */
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
        if (actual.guardando || actual.soloLectura || actual.cargando) return
        if (!validarFormulario()) return

        val usuario = _usuario.value
        if (usuario == null) {
            _formulario.update { it.copy(mensajeError = "Tu sesión expiró. Vuelve a iniciar sesión.") }
            return
        }

        _formulario.update { it.copy(guardando = true, mensajeError = null) }
        viewModelScope.launch {
            try {
                val id = actual.solicitudId
                if (id == null) {
                    if (usuario.perfil == Perfil.TECNICO) {
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
                    _mensajes.send("Solicitud ${creada.codigo} registrada")
                    NotificacionHelper.notificarSolicitudRegistrada(getApplication(), creada.codigo, creada.titulo)
                } else {
                    val original = repositorio.obtenerPorId(id)
                    if (original == null ||
                        !Permisos.puedeEditar(usuario, original.creadaPor, original.estado)
                    ) {
                        _formulario.update {
                            it.copy(guardando = false, mensajeError = "No tienes permiso para modificar esta solicitud.")
                        }
                        return@launch
                    }
                    val actualizada = repositorio.actualizar(
                        original = original,
                        titulo = actual.titulo,
                        descripcion = actual.descripcion,
                        categoria = actual.categoria,
                        servicio = actual.servicio,
                        evidenciaUri = actual.evidenciaUri
                    )
                    _mensajes.send("Solicitud ${actualizada.codigo} actualizada")
                }
                _formulario.update { it.copy(guardando = false, guardadoExitosamente = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _formulario.update {
                    it.copy(guardando = false, mensajeError = "No se pudo guardar la solicitud. Intenta nuevamente.")
                }
            }
        }
    }
}
