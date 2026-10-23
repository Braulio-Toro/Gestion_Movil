package com.example.gestionsolicitudes.ui.login

import androidx.lifecycle.ViewModel
import com.example.gestionsolicitudes.data.repository.AuthRepository
import com.example.gestionsolicitudes.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val correoError: String? = null,
    val claveError: String? = null,
    val errorGeneral: String? = null
)

class LoginViewModel : ViewModel() {

    private val repositorio = AuthRepository()

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    private val _sesion = MutableStateFlow<Usuario?>(null)
    /** Usuario con sesión iniciada (null si no hay sesión). */
    val sesion: StateFlow<Usuario?> = _sesion.asStateFlow()

    val cuentasDemo: List<Usuario> = repositorio.cuentasDemo

    fun onCorreoChange(valor: String) {
        _estado.update { it.copy(correo = valor, correoError = null, errorGeneral = null) }
    }

    fun onClaveChange(valor: String) {
        _estado.update { it.copy(clave = valor, claveError = null, errorGeneral = null) }
    }

    fun usarCuenta(usuario: Usuario) {
        _estado.value = LoginUiState(correo = usuario.correo, clave = usuario.clave)
    }

    fun iniciarSesion(onExito: () -> Unit) {
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

        val usuario = repositorio.autenticar(actual.correo, actual.clave)
        if (usuario == null) {
            _estado.update { it.copy(errorGeneral = "Correo o contraseña incorrectos") }
            return
        }
        _sesion.value = usuario
        _estado.value = LoginUiState()
        onExito()
    }

    fun cerrarSesion() {
        _sesion.value = null
        _estado.value = LoginUiState()
    }
}
