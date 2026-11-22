package com.example.gestionsolicitudes.ui.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.gestionsolicitudes.model.Catalogos
import com.example.gestionsolicitudes.model.EstadoSolicitud
import com.example.gestionsolicitudes.model.PrioridadSolicitud
import com.example.gestionsolicitudes.model.SolicitudValidador
import com.example.gestionsolicitudes.ui.components.BarraApp
import com.example.gestionsolicitudes.ui.components.CampoSeleccion
import com.example.gestionsolicitudes.ui.components.EstadoChip
import com.example.gestionsolicitudes.ui.components.IconoError
import com.example.gestionsolicitudes.ui.components.ImagenEvidencia
import com.example.gestionsolicitudes.ui.components.Tarjeta
import com.example.gestionsolicitudes.util.CameraPermissionHelper
import com.example.gestionsolicitudes.util.CompartirUtils
import com.example.gestionsolicitudes.util.Formato
import com.example.gestionsolicitudes.util.QrGenerator
import com.example.gestionsolicitudes.viewmodel.SolicitudFormViewModel
import kotlinx.coroutines.launch

@Composable
fun SolicitudFormScreen(
    solicitudId: Long?,
    viewModel: SolicitudFormViewModel,
    /** Se invoca al guardar o eliminar con éxito; recibe el texto de confirmación. */
    onFinalizado: (String?) -> Unit,
    onVolver: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    // Sobrevive a la recreación de la actividad mientras la cámara está abierta
    var uriPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmarEliminar by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(solicitudId) { viewModel.cargar(solicitudId) }
    LaunchedEffect(estado.guardadoExitosamente, estado.eliminadoExitosamente) {
        if (estado.guardadoExitosamente || estado.eliminadoExitosamente) {
            onFinalizado(estado.mensajeConfirmacion)
        }
    }

    val cerrar: () -> Unit = onVolver

    fun mostrarMensaje(texto: String) {
        scope.launch { snackbarHostState.showSnackbar(texto) }
    }

    // Recurso nativo: cámara del dispositivo (fotografía ficticia como evidencia)
    val tomarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val pendiente = uriPendiente
        uriPendiente = null
        if (pendiente != null) viewModel.onFotoResultado(pendiente, exito)
    }

    // Alternativa: elegir una imagen ficticia desde la galería
    val elegirImagen = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onImagenGaleria(uri.toString())
    }

    fun abrirCamara() {
        // El ViewModel reserva el archivo; la pantalla solo lanza la cámara del sistema
        val uri = viewModel.prepararFoto() ?: return
        uriPendiente = uri
        try {
            tomarFoto.launch(Uri.parse(uri))
        } catch (e: ActivityNotFoundException) {
            viewModel.descartarFoto(uri)
            uriPendiente = null
            mostrarMensaje("No hay una aplicación de cámara disponible en este dispositivo")
        }
    }

    // Como la app declara el permiso CAMERA, Android exige concederlo antes de abrir la cámara
    val pedirPermisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) {
            abrirCamara()
        } else {
            mostrarMensaje("Sin permiso de cámara no se puede tomar la fotografía")
        }
    }

    fun solicitarCamara() {
        if (CameraPermissionHelper.tienePermisoCamara(context)) {
            abrirCamara()
        } else {
            pedirPermisoCamara.launch(Manifest.permission.CAMERA)
        }
    }

    val titulo = when {
        estado.esEdicion && estado.soloConsulta -> "Detalle de solicitud"
        estado.esEdicion && estado.soloLectura -> "Gestionar solicitud"
        estado.esEdicion -> "Editar solicitud"
        else -> "Nueva solicitud"
    }
    val servicioObligatorio = estado.servicioObligatorio
    val evidencia = estado.evidenciaUri

    Scaffold(
        topBar = { BarraApp(titulo = titulo, onVolver = cerrar) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (estado.cargando) {
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .imePadding()
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (estado.esEdicion && estado.codigo.isNotEmpty()) {
                    Tarjeta {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    estado.codigo,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                EstadoChip(estado.estado)
                            }
                            estado.fechaCreacion?.let {
                                Text("Creada: ${Formato.fechaHora(it)}", style = MaterialTheme.typography.bodyMedium)
                            }
                            estado.fechaActualizacion?.let {
                                Text(
                                    "Última actualización: ${Formato.fechaHora(it)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = estado.soloLectura && estado.mensajeError == null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        if (estado.puedeGestionar) {
                            "Los datos de la solicitud son de solo lectura. Puedes actualizar los campos de gestión."
                        } else {
                            "Modo consulta: no puedes modificar esta solicitud."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                AnimatedVisibility(
                    visible = estado.mensajeError != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        estado.mensajeError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Tarjeta {
                    Column(
                        modifier = Modifier
                            .animateContentSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Datos de la solicitud", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))

                        OutlinedTextField(
                            value = estado.titulo,
                            onValueChange = viewModel::onTituloChange,
                            label = { Text("Título *") },
                            singleLine = true,
                            readOnly = estado.soloLectura,
                            leadingIcon = { Icon(Icons.Filled.Title, contentDescription = null) },
                            trailingIcon = if (estado.tituloError != null) {
                                { IconoError() }
                            } else {
                                null
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            isError = estado.tituloError != null,
                            supportingText = { Text(estado.tituloError ?: "") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = estado.descripcion,
                            onValueChange = viewModel::onDescripcionChange,
                            label = { Text("Descripción *") },
                            minLines = 4,
                            maxLines = 8,
                            readOnly = estado.soloLectura,
                            leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null) },
                            trailingIcon = if (estado.descripcionError != null) {
                                { IconoError() }
                            } else {
                                null
                            },
                            isError = estado.descripcionError != null,
                            supportingText = {
                                Text(
                                    estado.descripcionError
                                        ?: "${estado.descripcion.length}/${SolicitudValidador.DESCRIPCION_MAX} · mínimo ${SolicitudValidador.DESCRIPCION_MIN} caracteres"
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        CampoSeleccion(
                            valor = estado.categoria,
                            etiqueta = "Categoría *",
                            opciones = Catalogos.categorias,
                            onSeleccion = viewModel::onCategoriaChange,
                            iconoInicial = Icons.Filled.Category,
                            error = estado.categoriaError,
                            habilitado = !estado.soloLectura,
                            modifier = Modifier.fillMaxWidth()
                        )

                        CampoSeleccion(
                            valor = estado.servicio,
                            etiqueta = if (servicioObligatorio) "Servicio relacionado *" else "Servicio relacionado (opcional)",
                            opciones = Catalogos.servicios,
                            onSeleccion = viewModel::onServicioChange,
                            iconoInicial = Icons.Filled.Settings,
                            error = estado.servicioError,
                            habilitado = !estado.soloLectura,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Prioridad, técnico, estado y solución (solo solicitudes ya registradas)
                if (estado.esEdicion && estado.codigo.isNotEmpty()) {
                    Tarjeta {
                        Column(
                            modifier = Modifier
                                .animateContentSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Gestión de la solicitud", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))

                            // Prioridad y técnico: solo el administrador
                            CampoSeleccion(
                                valor = estado.prioridad.ifBlank { PrioridadSolicitud.SIN_ASIGNAR },
                                etiqueta = "Prioridad",
                                opciones = PrioridadSolicitud.todas,
                                onSeleccion = viewModel::onPrioridadChange,
                                iconoInicial = Icons.Filled.Flag,
                                habilitado = estado.puedeAsignar,
                                modifier = Modifier.fillMaxWidth()
                            )

                            CampoSeleccion(
                                valor = estado.nombreTecnico.ifBlank { PrioridadSolicitud.SIN_ASIGNAR },
                                etiqueta = "Técnico asignado",
                                opciones = estado.tecnicosDisponibles.map { it.nombre },
                                onSeleccion = viewModel::onTecnicoSeleccionado,
                                iconoInicial = Icons.Filled.Engineering,
                                habilitado = estado.puedeAsignar,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Estado: administrador y técnico asignado
                            CampoSeleccion(
                                valor = estado.estado,
                                etiqueta = "Estado",
                                opciones = EstadoSolicitud.todos,
                                onSeleccion = viewModel::onEstadoChange,
                                iconoInicial = Icons.Filled.Autorenew,
                                habilitado = estado.puedeCambiarEstado,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Solución: solo la escribe el técnico asignado; el resto la consulta
                            OutlinedTextField(
                                value = estado.solucion,
                                onValueChange = viewModel::onSolucionChange,
                                label = { Text("Solución") },
                                minLines = 3,
                                maxLines = 8,
                                readOnly = !estado.puedeEscribirSolucion,
                                leadingIcon = { Icon(Icons.Filled.TaskAlt, contentDescription = null) },
                                supportingText = {
                                    Text(
                                        when {
                                            estado.puedeEscribirSolucion ->
                                                "${estado.solucion.length}/${SolicitudValidador.SOLUCION_MAX}"
                                            estado.solucion.isBlank() -> "Aún no hay una solución registrada."
                                            else -> ""
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Tarjeta {
                    Column(
                        modifier = Modifier
                            .animateContentSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Evidencia (imagen ficticia)", style = MaterialTheme.typography.titleMedium)

                        AnimatedVisibility(
                            visible = evidencia != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            if (evidencia != null) {
                                ImagenEvidencia(
                                    uri = evidencia,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 120.dp, max = 260.dp)
                                )
                            }
                        }
                        if (evidencia == null) {
                            Text(
                                "Sin evidencia adjunta",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!estado.soloLectura) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { solicitarCamara() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                ) {
                                    Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (evidencia != null) "Repetir foto" else "Tomar foto")
                                }
                                OutlinedButton(
                                    onClick = {
                                        elegirImagen.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                ) {
                                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Galería")
                                }
                            }
                            if (evidencia != null) {
                                TextButton(onClick = { viewModel.onEvidenciaChange(null) }) {
                                    Text("Quitar evidencia")
                                }
                            }
                        }
                    }
                }

                // Código QR de seguimiento + mensaje estructurado para WhatsApp (solo solicitudes ya guardadas)
                if (estado.esEdicion && estado.codigo.isNotEmpty()) {
                    Tarjeta {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Código QR de seguimiento", style = MaterialTheme.typography.titleMedium)
                            val qr = remember(estado.codigo) { QrGenerator.generar(estado.codigo, 512).asImageBitmap() }
                            Image(
                                bitmap = qr,
                                contentDescription = "Código QR de la solicitud ${estado.codigo}",
                                modifier = Modifier
                                    .size(180.dp)
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .padding(6.dp)
                            )
                            Text(
                                "Escanéalo desde \"Escanear código QR\" para abrir esta solicitud.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = {
                                    if (!CompartirUtils.compartirPorWhatsApp(context, estado.mensajeSeguimiento)) {
                                        mostrarMensaje("No hay ninguna aplicación disponible para compartir")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Compartir por WhatsApp")
                            }
                        }
                    }
                }

                if (!estado.soloConsulta) {
                    Button(
                        onClick = viewModel::guardar,
                        enabled = !estado.guardando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .animateContentSize()
                    ) {
                        if (estado.guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(if (estado.esEdicion) "Guardar cambios" else "Registrar solicitud")
                    }
                    OutlinedButton(
                        onClick = cerrar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                    ) {
                        Text("Cancelar")
                    }
                }

                // D del CRUD: solo técnico y administrador (el cliente no ve este botón)
                if (estado.esEdicion && estado.puedeEliminar) {
                    OutlinedButton(
                        onClick = { confirmarEliminar = true },
                        enabled = !estado.eliminando,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                    ) {
                        if (estado.eliminando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Eliminar solicitud")
                    }
                }
            }
        }
    }

    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("¿Eliminar solicitud?") },
            text = { Text("Se eliminará ${estado.codigo} de forma permanente. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEliminar = false
                    viewModel.eliminar()
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") }
            }
        )
    }
}
