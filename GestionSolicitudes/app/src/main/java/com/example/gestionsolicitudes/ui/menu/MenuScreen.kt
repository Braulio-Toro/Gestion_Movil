package com.example.gestionsolicitudes.ui.menu

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.gestionsolicitudes.model.Perfil
import com.example.gestionsolicitudes.model.Usuario
import com.example.gestionsolicitudes.ui.components.EncabezadoDegradado
import com.example.gestionsolicitudes.ui.components.Tarjeta

@Composable
fun MenuScreen(
    usuario: Usuario,
    onSolicitudes: () -> Unit,
    onEscanearQr: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current

    // Recurso nativo: permiso para mostrar notificaciones locales (Android 13+)
    val pedirNotificaciones = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            pedirNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            EncabezadoDegradado(
                icono = Icons.Filled.Build,
                titulo = "Hola, ${usuario.nombre}",
                subtitulo = "Perfil ${usuario.perfil.etiqueta} · ${usuario.empresa}"
            )

            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("¿Qué deseas hacer?", style = MaterialTheme.typography.titleLarge)

                OpcionMenu(
                    icono = Icons.AutoMirrored.Filled.List,
                    titulo = "Solicitudes",
                    descripcion = when (usuario.perfil) {
                        Perfil.CLIENTE -> "Registra y consulta tus solicitudes"
                        Perfil.ADMINISTRADOR -> "Consulta y edita todas las solicitudes"
                        Perfil.TECNICO -> "Consulta las solicitudes registradas"
                    },
                    onClick = onSolicitudes
                )

                OpcionMenu(
                    icono = Icons.Filled.QrCodeScanner,
                    titulo = "Escanear código QR",
                    descripcion = "Identifica una solicitud con la cámara y abre su detalle",
                    onClick = onEscanearQr
                )

                OutlinedButton(
                    onClick = onCerrarSesion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar sesión")
                }
            }
        }
    }
}

@Composable
private fun OpcionMenu(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    onClick: () -> Unit
) {
    Tarjeta(onClick = onClick) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                Text(
                    descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
