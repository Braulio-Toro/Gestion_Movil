package com.example.gestionsolicitudes.model

enum class Perfil(val etiqueta: String) {
    CLIENTE("Cliente"),
    ADMINISTRADOR("Administrador"),
    TECNICO("Técnico")
}

/** Usuario ficticio de prueba (el caso exige cuentas y datos ficticios). */
data class Usuario(
    val id: Int,
    val nombre: String,
    val correo: String,
    val clave: String,
    val perfil: Perfil,
    val empresa: String
)
