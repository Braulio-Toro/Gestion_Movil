package com.example.gestionsolicitudes.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.gestionsolicitudes.util.EvidenciaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private sealed interface CargaImagen {
    data object Cargando : CargaImagen
    data object Error : CargaImagen
    class Lista(val bitmap: Bitmap) : CargaImagen
}

/** Muestra la fotografía de evidencia guardada, decodificándola fuera del hilo principal. */
@Composable
fun ImagenEvidencia(uri: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val carga by produceState<CargaImagen>(initialValue = CargaImagen.Cargando, key1 = uri) {
        val bitmap = withContext(Dispatchers.IO) {
            EvidenciaUtils.cargarBitmap(context, Uri.parse(uri))
        }
        value = if (bitmap != null) CargaImagen.Lista(bitmap) else CargaImagen.Error
    }

    when (val actual = carga) {
        is CargaImagen.Lista -> Image(
            bitmap = actual.bitmap.asImageBitmap(),
            contentDescription = "Fotografía de evidencia de la solicitud",
            contentScale = ContentScale.Fit,
            modifier = modifier
        )
        CargaImagen.Cargando -> Box(modifier, contentAlignment = Alignment.Center) {
            Text("Cargando evidencia…", style = MaterialTheme.typography.bodyMedium)
        }
        CargaImagen.Error -> Box(modifier, contentAlignment = Alignment.Center) {
            Text("No se pudo cargar la evidencia", color = MaterialTheme.colorScheme.error)
        }
    }
}
