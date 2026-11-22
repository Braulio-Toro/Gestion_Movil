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
import com.example.gestionsolicitudes.model.PrioridadSolicitud

/** Etiqueta de color para la prioridad: de verde (baja) a rojo (muy alta). */
@Composable
fun PrioridadChip(prioridad: String, modifier: Modifier = Modifier) {
    val objetivo = when (prioridad) {
        PrioridadSolicitud.BAJA -> Color(0xFFC9EFC7)
        PrioridadSolicitud.MEDIA -> Color(0xFFFFF0B8)
        PrioridadSolicitud.ALTA -> Color(0xFFFFD9B0)
        PrioridadSolicitud.MUY_ALTA -> Color(0xFFFFCFCB)
        else -> Color(0xFFDADDE2)
    }
    val fondo by animateColorAsState(targetValue = objetivo, label = "colorPrioridad")
    Surface(color = fondo, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            text = "Prioridad $prioridad",
            color = Color(0xFF1B1B1F),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
