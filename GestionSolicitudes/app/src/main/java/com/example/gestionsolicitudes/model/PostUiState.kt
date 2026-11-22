package com.example.gestionsolicitudes.model

/** Estado de la pantalla con el listado de publicaciones de la API REST. */
data class PostUiState(
    val posts: List<Post> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null
)
