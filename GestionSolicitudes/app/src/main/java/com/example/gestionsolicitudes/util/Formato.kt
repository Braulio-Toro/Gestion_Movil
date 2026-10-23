package com.example.gestionsolicitudes.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formato {
    fun fechaHora(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CL")).format(Date(millis))
}
