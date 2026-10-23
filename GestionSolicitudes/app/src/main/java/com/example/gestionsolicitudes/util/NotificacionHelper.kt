package com.example.gestionsolicitudes.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.gestionsolicitudes.MainActivity
import com.example.gestionsolicitudes.R

/** Recurso nativo: notificaciones locales (se omiten si el usuario no concedió el permiso). */
object NotificacionHelper {
    private const val CANAL_ID = "solicitudes"

    private fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                "Solicitudes",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Avisos sobre el registro de solicitudes" }
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(canal)
        }
    }

    @SuppressLint("MissingPermission")
    fun notificarSolicitudRegistrada(context: Context, codigo: String, titulo: String) {
        crearCanal(context)
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendiente = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val texto = "$titulo · Estado: Recibida"
        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("Solicitud $codigo registrada")
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setContentIntent(pendiente)
            .setAutoCancel(true)
            .build()
        try {
            manager.notify(codigo.hashCode(), notificacion)
        } catch (e: SecurityException) {
            // sin permiso de notificaciones: se ignora
        }
    }
}
