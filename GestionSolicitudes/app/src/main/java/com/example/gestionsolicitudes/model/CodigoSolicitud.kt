package com.example.gestionsolicitudes.model

/** Extrae el código único (SOL-AAAAMMDD-####) desde el contenido leído en un QR. */
object CodigoSolicitud {
    private val patron = Regex("SOL-\\d{8}-\\d{4}", RegexOption.IGNORE_CASE)

    fun extraer(contenido: String): String? =
        patron.find(contenido.trim())?.value?.uppercase()
}
