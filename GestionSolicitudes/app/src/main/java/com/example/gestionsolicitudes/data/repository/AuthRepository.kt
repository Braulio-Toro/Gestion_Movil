package com.example.gestionsolicitudes.data.repository

import com.example.gestionsolicitudes.model.Perfil
import com.example.gestionsolicitudes.model.Usuario

/** Autenticación simulada con cuentas ficticias (no hay credenciales reales en el MVP). */
class AuthRepository {

    val cuentasDemo: List<Usuario> = listOf(
        Usuario(1, "Cliente Demo", "cliente@demo.test", "cliente123", Perfil.CLIENTE, "Empresa Demo SpA"),
        Usuario(2, "Admin Demo", "admin@demo.test", "admin123", Perfil.ADMINISTRADOR, "Servicios Digitales Demo"),
        Usuario(3, "Técnico Demo", "tecnico@demo.test", "tecnico123", Perfil.TECNICO, "Servicios Digitales Demo"),
        Usuario(4, "Técnico Demo 2", "tecnico2@demo.test", "tecnico123", Perfil.TECNICO, "Servicios Digitales Demo")
    )

    /** Técnicos que el administrador puede asignar a una solicitud. */
    val tecnicos: List<Usuario> = cuentasDemo.filter { it.perfil == Perfil.TECNICO }

    fun autenticar(correo: String, clave: String): Usuario? =
        cuentasDemo.firstOrNull {
            it.correo.equals(correo.trim(), ignoreCase = true) && it.clave == clave
        }
}
