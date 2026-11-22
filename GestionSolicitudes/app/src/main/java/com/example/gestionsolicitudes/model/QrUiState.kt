package com.example.gestionsolicitudes.model

/** Estado del lector de códigos QR. */
data class QrUiState(
    val buscando: Boolean = false,
    val codigo: String? = null,
    /** Id de la solicitud encontrada (la pantalla navega al detalle y consume el evento). */
    val solicitudId: Long? = null,
    val mensaje: String? = null
)
