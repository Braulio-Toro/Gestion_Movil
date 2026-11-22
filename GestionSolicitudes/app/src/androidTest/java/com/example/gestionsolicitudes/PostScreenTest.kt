package com.example.gestionsolicitudes

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.gestionsolicitudes.data.repository.PostRepository
import com.example.gestionsolicitudes.model.Post
import com.example.gestionsolicitudes.ui.screens.PostScreen
import com.example.gestionsolicitudes.viewmodel.PostViewModel
import org.junit.Rule
import org.junit.Test

class PostScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val fakePosts = listOf(
        Post(1, 1, "Titulo 1", "Contenido 1"),
        Post(2, 2, "Titulo 2", "Contenido 2")
    )

    @Test
    fun los_posts_aparecen_en_pantalla() {
        val repo = object : PostRepository() {
            override suspend fun getPosts(): List<Post> = fakePosts
        }

        composeRule.setContent { PostScreen(viewModel = PostViewModel(repo), onVolver = {}) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Titulo: Titulo 1").assertIsDisplayed()
        composeRule.onNodeWithText("Titulo: Titulo 2").assertIsDisplayed()
    }

    @Test
    fun si_la_api_falla_se_muestra_el_boton_reintentar() {
        val repo = object : PostRepository() {
            override suspend fun getPosts(): List<Post> = throw RuntimeException("fallo")
        }

        composeRule.setContent { PostScreen(viewModel = PostViewModel(repo), onVolver = {}) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Reintentar").assertIsDisplayed()
    }
}
