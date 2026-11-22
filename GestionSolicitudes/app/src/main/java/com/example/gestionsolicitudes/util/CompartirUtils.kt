package com.example.gestionsolicitudes.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent

/** Recurso nativo: compartir un mensaje estructurado abriendo WhatsApp (sin integración productiva). */
object CompartirUtils {

    private val paquetesWhatsApp = listOf("com.whatsapp", "com.whatsapp.w4b")

    /** Devuelve false si no existe ninguna aplicación capaz de recibir el mensaje. */
    fun compartirPorWhatsApp(context: Context, mensaje: String): Boolean {
        val base = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, mensaje)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        for (paquete in paquetesWhatsApp) {
            try {
                context.startActivity(Intent(base).setPackage(paquete))
                return true
            } catch (e: ActivityNotFoundException) {
                // se intenta con la siguiente alternativa
            }
        }
        return try {
            val selector = Intent.createChooser(base, "Compartir solicitud")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(selector)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
