package com.example.gestionsolicitudes.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gestionsolicitudes.ui.components.EncabezadoDegradado
import com.example.gestionsolicitudes.ui.components.IconoError
import com.example.gestionsolicitudes.ui.components.Tarjeta

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginExitoso: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    var verClave by remember { mutableStateOf(false) }

    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            EncabezadoDegradado(
                icono = Icons.Filled.Build,
                titulo = "Gestión de solicitudes",
                subtitulo = "Registra y sigue tus requerimientos de servicios informáticos y digitales"
            )

            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Iniciar sesión", style = MaterialTheme.typography.titleLarge)

                OutlinedTextField(
                    value = estado.correo,
                    onValueChange = viewModel::onCorreoChange,
                    label = { Text("Correo electrónico") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                    trailingIcon = if (estado.correoError != null) {
                        { IconoError() }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    isError = estado.correoError != null,
                    supportingText = { Text(estado.correoError ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estado.clave,
                    onValueChange = viewModel::onClaveChange,
                    label = { Text("Contraseña") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    trailingIcon = {
                        if (estado.claveError != null) {
                            IconoError()
                        } else {
                            IconButton(onClick = { verClave = !verClave }) {
                                Icon(
                                    if (verClave) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (verClave) "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        }
                    },
                    visualTransformation = if (verClave) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    isError = estado.claveError != null,
                    supportingText = { Text(estado.claveError ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                AnimatedVisibility(
                    visible = estado.errorGeneral != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        estado.errorGeneral ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = { viewModel.iniciarSesion(onLoginExitoso) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                ) {
                    Text("Ingresar")
                }

                Tarjeta(modifier = Modifier.padding(top = 8.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Cuentas de prueba (toca una para completar)",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        viewModel.cuentasDemo.forEach { cuenta ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clickable { viewModel.usarCuenta(cuenta) }
                                    .padding(vertical = 6.dp)
                            ) {
                                Text(
                                    "${cuenta.perfil.etiqueta}: ${cuenta.correo}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text("Contraseña: ${cuenta.clave}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
