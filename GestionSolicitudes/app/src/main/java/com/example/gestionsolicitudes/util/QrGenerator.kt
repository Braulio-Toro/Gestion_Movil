package com.example.gestionsolicitudes.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/** Genera el código QR de seguimiento de una solicitud (contenido: su código único). */
object QrGenerator {
    fun generar(contenido: String, tamano: Int = 512): Bitmap {
        val matriz = QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, tamano, tamano)
        val pixeles = IntArray(tamano * tamano)
        for (y in 0 until tamano) {
            for (x in 0 until tamano) {
                pixeles[y * tamano + x] = if (matriz.get(x, y)) Color.BLACK else Color.WHITE
            }
        }
        return Bitmap.createBitmap(pixeles, tamano, tamano, Bitmap.Config.ARGB_8888)
    }
}
