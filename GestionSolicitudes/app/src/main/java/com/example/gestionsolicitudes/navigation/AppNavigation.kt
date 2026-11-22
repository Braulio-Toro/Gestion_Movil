package com.example.gestionsolicitudes.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gestionsolicitudes.model.Usuario
import com.example.gestionsolicitudes.ui.screens.ListaSolicitudesScreen
import com.example.gestionsolicitudes.ui.screens.LoginScreen
import com.example.gestionsolicitudes.ui.screens.MenuScreen
import com.example.gestionsolicitudes.ui.screens.PostScreen
import com.example.gestionsolicitudes.ui.screens.QrScannerScreen
import com.example.gestionsolicitudes.ui.screens.SolicitudFormScreen
import com.example.gestionsolicitudes.viewmodel.ListaSolicitudesViewModel
import com.example.gestionsolicitudes.viewmodel.LoginViewModel
import com.example.gestionsolicitudes.viewmodel.PostViewModel
import com.example.gestionsolicitudes.viewmodel.QrViewModel
import com.example.gestionsolicitudes.viewmodel.SesionViewModel
import com.example.gestionsolicitudes.viewmodel.SolicitudFormViewModel

object Rutas {
    const val LOGIN = "login"
    const val MENU = "menu"
    const val LISTA = "lista"
    const val QR = "qr"
    const val POSTS = "posts"
    const val FORM = "form"
    const val FORM_ID = "solicitudId"
    const val FORM_RUTA = "$FORM?$FORM_ID={$FORM_ID}"

    /** Clave con la que el formulario le deja un mensaje de confirmación a la pantalla anterior. */
    const val MENSAJE = "mensaje"

    /** id = null abre el formulario para una solicitud nueva. */
    fun form(id: Long? = null): String = "$FORM?$FORM_ID=${id ?: -1L}"
}

private const val DURACION = 350

/**
 * Flujo: Login -> Menú -> Lista / Escáner QR -> Formulario (detalle).
 *
 * Cada pantalla obtiene su propio ViewModel (alcance: su entrada del back stack) desde la
 * fábrica; la navegación ya no crea ViewModels ni les "empuja" la sesión.
 */
@Composable
fun AppNavigation(viewModelFactory: ViewModelProvider.Factory) {
    val navController = rememberNavController()
    val sesionViewModel: SesionViewModel = viewModel(factory = viewModelFactory)
    val usuario by sesionViewModel.usuario.collectAsState()

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
                viewModel = viewModel<LoginViewModel>(factory = viewModelFactory),
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
                    onPosts = { navController.navigate(Rutas.POSTS) },
                    onCerrarSesion = {
                        sesionViewModel.cerrarSesion()
                        irALogin()
                    }
                )
            }
        }

        composable(Rutas.LISTA) { entrada ->
            // Mensaje que deja el formulario al guardar o eliminar (se lee y se limpia aquí)
            val mensaje by entrada.savedStateHandle
                .getStateFlow<String?>(Rutas.MENSAJE, null)
                .collectAsState()

            RequiereSesion(usuario, irALogin) {
                ListaSolicitudesScreen(
                    viewModel = viewModel<ListaSolicitudesViewModel>(factory = viewModelFactory),
                    mensaje = mensaje,
                    onMensajeMostrado = { entrada.savedStateHandle.set<String?>(Rutas.MENSAJE, null) },
                    onNuevaSolicitud = { navController.navigate(Rutas.form(null)) },
                    onAbrirSolicitud = { id -> navController.navigate(Rutas.form(id)) },
                    onEscanearQr = { navController.navigate(Rutas.QR) },
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable(Rutas.POSTS) {
            RequiereSesion(usuario, irALogin) {
                PostScreen(
                    viewModel = viewModel<PostViewModel>(factory = viewModelFactory),
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable(Rutas.QR) {
            RequiereSesion(usuario, irALogin) {
                QrScannerScreen(
                    viewModel = viewModel<QrViewModel>(factory = viewModelFactory),
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
                    viewModel = viewModel<SolicitudFormViewModel>(factory = viewModelFactory),
                    onFinalizado = { mensaje ->
                        navController.previousBackStackEntry?.savedStateHandle?.set(Rutas.MENSAJE, mensaje)
                        navController.popBackStack()
                    },
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
