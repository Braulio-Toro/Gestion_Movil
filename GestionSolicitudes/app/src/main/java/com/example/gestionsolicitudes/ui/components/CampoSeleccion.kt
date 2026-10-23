package com.example.gestionsolicitudes.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role

/** Lista desplegable con ícono inicial, ícono de error y flecha animada. */
@Composable
fun CampoSeleccion(
    valor: String,
    etiqueta: String,
    opciones: List<String>,
    onSeleccion: (String) -> Unit,
    modifier: Modifier = Modifier,
    iconoInicial: ImageVector? = null,
    error: String? = null,
    habilitado: Boolean = true
) {
    var expandido by remember { mutableStateOf(false) }
    val giro by animateFloatAsState(targetValue = if (expandido) 180f else 0f, label = "giroFlecha")

    Box(modifier = modifier) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            leadingIcon = if (iconoInicial != null) {
                { Icon(iconoInicial, contentDescription = null) }
            } else {
                null
            },
            trailingIcon = {
                if (error != null) {
                    IconoError()
                } else {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.rotate(giro))
                }
            },
            isError = error != null,
            supportingText = { Text(error ?: "") },
            modifier = Modifier.fillMaxWidth()
        )
        // Capa transparente que captura el toque para abrir el menú
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = habilitado, role = Role.DropdownList) { expandido = true }
        )
        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}
