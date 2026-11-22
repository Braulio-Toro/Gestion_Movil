package com.example.gestionsolicitudes.model

/**
 * Mensaje estructurado para compartir por WhatsApp.
 * Solo incluye código, título, categoría y un enlace ficticio de seguimiento (sin datos sensibles).
 */
object MensajeSeguimiento {
    const val URL_BASE = "https://seguimiento.demo.test/solicitudes/"

    fun armar(codigo: String, titulo: String, categoria: String): String =
        buildString {
            appendLine("Hola, registré una solicitud en Gestión de Solicitudes.")
            appendLine("Código: $codigo")
            appendLine("Título: ${titulo.trim()}")
            appendLine("Categoría: $categoria")
            append("Seguimiento: $URL_BASE$codigo")
        }
}
