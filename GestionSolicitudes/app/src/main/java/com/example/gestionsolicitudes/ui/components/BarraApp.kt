package com.example.gestionsolicitudes.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.gestionsolicitudes.ui.theme.AzulMarca

/** Barra superior con los colores de marca (iconos blancos legibles en modo claro y oscuro). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraApp(
    titulo: String,
    onVolver: (() -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(titulo, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            if (onVolver != null) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        },
        actions = acciones,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AzulMarca,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}
