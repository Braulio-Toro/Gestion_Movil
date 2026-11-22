package com.example.gestionsolicitudes.di

import android.content.Context
import com.example.gestionsolicitudes.data.local.AppDatabase
import com.example.gestionsolicitudes.data.repository.AuthRepository
import com.example.gestionsolicitudes.data.repository.EvidenciaRepository
import com.example.gestionsolicitudes.data.repository.NotificacionRepository
import com.example.gestionsolicitudes.data.repository.PostRepository
import com.example.gestionsolicitudes.data.repository.QrRepository
import com.example.gestionsolicitudes.data.repository.SesionRepository
import com.example.gestionsolicitudes.data.repository.SolicitudRepository
import com.example.gestionsolicitudes.viewmodel.ViewModelFactory

/**
 * Contenedor de dependencias (inyección manual). Es el único lugar que construye
 * base de datos, repositorios y la fábrica de ViewModels; vive mientras viva la aplicación.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val baseDeDatos by lazy { AppDatabase.getDatabase(appContext) }

    val sesionRepository = SesionRepository()
    val authRepository = AuthRepository()
    val postRepository by lazy { PostRepository() }
    val evidenciaRepository = EvidenciaRepository(appContext)
    val notificacionRepository = NotificacionRepository(appContext)
    val solicitudRepository by lazy { SolicitudRepository(baseDeDatos.solicitudDao()) }
    val qrRepository by lazy { QrRepository(solicitudRepository) }

    val viewModelFactory = ViewModelFactory(this)
}
