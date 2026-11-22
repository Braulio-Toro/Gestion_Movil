package com.example.gestionsolicitudes.data.remote

import com.example.gestionsolicitudes.model.Post
import retrofit2.http.GET

/** Colección de endpoints de la API REST consumida con Retrofit. */
interface ApiService {
    @GET("posts")
    suspend fun getPosts(): List<Post>
}
