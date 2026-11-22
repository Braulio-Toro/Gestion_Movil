package com.example.gestionsolicitudes.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gestionsolicitudes.model.EstadoSolicitud

/** Etiqueta de color para diferenciar cada estado; el color cambia con animación. */
@Composable
fun EstadoChip(estado: String, modifier: Modifier = Modifier) {
    val objetivo = when (estado) {
        EstadoSolicitud.RECIBIDA -> Color(0xFFD6E6FF)
        EstadoSolicitud.EN_REVISION -> Color(0xFFFFF0B8)
        EstadoSolicitud.EN_PROCESO -> Color(0xFFBFEFEA)
        EstadoSolicitud.RESUELTA -> Color(0xFFC9EFC7)
        EstadoSolicitud.CERRADA -> Color(0xFFDADDE2)
        EstadoSolicitud.CANCELADA -> Color(0xFFFFCFCB)
        else -> Color(0xFFDADDE2)
    }
    val fondo by animateColorAsState(targetValue = objetivo, label = "colorEstado")
    Surface(color = fondo, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            text = estado,
            color = Color(0xFF1B1B1F),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
