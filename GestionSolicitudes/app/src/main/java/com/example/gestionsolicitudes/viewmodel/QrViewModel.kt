package com.example.gestionsolicitudes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionsolicitudes.data.repository.QrRepository
import com.example.gestionsolicitudes.data.repository.SesionRepository
import com.example.gestionsolicitudes.model.QrResult
import com.example.gestionsolicitudes.model.QrUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Recibe el texto leído por la cámara, lo delega al repositorio y publica el resultado como estado. */
class QrViewModel(
    private val repositorio: QrRepository,
    private val sesion: SesionRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(QrUiState())
    val estado: StateFlow<QrUiState> = _estado.asStateFlow()

    fun onQrDetectado(contenido: String) {
        if (_estado.value.buscando) return
        _estado.value = QrUiState(buscando = true)
        viewModelScope.launch {
            _estado.value = when (val resultado = repositorio.procesarContenido(contenido, sesion.usuario.value)) {
                is QrResult.Encontrada ->
                    QrUiState(codigo = resultado.codigo, solicitudId = resultado.solicitudId)

                QrResult.NoValido ->
                    QrUiState(mensaje = "El código QR leído no corresponde a una solicitud registrada.")

                is QrResult.NoEncontrada ->
                    QrUiState(
                        codigo = resultado.codigo,
                        mensaje = "No se encontró una solicitud con el código ${resultado.codigo} o no tienes acceso a ella."
                    )
            }
        }
    }

    fun reiniciar() {
        _estado.value = QrUiState()
    }
}
