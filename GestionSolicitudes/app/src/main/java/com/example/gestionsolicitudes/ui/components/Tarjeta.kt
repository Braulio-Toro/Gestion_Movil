package com.example.gestionsolicitudes.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Tarjeta redondeada con elevación suave; clicable si se entrega onClick. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val forma = RoundedCornerShape(18.dp)
    val colores = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val elevacion = CardDefaults.cardElevation(defaultElevation = 3.dp)
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = forma,
            colors = colores,
            elevation = elevacion,
            content = content
        )
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = forma,
            colors = colores,
            elevation = elevacion,
            content = content
        )
    }
}

/** Ícono que acompaña a los campos con error de validación. */
@Composable
fun IconoError() {
    Icon(
        imageVector = Icons.Filled.Error,
        contentDescription = "Campo con error",
        tint = MaterialTheme.colorScheme.error
    )
}
