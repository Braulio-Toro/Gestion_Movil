package com.example.gestionsolicitudes.data.repository

import android.content.Context
import android.net.Uri
import com.example.gestionsolicitudes.util.EvidenciaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Acceso al almacenamiento de las fotografías de evidencia. Aísla el uso de Context,
 * FileProvider y archivos para que los ViewModels trabajen solo con textos (URI como String).
 */
open class EvidenciaRepository(private val context: Context) {

    /** Crea el archivo donde la cámara dejará la foto y devuelve su URI segura. */
    @Throws(IOException::class)
    open fun crearArchivoParaFoto(): String {
        val archivo = EvidenciaUtils.crearArchivoEvidencia(context)
        return EvidenciaUtils.uriDeArchivo(context, archivo).toString()
    }

    /** Copia una imagen de la galería al almacenamiento de la app (en hilo de IO). */
    open suspend fun copiarDesdeGaleria(origen: String): String? = withContext(Dispatchers.IO) {
        EvidenciaUtils.copiarDesdeUri(context, Uri.parse(origen))?.toString()
    }

    /** Elimina un archivo de evidencia propio (captura cancelada o solicitud eliminada). */
    open suspend fun eliminar(uri: String) = withContext(Dispatchers.IO) {
        EvidenciaUtils.eliminar(context, uri)
    }
}
