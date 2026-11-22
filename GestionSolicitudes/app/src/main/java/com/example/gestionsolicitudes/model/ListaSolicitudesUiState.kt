package com.example.gestionsolicitudes.model

/** Estado de la pantalla con el listado de solicitudes. */
data class ListaSolicitudesUiState(
    val solicitudes: List<Solicitud> = emptyList(),
    val busqueda: String = "",
    val cargando: Boolean = true,
    val usuario: Usuario? = null,
    /** Nombre de cada técnico según su correo (en minúsculas), para mostrarlo en la lista. */
    val nombresTecnicos: Map<String, String> = emptyMap()
) {
    /** Regla de negocio resuelta en el estado: la pantalla solo decide si muestra el botón. */
    val puedeCrear: Boolean get() = usuario?.let { Permisos.puedeCrear(it) } ?: false
}
