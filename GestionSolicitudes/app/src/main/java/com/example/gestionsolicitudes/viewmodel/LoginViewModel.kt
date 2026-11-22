package com.example.gestionsolicitudes.viewmodel

import androidx.lifecycle.ViewModel
import com.example.gestionsolicitudes.data.repository.AuthRepository
import com.example.gestionsolicitudes.data.repository.SesionRepository
import com.example.gestionsolicitudes.model.LoginUiState
import com.example.gestionsolicitudes.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel(
    private val autenticacion: AuthRepository,
    private val sesion: SesionRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    val cuentasDemo: List<Usuario> = autenticacion.cuentasDemo

    fun onCorreoChange(valor: String) {
        _estado.update { it.copy(correo = valor, correoError = null, errorGeneral = null) }
    }

    fun onClaveChange(valor: String) {
        _estado.update { it.copy(clave = valor, claveError = null, errorGeneral = null) }
    }

    fun usarCuenta(usuario: Usuario) {
        _estado.value = LoginUiState(correo = usuario.correo, clave = usuario.clave)
    }

    /** Valida, autentica y deja la sesión en el repositorio; el resultado queda en el UiState. */
    fun iniciarSesion() {
        val actual = _estado.value
        val correoError = when {
            actual.correo.isBlank() -> "El correo es obligatorio"
            !actual.correo.contains("@") -> "Correo inválido"
            else -> null
        }
        val claveError = if (actual.clave.isBlank()) "La contraseña es obligatoria" else null
        if (correoError != null || claveError != null) {
            _estado.update { it.copy(correoError = correoError, claveError = claveError) }
            return
        }

        val usuario = autenticacion.autenticar(actual.correo, actual.clave)
        if (usuario == null) {
            _estado.update { it.copy(errorGeneral = "Correo o contraseña incorrectos") }
            return
        }
        sesion.iniciar(usuario)
        _estado.value = LoginUiState(loginExitoso = true)
    }

    /** La pantalla ya navegó al menú: se limpia el evento de un solo uso. */
    fun consumirLoginExitoso() {
        _estado.update { it.copy(loginExitoso = false) }
    }
}
