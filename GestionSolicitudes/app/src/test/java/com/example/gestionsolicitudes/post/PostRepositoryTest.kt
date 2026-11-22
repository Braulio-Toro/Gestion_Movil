package com.example.gestionsolicitudes.post

import com.example.gestionsolicitudes.data.remote.ApiService
import com.example.gestionsolicitudes.data.repository.PostRepository
import com.example.gestionsolicitudes.model.Post
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest

class PostRepositoryTest : StringSpec({

    val fakePosts = listOf(
        Post(1, 1, "Titulo 1", "Contenido 1"),
        Post(2, 2, "Titulo 2", "Contenido 2")
    )

    "getPosts devuelve los datos entregados por la API" {
        val api = mockk<ApiService>()
        coEvery { api.getPosts() } returns fakePosts

        val repo = PostRepository(api)

        runTest { repo.getPosts() shouldContainExactly fakePosts }
    }

    "getPosts consulta la API exactamente una vez" {
        val api = mockk<ApiService>()
        coEvery { api.getPosts() } returns emptyList()

        runTest {
            PostRepository(api).getPosts() shouldBe emptyList()
            coVerify(exactly = 1) { api.getPosts() }
        }
    }

    "getPosts propaga el error de red" {
        val api = mockk<ApiService>()
        coEvery { api.getPosts() } throws java.io.IOException("sin red")

        runTest {
            val error = runCatching { PostRepository(api).getPosts() }.exceptionOrNull()
            error?.message shouldBe "sin red"
        }
    }
})
