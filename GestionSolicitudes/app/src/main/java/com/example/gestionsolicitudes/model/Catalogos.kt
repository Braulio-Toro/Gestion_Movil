package com.example.gestionsolicitudes.model

/**
 * Estados de una solicitud. Toda solicitud nace como "Recibida"; luego los cambia el
 * administrador o el técnico asignado (ver [Permisos]).
 */
object EstadoSolicitud {
    const val RECIBIDA = "Recibida"
    const val EN_REVISION = "En revisión"
    const val EN_PROCESO = "En proceso"
    const val RESUELTA = "Resuelta"
    const val CERRADA = "Cerrada"
    const val CANCELADA = "Cancelada"

    val todos = listOf(RECIBIDA, EN_REVISION, EN_PROCESO, RESUELTA, CERRADA, CANCELADA)

    /** Estados finales: una vez alcanzados, la solicitud ya no se puede gestionar. */
    fun esFinal(estado: String): Boolean = estado == CERRADA || estado == CANCELADA
}

/** Prioridad de atención. La asigna el administrador; una solicitud nueva no tiene prioridad. */
object PrioridadSolicitud {
    const val BAJA = "Baja"
    const val MEDIA = "Media"
    const val ALTA = "Alta"
    const val MUY_ALTA = "Muy alta"

    /** Texto que se muestra mientras el administrador no asigna prioridad o técnico. */
    const val SIN_ASIGNAR = "Sin asignar"

    val todas = listOf(BAJA, MEDIA, ALTA, MUY_ALTA)
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
