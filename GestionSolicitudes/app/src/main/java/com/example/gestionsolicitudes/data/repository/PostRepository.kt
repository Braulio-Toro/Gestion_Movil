package com.example.gestionsolicitudes.data.repository

import com.example.gestionsolicitudes.data.remote.ApiService
import com.example.gestionsolicitudes.data.remote.RetrofitInstance
import com.example.gestionsolicitudes.model.Post

/** La API se inyecta por constructor para poder reemplazarla por un mock en las pruebas. */
open class PostRepository(private val api: ApiService = RetrofitInstance.api) {
    open suspend fun getPosts(): List<Post> = api.getPosts()
}
