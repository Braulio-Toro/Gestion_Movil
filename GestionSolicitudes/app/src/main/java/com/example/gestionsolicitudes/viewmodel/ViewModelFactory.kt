package com.example.gestionsolicitudes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionsolicitudes.di.AppContainer

/** Crea cada ViewModel entregándole los repositorios que necesita (sin que ellos los construyan). */
class ViewModelFactory(private val contenedor: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                LoginViewModel(contenedor.authRepository, contenedor.sesionRepository)

            modelClass.isAssignableFrom(SesionViewModel::class.java) ->
                SesionViewModel(contenedor.sesionRepository)

            modelClass.isAssignableFrom(ListaSolicitudesViewModel::class.java) ->
                ListaSolicitudesViewModel(
                    contenedor.solicitudRepository,
                    contenedor.sesionRepository,
                    contenedor.authRepository
                )

            modelClass.isAssignableFrom(SolicitudFormViewModel::class.java) ->
                SolicitudFormViewModel(
                    contenedor.solicitudRepository,
                    contenedor.sesionRepository,
                    contenedor.evidenciaRepository,
                    contenedor.notificacionRepository,
                    contenedor.authRepository
                )

            modelClass.isAssignableFrom(QrViewModel::class.java) ->
                QrViewModel(contenedor.qrRepository, contenedor.sesionRepository)

            modelClass.isAssignableFrom(PostViewModel::class.java) ->
                PostViewModel(contenedor.postRepository)

            else -> throw IllegalArgumentException("ViewModel no registrado: ${modelClass.name}")
        }
        return viewModel as T
    }
}
