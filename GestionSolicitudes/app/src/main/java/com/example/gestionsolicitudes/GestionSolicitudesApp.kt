package com.example.gestionsolicitudes

import android.app.Application
import com.example.gestionsolicitudes.di.AppContainer

/** Punto de entrada de la aplicación: crea el contenedor de dependencias una sola vez. */
class GestionSolicitudesApp : Application() {

    lateinit var contenedor: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = AppContainer(this)
    }
}
