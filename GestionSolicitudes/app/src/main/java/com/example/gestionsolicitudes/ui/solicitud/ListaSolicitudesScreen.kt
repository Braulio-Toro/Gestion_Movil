package com.example.gestionsolicitudes.ui.solicitud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gestionsolicitudes.data.local.SolicitudEntity
import com.example.gestionsolicitudes.model.Permisos
import com.example.gestionsolicitudes.ui.components.BarraApp
import com.example.gestionsolicitudes.ui.components.EstadoChip
import com.example.gestionsolicitudes.ui.components.Tarjeta
import com.example.gestionsolicitudes.util.Formato

@Composable
fun ListaSolicitudesScreen(
    viewModel: SolicitudViewModel,
    onNuevaSolicitud: () -> Unit,
    onAbrirSolicitud: (Long) -> Unit,
    onEscanearQr: () -> Unit,
    onVolver: () -> Unit
) {
    val estado by viewModel.lista.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val puedeCrear = estado.usuario?.let { Permisos.puedeCrear(it) } ?: false

    LaunchedEffect(Unit) {
        viewModel.mensajes.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            BarraApp(
                titulo = "Solicitudes",
                onVolver = onVolver,
                acciones = {
                    IconButton(onClick = onEscanearQr) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = "Escanear código QR")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (puedeCrear) {
                ExtendedFloatingActionButton(
                    onClick = onNuevaSolicitud,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nueva solicitud") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = estado.busqueda,
                onValueChange = viewModel::onBusquedaChange,
                label = { Text("Buscar por código, título o categoría") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when {
                estado.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                estado.solicitudes.isEmpty() -> Box(
                    Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (estado.busqueda.isBlank()) {
                            "Aún no hay solicitudes registradas."
                        } else {
                            "No hay solicitudes que coincidan con la búsqueda."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(estado.solicitudes, key = { it.id }) { solicitud ->
                        SolicitudItem(
                            solicitud = solicitud,
                            onClick = { onAbrirSolicitud(solicitud.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SolicitudItem(solicitud: SolicitudEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Tarjeta(modifier = modifier, onClick = onClick) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    solicitud.codigo,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                EstadoChip(solicitud.estado)
            }
            Text(
                solicitud.titulo,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "Categoría: ${solicitud.categoria}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                buildString {
                    append(Formato.fechaHora(solicitud.fechaCreacion))
                    if (solicitud.evidenciaUri != null) append(" · Con evidencia")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
