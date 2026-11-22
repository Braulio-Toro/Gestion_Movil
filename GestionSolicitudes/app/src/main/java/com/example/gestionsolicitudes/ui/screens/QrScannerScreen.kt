package com.example.gestionsolicitudes.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gestionsolicitudes.ui.components.BarraApp
import com.example.gestionsolicitudes.ui.components.QrScanner
import com.example.gestionsolicitudes.ui.components.Tarjeta
import com.example.gestionsolicitudes.ui.theme.Turquesa
import com.example.gestionsolicitudes.util.CameraPermissionHelper
import com.example.gestionsolicitudes.viewmodel.QrViewModel

@Composable
fun QrScannerScreen(
    viewModel: QrViewModel,
    onVolver: () -> Unit,
    onSolicitudEncontrada: (Long) -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current
    var tienePermiso by remember { mutableStateOf(CameraPermissionHelper.tienePermisoCamara(context)) }
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        tienePermiso = it
    }

    // Al identificar la solicitud se abre su detalle
    LaunchedEffect(estado.solicitudId) {
        estado.solicitudId?.let { id ->
            viewModel.reiniciar()
            onSolicitudEncontrada(id)
        }
    }

    Scaffold(
        topBar = { BarraApp(titulo = "Escanear código QR", onVolver = onVolver) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                !tienePermiso -> {
                    Icon(
                        Icons.Filled.QrCodeScanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Permiso de cámara requerido", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Para leer el QR de una solicitud necesitamos acceso a la cámara.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { pedirPermiso.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                    ) { Text("Conceder permiso de cámara") }
                }

                estado.mensaje != null -> {
                    Tarjeta {
                        Column(
                            Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Filled.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                estado.mensaje ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = viewModel::reiniciar,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                            ) { Text("Escanear otro código QR") }
                        }
                    }
                }

                estado.buscando || estado.solicitudId != null -> {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Buscando solicitud ${estado.codigo ?: ""}…", style = MaterialTheme.typography.bodyLarge)
                }

                else -> {
                    Text(
                        "Enfoca el código QR de una solicitud",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp)
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        QrScanner(
                            onQrCodeScanned = viewModel::onQrDetectado,
                            modifier = Modifier.fillMaxSize()
                        )
                        MarcoDeEscaneo(Modifier.align(Alignment.Center))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "El código se lee automáticamente al quedar dentro del marco.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/** Marco guía con una línea de escaneo animada. */
@Composable
private fun MarcoDeEscaneo(modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "escaneo")
    val avance by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lineaEscaneo"
    )
    Box(
        modifier = modifier
            .size(250.dp)
            .border(3.dp, Turquesa, RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .height(3.dp)
                .offset(y = (avance * 240f).dp)
                .background(Turquesa, CircleShape)
        )
    }
}
