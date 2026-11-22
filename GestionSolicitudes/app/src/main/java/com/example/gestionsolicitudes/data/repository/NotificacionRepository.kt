package com.example.gestionsolicitudes.data.repository

import android.content.Context
import com.example.gestionsolicitudes.util.NotificacionHelper

/** Envío de notificaciones locales; el ViewModel no necesita conocer Context ni NotificationManager. */
open class NotificacionRepository(private val context: Context) {

    open fun solicitudRegistrada(codigo: String, titulo: String) {
        NotificacionHelper.notificarSolicitudRegistrada(context, codigo, titulo)
    }
}
