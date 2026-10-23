package com.example.gestionsolicitudes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.gestionsolicitudes.ui.theme.AzulMarca
import com.example.gestionsolicitudes.ui.theme.AzulPrimario
import com.example.gestionsolicitudes.ui.theme.Turquesa

/** Cabecera con degradado de marca usada en el login y en el menú. */
@Composable
fun EncabezadoDegradado(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier,
    contenidoExtra: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(listOf(AzulMarca, AzulPrimario, Turquesa)),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color.White.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
        }
        Text(titulo, style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.88f))
        contenidoExtra()
    }
}
