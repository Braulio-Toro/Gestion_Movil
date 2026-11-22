package com.example.gestionsolicitudes.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PermisosTest : StringSpec({

    fun usuario(perfil: Perfil) = Usuario(1, "Prueba", "prueba@correo.cl", "1234", perfil, "Empresa")

    "el administrador puede eliminar solicitudes" {
        Permisos.puedeEliminar(usuario(Perfil.ADMINISTRADOR)) shouldBe true
    }

    "el tecnico puede eliminar solicitudes" {
        Permisos.puedeEliminar(usuario(Perfil.TECNICO)) shouldBe true
    }

    "el cliente no puede eliminar solicitudes" {
        Permisos.puedeEliminar(usuario(Perfil.CLIENTE)) shouldBe false
    }

    "el tecnico sigue sin poder crear ni editar los datos basicos" {
        val tecnico = usuario(Perfil.TECNICO)
        Permisos.puedeCrear(tecnico) shouldBe false
        Permisos.puedeEditar(tecnico, "prueba@correo.cl", EstadoSolicitud.RECIBIDA) shouldBe false
    }

    // ---------- Prioridad y técnico asignado: solo el administrador ----------

    "solo el administrador asigna prioridad y tecnico" {
        Permisos.puedeAsignar(usuario(Perfil.ADMINISTRADOR), EstadoSolicitud.RECIBIDA) shouldBe true
        Permisos.puedeAsignar(usuario(Perfil.TECNICO), EstadoSolicitud.RECIBIDA) shouldBe false
        Permisos.puedeAsignar(usuario(Perfil.CLIENTE), EstadoSolicitud.RECIBIDA) shouldBe false
    }

    "el administrador no asigna en solicitudes cerradas o canceladas" {
        val admin = usuario(Perfil.ADMINISTRADOR)
        Permisos.puedeAsignar(admin, EstadoSolicitud.CERRADA) shouldBe false
        Permisos.puedeAsignar(admin, EstadoSolicitud.CANCELADA) shouldBe false
    }

    // ---------- Estado y solución ----------

    "el administrador puede cambiar el estado mientras la solicitud este abierta" {
        val admin = usuario(Perfil.ADMINISTRADOR)
        Permisos.puedeCambiarEstado(admin, null, EstadoSolicitud.EN_REVISION) shouldBe true
        Permisos.puedeCambiarEstado(admin, null, EstadoSolicitud.CERRADA) shouldBe false
    }

    "el tecnico asignado puede cambiar el estado y escribir la solucion" {
        val tecnico = usuario(Perfil.TECNICO)
        Permisos.puedeCambiarEstado(tecnico, tecnico.correo, EstadoSolicitud.EN_PROCESO) shouldBe true
        Permisos.puedeEscribirSolucion(tecnico, tecnico.correo, EstadoSolicitud.EN_PROCESO) shouldBe true
    }

    "el tecnico no asignado no puede cambiar el estado ni escribir la solucion" {
        val tecnico = usuario(Perfil.TECNICO)
        Permisos.puedeCambiarEstado(tecnico, null, EstadoSolicitud.EN_PROCESO) shouldBe false
        Permisos.puedeCambiarEstado(tecnico, "otro@correo.cl", EstadoSolicitud.EN_PROCESO) shouldBe false
        Permisos.puedeEscribirSolucion(tecnico, "otro@correo.cl", EstadoSolicitud.EN_PROCESO) shouldBe false
    }

    "el tecnico asignado no modifica una solicitud cerrada o cancelada" {
        val tecnico = usuario(Perfil.TECNICO)
        Permisos.puedeCambiarEstado(tecnico, tecnico.correo, EstadoSolicitud.CERRADA) shouldBe false
        Permisos.puedeEscribirSolucion(tecnico, tecnico.correo, EstadoSolicitud.CANCELADA) shouldBe false
    }

    "el cliente y el administrador no escriben la solucion" {
        val cliente = usuario(Perfil.CLIENTE)
        val admin = usuario(Perfil.ADMINISTRADOR)
        Permisos.puedeCambiarEstado(cliente, cliente.correo, EstadoSolicitud.RECIBIDA) shouldBe false
        Permisos.puedeEscribirSolucion(cliente, cliente.correo, EstadoSolicitud.RECIBIDA) shouldBe false
        Permisos.puedeEscribirSolucion(admin, admin.correo, EstadoSolicitud.RECIBIDA) shouldBe false
    }
})
