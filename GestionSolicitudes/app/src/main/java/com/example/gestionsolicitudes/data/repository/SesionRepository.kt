package com.example.gestionsolicitudes.data.repository

import com.example.gestionsolicitudes.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fuente única de la sesión activa. Los ViewModels que dependen del perfil la observan
 * directamente, de modo que ya no hay que "empujarles" el usuario desde la navegación.
 */
class SesionRepository {

    private val _usuario = MutableStateFlow<Usuario?>(null)

    /** Usuario con sesión iniciada (null si no hay sesión). */
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    fun iniciar(usuario: Usuario) {
        _usuario.value = usuario
    }

    fun cerrar() {
        _usuario.value = null
    }
}
