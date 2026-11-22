package com.example.gestionsolicitudes.model

/**
 * Modelo de dominio de una solicitud. Es lo único que ven los ViewModels y las pantallas:
 * la entidad de Room ([com.example.gestionsolicitudes.data.local.SolicitudEntity]) se queda
 * dentro de la capa de datos.
 */
data class Solicitud(
    val id: Long = 0,
    /** Código único de seguimiento, por ejemplo SOL-20261015-4821. */
    val codigo: String,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val servicio: String,
    /** URI (content://) de la fotografía de evidencia guardada en el almacenamiento de la app. */
    val evidenciaUri: String?,
    val estado: String,
    val fechaCreacion: Long,
    val fechaActualizacion: Long,
    /** Correo (ficticio) del usuario que registró la solicitud. */
    val creadaPor: String,
    /** Prioridad ([PrioridadSolicitud]); null mientras el administrador no la asigne. */
    val prioridad: String? = null,
    /** Correo del técnico asignado por el administrador; null si aún no hay técnico. */
    val tecnicoAsignado: String? = null,
    /** Solución escrita por el técnico asignado; null mientras no exista. */
    val solucion: String? = null
)
