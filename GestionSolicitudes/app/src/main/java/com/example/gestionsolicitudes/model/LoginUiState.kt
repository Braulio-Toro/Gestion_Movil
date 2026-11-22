package com.example.gestionsolicitudes.model

/** Estado de la pantalla de inicio de sesión. */
data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val correoError: String? = null,
    val claveError: String? = null,
    val errorGeneral: String? = null,
    /** La pantalla navega al menú y consume este evento con LoginViewModel.consumirLoginExitoso(). */
    val loginExitoso: Boolean = false
)
