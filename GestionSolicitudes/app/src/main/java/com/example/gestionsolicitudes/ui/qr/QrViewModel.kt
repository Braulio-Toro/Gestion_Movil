package com.example.gestionsolicitudes.ui.qr

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionsolicitudes.data.local.AppDatabase
import com.example.gestionsolicitudes.data.repository.SolicitudRepository
import com.example.gestionsolicitudes.model.CodigoSolicitud
import com.example.gestionsolicitudes.model.Permisos
import com.example.gestionsolicitudes.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QrUiState(
    val buscando: Boolean = false,
    val codigo: String? = null,
    /** Id de la solicitud encontrada (la pantalla navega al detalle y consume el evento). */
    val solicitudId: Long? = null,
    val mensaje: String? = null
)

/** Procesa el contenido leído desde el QR: identifica la solicitud por su código único. */
class QrViewModel(app: Application) : AndroidViewModel(app) {

    private val repositorio = SolicitudRepository(AppDatabase.getDatabase(app).solicitudDao())
    private var usuario: Usuario? = null

    private val _estado = MutableStateFlow(QrUiState())
    val estado: StateFlow<QrUiState> = _estado.asStateFlow()

    fun setUsuario(usuario: Usuario?) {
        this.usuario = usuario
        if (usuario == null) reiniciar()
    }

    fun onQrDetectado(contenido: String) {
        if (_estado.value.buscando) return
        val codigo = CodigoSolicitud.extraer(contenido)
        if (codigo == null) {
            _estado.value = QrUiState(mensaje = "El código QR leído no corresponde a una solicitud registrada.")
            return
        }
        _estado.value = QrUiState(buscando = true, codigo = codigo)
        viewModelScope.launch {
            val solicitud = repositorio.buscarPorCodigo(codigo)
            val actual = usuario
            _estado.value =
                if (solicitud != null && actual != null && Permisos.puedeVer(actual, solicitud.creadaPor)) {
                    QrUiState(codigo = codigo, solicitudId = solicitud.id)
                } else {
                    QrUiState(
                        codigo = codigo,
                        mensaje = "No se encontró una solicitud con el código $codigo o no tienes acceso a ella."
                    )
                }
        }
    }

    fun reiniciar() {
        _estado.value = QrUiState()
    }
}
