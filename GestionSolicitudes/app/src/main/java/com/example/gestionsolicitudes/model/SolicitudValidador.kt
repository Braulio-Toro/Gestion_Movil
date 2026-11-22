package com.example.gestionsolicitudes.model

/**
 * Reglas de validación del formulario. Son funciones puras (sin Android) para poder
 * probarlas con pruebas unitarias; el ViewModel las usa para poblar el UiState.
 */
object SolicitudValidador {
    const val TITULO_MAX = 80
    const val DESCRIPCION_MIN = 20
    const val DESCRIPCION_MAX = 500
    const val SOLUCION_MAX = 500

    fun validarTitulo(titulo: String): String? {
        val valor = titulo.trim()
        return when {
            valor.isEmpty() -> "El título es obligatorio"
            valor.length > TITULO_MAX -> "El título no puede superar los $TITULO_MAX caracteres"
            else -> null
        }
    }

    fun validarDescripcion(descripcion: String): String? {
        val valor = descripcion.trim()
        return when {
            valor.isEmpty() -> "La descripción es obligatoria"
            valor.length < DESCRIPCION_MIN ->
                "La descripción debe tener al menos $DESCRIPCION_MIN caracteres (tiene ${valor.length})"
            valor.length > DESCRIPCION_MAX -> "La descripción no puede superar los $DESCRIPCION_MAX caracteres"
            else -> null
        }
    }

    fun validarCategoria(categoria: String): String? =
        if (categoria.isBlank()) "Debes seleccionar una categoría" else null

    /** El servicio es obligatorio solo cuando la categoría lo requiere. */
    fun validarServicio(categoria: String, servicio: String): String? =
        if (Catalogos.requiereServicio(categoria) && servicio.isBlank()) {
            "Debes seleccionar el servicio relacionado"
        } else {
            null
        }
}
