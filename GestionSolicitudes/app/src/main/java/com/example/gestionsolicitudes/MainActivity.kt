package com.example.gestionsolicitudes

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.gestionsolicitudes.navigation.AppNavigation
import com.example.gestionsolicitudes.ui.theme.GestionSolicitudesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // La Activity solo conoce el contenedor de dependencias; no construye repositorios
        val contenedor = (application as GestionSolicitudesApp).contenedor
        // Barras de estado con íconos claros: las cabeceras de la app usan siempre el azul de marca
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            GestionSolicitudesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModelFactory = contenedor.viewModelFactory)
                }
            }
        }
    }
}
