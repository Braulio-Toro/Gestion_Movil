package com.example.gestionsolicitudes.model

/** Publicación devuelta por la API REST (JSONPlaceholder), igual que en AppBrause. */
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)
