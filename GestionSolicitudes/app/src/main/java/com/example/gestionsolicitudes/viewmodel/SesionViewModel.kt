package com.example.gestionsolicitudes.viewmodel

import androidx.lifecycle.ViewModel
import com.example.gestionsolicitudes.data.repository.SesionRepository
import com.example.gestionsolicitudes.model.Usuario
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel de alcance global (Activity): expone la sesión a la navegación y permite cerrarla.
 * No contiene reglas de negocio; solo traduce el repositorio de sesión a estado observable.
 */
class SesionViewModel(private val sesion: SesionRepository) : ViewModel() {

    /** Usuario con sesión iniciada (null si no hay sesión). */
    val usuario: StateFlow<Usuario?> = sesion.usuario

    fun cerrarSesion() = sesion.cerrar()
}
