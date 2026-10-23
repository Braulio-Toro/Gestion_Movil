package com.example.gestionsolicitudes.model

/** Estados mínimos definidos en el caso. En esta entrega toda solicitud nace como "Recibida". */
object EstadoSolicitud {
    const val RECIBIDA = "Recibida"
    const val EN_REVISION = "En revisión"
    const val PENDIENTE_INFORMACION = "Pendiente de información"
    const val ASIGNADA = "Asignada"
    const val EN_PROCESO = "En proceso"
    const val RESUELTA = "Resuelta"
    const val CERRADA = "Cerrada"
    const val CANCELADA = "Cancelada"

    val todos = listOf(
        RECIBIDA, EN_REVISION, PENDIENTE_INFORMACION, ASIGNADA,
        EN_PROCESO, RESUELTA, CERRADA, CANCELADA
    )
}

/** Catálogos académicos (categorías sugeridas en el caso y servicios ficticios). */
object Catalogos {
    val categorias = listOf(
        "Desarrollo web",
        "Mantenimiento web",
        "Incidente técnico",
        "Hosting",
        "Soporte de correo",
        "Marketing digital",
        "Solicitud de cambio",
        "Consulta",
        "Acceso o configuración",
        "Otro requerimiento"
    )

    val servicios = listOf(
        "Sitio web corporativo",
        "Tienda en línea",
        "Hosting y dominio",
        "Correo corporativo",
        "Campaña de marketing digital",
        "Soporte tecnológico general"
    )

    /** Categorías generales para las que el servicio relacionado no es obligatorio. */
    private val categoriasSinServicio = setOf("Consulta", "Otro requerimiento")

    fun requiereServicio(categoria: String): Boolean =
        categoria.isNotBlank() && categoria !in categoriasSinServicio
}
