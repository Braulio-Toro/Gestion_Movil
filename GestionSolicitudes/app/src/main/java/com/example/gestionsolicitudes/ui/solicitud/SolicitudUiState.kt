package com.example.gestionsolicitudes.ui.solicitud

import com.example.gestionsolicitudes.data.local.SolicitudEntity
import com.example.gestionsolicitudes.model.EstadoSolicitud
import com.example.gestionsolicitudes.model.Usuario

/** Estado del formulario de solicitud (crear / consultar / editar). */
data class SolicitudUiState(
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

    val tituloError: String? = null,
    val descripcionError: String? = null,
    val categoriaError: String? = null,
    val servicioError: String? = null,
    val mensajeError: String? = null,

    val soloLectura: Boolean = false,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false
) {
    val esEdicion: Boolean get() = solicitudId != null
}

/** Estado de la pantalla con el listado de solicitudes. */
data class ListaSolicitudesUiState(
    val solicitudes: List<SolicitudEntity> = emptyList(),
    val busqueda: String = "",
    val cargando: Boolean = true,
    val usuario: Usuario? = null
)
