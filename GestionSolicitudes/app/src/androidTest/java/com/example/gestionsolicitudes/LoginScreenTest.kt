package com.example.gestionsolicitudes

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.gestionsolicitudes.data.repository.AuthRepository
import com.example.gestionsolicitudes.data.repository.SesionRepository
import com.example.gestionsolicitudes.model.Perfil
import com.example.gestionsolicitudes.ui.screens.LoginScreen
import com.example.gestionsolicitudes.viewmodel.LoginViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Pruebas de interfaz de LoginScreen. Se usa el LoginViewModel real con los
 * repositorios en memoria (AuthRepository y SesionRepository no dependen de Android).
 */
class LoginScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val sesion = SesionRepository()
    private var loginExitosoLlamado = false

    private fun mostrarLogin() {
        val viewModel = LoginViewModel(AuthRepository(), sesion)
        composeRule.setContent {
            LoginScreen(viewModel = viewModel, onLoginExitoso = { loginExitosoLlamado = true })
        }
        composeRule.waitForIdle()
    }

    private fun campoCorreo() =
        composeRule.onNode(hasText("Correo electrónico") and hasSetTextAction())

    private fun campoClave() =
        composeRule.onNode(hasText("Contraseña") and hasSetTextAction())

    private fun pulsarIngresar() {
        composeRule.onNodeWithText("Ingresar").performScrollTo().performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun la_pantalla_muestra_titulo_campos_y_boton_ingresar() {
        mostrarLogin()

        composeRule.onNodeWithText("Gestión de solicitudes").assertIsDisplayed()
        composeRule.onNodeWithText("Iniciar sesión").assertIsDisplayed()
        campoCorreo().assertIsDisplayed()
        campoClave().assertIsDisplayed()
        composeRule.onNodeWithText("Ingresar").assertIsDisplayed()
    }

    @Test
    fun con_campos_vacios_se_muestran_los_errores_de_validacion() {
        mostrarLogin()

        pulsarIngresar()

        composeRule.onNodeWithText("El correo es obligatorio").assertIsDisplayed()
        composeRule.onNodeWithText("La contraseña es obligatoria").assertIsDisplayed()
        assertFalse(loginExitosoLlamado)
        assertNull(sesion.usuario.value)
    }

    @Test
    fun un_correo_sin_arroba_muestra_correo_invalido() {
        mostrarLogin()

        campoCorreo().performTextInput("correo-sin-arroba")
        campoClave().performTextInput("cliente123")
        pulsarIngresar()

        composeRule.onNodeWithText("Correo inválido").assertIsDisplayed()
        assertFalse(loginExitosoLlamado)
    }

    @Test
    fun el_error_de_un_campo_desaparece_al_empezar_a_escribir() {
        mostrarLogin()
        pulsarIngresar()
        composeRule.onNodeWithText("El correo es obligatorio").assertIsDisplayed()

        campoCorreo().performTextInput("a")

        composeRule.onNodeWithText("El correo es obligatorio").assertDoesNotExist()
    }

    @Test
    fun con_credenciales_incorrectas_se_muestra_el_error_general() {
        mostrarLogin()

        campoCorreo().performTextInput("cliente@demo.test")
        campoClave().performTextInput("clave-incorrecta")
        pulsarIngresar()

        composeRule.onNodeWithText("Correo o contraseña incorrectos").assertIsDisplayed()
        assertFalse(loginExitosoLlamado)
        assertNull(sesion.usuario.value)
    }

    @Test
    fun con_credenciales_validas_inicia_sesion_y_llama_a_onLoginExitoso() {
        mostrarLogin()

        campoCorreo().performTextInput("admin@demo.test")
        campoClave().performTextInput("admin123")
        pulsarIngresar()

        assertTrue(loginExitosoLlamado)
        assertEquals(Perfil.ADMINISTRADOR, sesion.usuario.value?.perfil)
    }

    @Test
    fun tocar_una_cuenta_de_prueba_completa_el_correo() {
        mostrarLogin()

        composeRule.onNodeWithText("Cliente: cliente@demo.test").performScrollTo().performClick()
        composeRule.waitForIdle()

        campoCorreo().assertTextContains("cliente@demo.test")
    }

    @Test
    fun tocar_una_cuenta_de_prueba_y_pulsar_ingresar_abre_sesion() {
        mostrarLogin()

        composeRule.onNodeWithText("Técnico: tecnico@demo.test").performScrollTo().performClick()
        pulsarIngresar()

        assertTrue(loginExitosoLlamado)
        assertEquals(Perfil.TECNICO, sesion.usuario.value?.perfil)
    }

    @Test
    fun el_icono_del_ojo_alterna_entre_mostrar_y_ocultar_la_contrasena() {
        mostrarLogin()

        composeRule.onNodeWithContentDescription("Mostrar contraseña").performClick()
        composeRule.onNodeWithContentDescription("Ocultar contraseña").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Ocultar contraseña").performClick()
        composeRule.onNodeWithContentDescription("Mostrar contraseña").assertIsDisplayed()
    }
}