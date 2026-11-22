package com.example.gestionsolicitudes.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

/** Manejo de las fotografías de evidencia (recurso nativo: cámara del dispositivo). */
object EvidenciaUtils {
    private const val DIRECTORIO = "evidencias"

    private fun directorio(context: Context): File =
        File(context.filesDir, DIRECTORIO).apply { mkdirs() }

    /** Crea un archivo vacío donde la cámara dejará la fotografía. */
    @Throws(IOException::class)
    fun crearArchivoEvidencia(context: Context): File {
        val marca = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File.createTempFile("EVID_${marca}_", ".jpg", directorio(context))
    }

    fun uriDeArchivo(context: Context, archivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

    /** Copia una imagen elegida desde la galería al almacenamiento propio de la app. */
    fun copiarDesdeUri(context: Context, origen: Uri): Uri? {
        val archivo = try {
            crearArchivoEvidencia(context)
        } catch (e: IOException) {
            return null
        }
        return try {
            val entrada = context.contentResolver.openInputStream(origen)
            if (entrada == null) {
                archivo.delete()
                null
            } else {
                entrada.use { input -> archivo.outputStream().use { salida -> input.copyTo(salida) } }
                uriDeArchivo(context, archivo)
            }
        } catch (e: Exception) {
            archivo.delete()
            null
        }
    }

    /** Elimina un archivo de evidencia propio (por ejemplo, si la captura fue cancelada). */
    fun eliminar(context: Context, uriTexto: String) {
        val nombre = Uri.parse(uriTexto).lastPathSegment ?: return
        File(directorio(context), nombre).delete()
    }

    /** Decodifica la imagen reducida (evita agotar memoria) y corrige su orientación. */
    fun cargarBitmap(context: Context, uri: Uri, maxLado: Int = 1280): Bitmap? {
        return try {
            val resolver = context.contentResolver
            val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }

            var muestra = 1
            val mayor = max(limites.outWidth, limites.outHeight)
            while (mayor / muestra > maxLado) muestra *= 2

            val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
            val bitmap = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opciones)
            } ?: return null

            val rotacion = try {
                resolver.openInputStream(uri)?.use { leerRotacion(it) } ?: 0
            } catch (e: Exception) {
                0
            }
            if (rotacion == 0) {
                bitmap
            } else {
                val matriz = Matrix().apply { postRotate(rotacion.toFloat()) }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun leerRotacion(entrada: InputStream): Int {
        val exif = ExifInterface(entrada)
        return when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }
}
