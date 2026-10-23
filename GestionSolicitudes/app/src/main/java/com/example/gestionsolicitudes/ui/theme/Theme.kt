package com.example.gestionsolicitudes.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB4C3FF),
    onPrimary = Color(0xFF0D2175),
    primaryContainer = Color(0xFF2B3F94),
    onPrimaryContainer = Color(0xFFDDE3FF),
    secondary = Color(0xFF6FD9D3),
    onSecondary = Color(0xFF00403D),
    secondaryContainer = Color(0xFF0B5F5B),
    onSecondaryContainer = Color(0xFFB8F5F1),
    tertiary = Color(0xFFFFB877),
    onTertiary = Color(0xFF4A2800),
    background = FondoOscuro,
    onBackground = TextoOscuro,
    surface = SuperficieOscura,
    onSurface = TextoOscuro,
    surfaceVariant = VarianteOscura,
    onSurfaceVariant = TextoSecundarioOscuro,
    outline = Color(0xFF8B92AD),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = AzulPrimario,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE3FF),
    onPrimaryContainer = Color(0xFF0A1B5C),
    secondary = TurquesaOscuro,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC6F1EE),
    onSecondaryContainer = Color(0xFF00302E),
    tertiary = AcentoNaranja,
    onTertiary = Color.White,
    background = FondoClaro,
    onBackground = TextoClaro,
    surface = SuperficieClara,
    onSurface = TextoClaro,
    surfaceVariant = VarianteClara,
    onSurfaceVariant = TextoSecundarioClaro,
    outline = Color(0xFF747B96),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun GestionSolicitudesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desactivado por defecto para mantener la identidad de color de la empresa
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
