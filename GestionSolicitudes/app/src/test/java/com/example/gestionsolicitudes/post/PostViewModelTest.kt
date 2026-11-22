package com.example.gestionsolicitudes.post

import com.example.gestionsolicitudes.data.repository.PostRepository
import com.example.gestionsolicitudes.model.Post
import com.example.gestionsolicitudes.viewmodel.PostViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class PostViewModelTest : StringSpec({

    val fakePosts = listOf(
        Post(1, 1, "Titulo 1", "Contenido 1"),
        Post(2, 2, "Titulo 2", "Contenido 2")
    )

    // viewModelScope usa Dispatchers.Main: se reemplaza por uno de prueba
    beforeSpec { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterSpec { Dispatchers.resetMain() }

    "al iniciar, el estado contiene los posts de la API" {
        val repo = mockk<PostRepository>()
        coEvery { repo.getPosts() } returns fakePosts

        val vm = PostViewModel(repo)

        vm.estado.value.posts shouldContainExactly fakePosts
        vm.estado.value.cargando shouldBe false
        vm.estado.value.error.shouldBeNull()
    }

    "si la API falla, el estado informa el error y no hay posts" {
        val repo = mockk<PostRepository>()
        coEvery { repo.getPosts() } throws RuntimeException("fallo")

        val vm = PostViewModel(repo)

        vm.estado.value.posts shouldBe emptyList()
        vm.estado.value.cargando shouldBe false
        vm.estado.value.error.shouldNotBeNull()
    }

    "cargarPosts permite reintentar tras un error" {
        val repo = mockk<PostRepository>()
        coEvery { repo.getPosts() } throws RuntimeException("fallo")
        val vm = PostViewModel(repo)

        coEvery { repo.getPosts() } returns fakePosts
        vm.cargarPosts()

        vm.estado.value.posts shouldContainExactly fakePosts
        vm.estado.value.error.shouldBeNull()
    }
})
