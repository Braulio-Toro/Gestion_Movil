package com.example.gestionsolicitudes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionsolicitudes.data.repository.AuthRepository
import com.example.gestionsolicitudes.data.repository.SesionRepository
import com.example.gestionsolicitudes.data.repository.SolicitudRepository
import com.example.gestionsolicitudes.model.ListaSolicitudesUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** ViewModel de la pantalla de listado: observa Room según el perfil y aplica la búsqueda. */
class ListaSolicitudesViewModel(
    private val repositorio: SolicitudRepository,
    sesion: SesionRepository,
    autenticacion: AuthRepository
) : ViewModel() {

    private val _busqueda = MutableStateFlow("")

    /** Correo (en minúsculas) → nombre de cada técnico, para mostrar quién atiende cada solicitud. */
    private val nombresTecnicos: Map<String, String> =
        autenticacion.tecnicos.associate { it.correo.lowercase() to it.nombre }

    /** Lista reactiva: se actualiza sola cuando Room registra o modifica una solicitud. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val estado: StateFlow<ListaSolicitudesUiState> = combine(
        sesion.usuario.flatMapLatest { usuario ->
            repositorio.observarSolicitudes(usuario).map { solicitudes -> usuario to solicitudes }
        },
        _busqueda
    ) { (usuario, solicitudes), busqueda ->
        val texto = busqueda.trim()
        val filtradas = if (texto.isEmpty()) {
            solicitudes
        } else {
            solicitudes.filter {
                it.codigo.contains(texto, ignoreCase = true) ||
                    it.titulo.contains(texto, ignoreCase = true) ||
                    it.categoria.contains(texto, ignoreCase = true)
            }
        }
        ListaSolicitudesUiState(
            solicitudes = filtradas,
            busqueda = busqueda,
            cargando = false,
            usuario = usuario,
            nombresTecnicos = nombresTecnicos
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListaSolicitudesUiState())

    fun onBusquedaChange(valor: String) {
        _busqueda.value = valor
    }
}
