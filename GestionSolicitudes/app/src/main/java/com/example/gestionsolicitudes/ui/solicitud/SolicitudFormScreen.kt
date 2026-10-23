package com.example.gestionsolicitudes.ui.solicitud

import android.Manifest
import android.content.ActivityNotFoundException
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
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
import com.example.gestionsolicitudes.model.MensajeSeguimiento
import com.example.gestionsolicitudes.model.SolicitudValidador
import com.example.gestionsolicitudes.ui.components.BarraApp
import com.example.gestionsolicitudes.ui.components.CampoSeleccion
import com.example.gestionsolicitudes.ui.components.EstadoChip
import com.example.gestionsolicitudes.ui.components.IconoError
import com.example.gestionsolicitudes.ui.components.ImagenEvidencia
import com.example.gestionsolicitudes.ui.components.Tarjeta
import com.example.gestionsolicitudes.util.CameraPermissionHelper
import com.example.gestionsolicitudes.util.CompartirUtils
import com.example.gestionsolicitudes.util.EvidenciaUtils
import com.example.gestionsolicitudes.util.Formato
import com.example.gestionsolicitudes.util.QrGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
fun SolicitudFormScreen(
    solicitudId: Long?,
    viewModel: SolicitudViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.formulario.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    // Sobrevive a la recreación de la actividad mientras la cámara está abierta
    var uriPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(solicitudId) { viewModel.prepararFormulario(solicitudId) }
    LaunchedEffect(estado.guardadoExitosamente) {
        if (estado.guardadoExitosamente) {
            viewModel.cerrarFormulario()
            onVolver()
        }
    }

    val cerrar: () -> Unit = {
        viewModel.cerrarFormulario()
        onVolver()
    }
    BackHandler(onBack = cerrar)

    fun mostrarMensaje(texto: String) {
        scope.launch { snackbarHostState.showSnackbar(texto) }
    }

    // Recurso nativo: cámara del dispositivo (fotografía ficticia como evidencia)
    val tomarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val pendiente = uriPendiente
        uriPendiente = null
        if (pendiente != null) {
            if (exito) {
                viewModel.onEvidenciaChange(pendiente)
            } else {
                EvidenciaUtils.eliminar(context, pendiente)
            }
        }
    }

    // Alternativa: elegir una imagen ficticia desde la galería
    val elegirImagen = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val copia = withContext(Dispatchers.IO) { EvidenciaUtils.copiarDesdeUri(context, uri) }
                if (copia != null) {
                    viewModel.onEvidenciaChange(copia.toString())
                } else {
                    snackbarHostState.showSnackbar("No se pudo adjuntar la imagen seleccionada")
                }
            }
        }
    }

    fun abrirCamara() {
        try {
            val archivo = EvidenciaUtils.crearArchivoEvidencia(context)
            val uri = EvidenciaUtils.uriDeArchivo(context, archivo)
            uriPendiente = uri.toString()
            tomarFoto.launch(uri)
        } catch (e: ActivityNotFoundException) {
            uriPendiente?.let { EvidenciaUtils.eliminar(context, it) }
            uriPendiente = null
            mostrarMensaje("No hay una aplicación de cámara disponible en este dispositivo")
        } catch (e: IOException) {
            mostrarMensaje("No se pudo preparar el archivo para la fotografía")
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
        estado.esEdicion && estado.soloLectura -> "Detalle de solicitud"
        estado.esEdicion -> "Editar solicitud"
        else -> "Nueva solicitud"
    }
    val servicioObligatorio = Catalogos.requiereServicio(estado.categoria)
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
                        "Modo consulta: no puedes modificar esta solicitud.",
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
                                    val mensaje = MensajeSeguimiento.armar(estado.codigo, estado.titulo, estado.categoria)
                                    if (!CompartirUtils.compartirPorWhatsApp(context, mensaje)) {
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

                if (!estado.soloLectura) {
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
            }
        }
    }
}
