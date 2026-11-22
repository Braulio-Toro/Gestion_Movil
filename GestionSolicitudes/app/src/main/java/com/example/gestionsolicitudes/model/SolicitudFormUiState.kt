package com.example.gestionsolicitudes.model

/** Estado del formulario de solicitud (crear / consultar / editar). */
data class SolicitudFormUiState(
    val solicitudId: Long? = null,
    val codigo: String = "",
    val estado: String = EstadoSolicitud.RECIBIDA,
    val fechaCreacion: Long? = null,
    val fechaActualizacion: Long? = null,

    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val servicio: String = "",
    val evidenciaUri: String? = null,

    // --- Gestión de la solicitud (prioridad, técnico, estado y solución) ---
    /** Vacío = sin prioridad asignada. */
    val prioridad: String = "",
    /** Correo del técnico asignado; vacío = sin técnico. */
    val tecnicoAsignado: String = "",
    val solucion: String = "",
    /** Técnicos que el administrador puede asignar. */
    val tecnicosDisponibles: List<Usuario> = emptyList(),
    /** Administrador: puede asignar prioridad y técnico. */
    val puedeAsignar: Boolean = false,
    /** Administrador o técnico asignado: puede cambiar el estado. */
    val puedeCambiarEstado: Boolean = false,
    /** Técnico asignado: puede escribir la solución. */
    val puedeEscribirSolucion: Boolean = false,

    val tituloError: String? = null,
    val descripcionError: String? = null,
    val categoriaError: String? = null,
    val servicioError: String? = null,
    val mensajeError: String? = null,

    val soloLectura: Boolean = false,
    /** Solo técnico y administrador pueden eliminar la solicitud. */
    val puedeEliminar: Boolean = false,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false,
    val eliminando: Boolean = false,
    val eliminadoExitosamente: Boolean = false,
    /** Texto de confirmación que la lista muestra en un Snackbar al volver. */
    val mensajeConfirmacion: String? = null
) {
    val esEdicion: Boolean get() = solicitudId != null

    /** El perfil puede tocar al menos un campo de gestión (prioridad, técnico, estado o solución). */
    val puedeGestionar: Boolean get() = puedeAsignar || puedeCambiarEstado || puedeEscribirSolucion

    /** Nada es editable: la pantalla es solo de consulta y no muestra "Guardar". */
    val soloConsulta: Boolean get() = soloLectura && !puedeGestionar

    /** Nombre del técnico asignado (si no está en la lista se muestra su correo). */
    val nombreTecnico: String
        get() = tecnicosDisponibles
            .firstOrNull { it.correo.equals(tecnicoAsignado, ignoreCase = true) }
            ?.nombre
            ?: tecnicoAsignado

    val servicioObligatorio: Boolean get() = Catalogos.requiereServicio(categoria)

    /** Mensaje estructurado para compartir (se arma aquí y no en la pantalla). */
    val mensajeSeguimiento: String
        get() = MensajeSeguimiento.armar(codigo, titulo, categoria)
}
