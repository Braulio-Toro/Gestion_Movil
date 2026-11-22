package com.example.gestionsolicitudes.model

/** Resultado de procesar el contenido leído desde un QR (equivalente al QrResult de camara_prueba). */
sealed interface QrResult {
    /** El QR corresponde a una solicitud que el usuario puede ver. */
    data class Encontrada(val solicitudId: Long, val codigo: String) : QrResult

    /** El contenido no tiene el formato de código de solicitud (SOL-AAAAMMDD-####). */
    data object NoValido : QrResult

    /** El código es válido pero no existe, o el perfil no tiene acceso a esa solicitud. */
    data class NoEncontrada(val codigo: String) : QrResult
}
