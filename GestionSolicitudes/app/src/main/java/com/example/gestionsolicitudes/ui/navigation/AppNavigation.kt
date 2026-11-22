package com.example.gestionsolicitudes.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gestionsolicitudes.model.Usuario
import com.example.gestionsolicitudes.ui.login.LoginScreen
import com.example.gestionsolicitudes.ui.login.LoginViewModel
import com.example.gestionsolicitudes.ui.menu.MenuScreen
import com.example.gestionsolicitudes.ui.qr.QrScannerScreen
import com.example.gestionsolicitudes.ui.qr.QrViewModel
import com.example.gestionsolicitudes.ui.solicitud.ListaSolicitudesScreen
import com.example.gestionsolicitudes.ui.solicitud.SolicitudFormScreen
import com.example.gestionsolicitudes.ui.solicitud.SolicitudViewModel

object Rutas {
    const val LOGIN = "login"
    const val MENU = "menu"
    const val LISTA = "lista"
    const val QR = "qr"
    const val FORM = "form"
    const val FORM_ID = "solicitudId"
    const val FORM_RUTA = "$FORM?$FORM_ID={$FORM_ID}"

    /** id = null abre el formulario para una solicitud nueva. */
    fun form(id: Long? = null): String = "$FORM?$FORM_ID=${id ?: -1L}"
}

private const val DURACION = 350

/** Flujo: Login -> Menú -> Lista / Escáner QR -> Formulario (detalle). */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()
    val solicitudViewModel: SolicitudViewModel = viewModel()
    val qrViewModel: QrViewModel = viewModel()
    val usuario by loginViewModel.sesion.collectAsState()

    // Mantiene sincronizada la sesión con los ViewModels que dependen del perfil
    LaunchedEffect(usuario) {
        solicitudViewModel.setUsuario(usuario)
        qrViewModel.setUsuario(usuario)
    }

    val irALogin: () -> Unit = {
        navController.navigate(Rutas.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN,
        // Animaciones de transición entre pantallas (deslizamiento + desvanecimiento)
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(DURACION)) +
                fadeIn(tween(DURACION))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(DURACION)) +
                fadeOut(tween(DURACION))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(DURACION)) +
                fadeIn(tween(DURACION))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(DURACION)) +
                fadeOut(tween(DURACION))
        }
    ) {

        composable(Rutas.LOGIN) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginExitoso = {
                    navController.navigate(Rutas.MENU) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.MENU) {
            RequiereSesion(usuario, irALogin) { u ->
                MenuScreen(
                    usuario = u,
                    onSolicitudes = { navController.navigate(Rutas.LISTA) },
                    onEscanearQr = { navController.navigate(Rutas.QR) },
                    onCerrarSesion = {
                        loginViewModel.cerrarSesion()
                        irALogin()
                    }
                )
            }
        }

        composable(Rutas.LISTA) {
            RequiereSesion(usuario, irALogin) {
                ListaSolicitudesScreen(
                    viewModel = solicitudViewModel,
                    onNuevaSolicitud = { navController.navigate(Rutas.form(null)) },
                    onAbrirSolicitud = { id -> navController.navigate(Rutas.form(id)) },
                    onEscanearQr = { navController.navigate(Rutas.QR) },
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable(Rutas.QR) {
            RequiereSesion(usuario, irALogin) {
                QrScannerScreen(
                    viewModel = qrViewModel,
                    onVolver = { navController.popBackStack() },
                    onSolicitudEncontrada = { id ->
                        navController.navigate(Rutas.form(id)) {
                            popUpTo(Rutas.QR) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(
            route = Rutas.FORM_RUTA,
            arguments = listOf(
                navArgument(Rutas.FORM_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { entrada ->
            val id = entrada.arguments?.getLong(Rutas.FORM_ID) ?: -1L
            RequiereSesion(usuario, irALogin) {
                SolicitudFormScreen(
                    solicitudId = id.takeIf { it >= 0 },
                    viewModel = solicitudViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
        }
    }
}

/** Si la sesión se perdió (p. ej. el sistema recreó el proceso) se vuelve al login. */
@Composable
private fun RequiereSesion(
    usuario: Usuario?,
    onSinSesion: () -> Unit,
    contenido: @Composable (Usuario) -> Unit
) {
    if (usuario == null) {
        LaunchedEffect(Unit) { onSinSesion() }
    } else {
        contenido(usuario)
    }
}
