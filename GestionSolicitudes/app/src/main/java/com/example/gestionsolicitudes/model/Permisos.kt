package com.example.gestionsolicitudes.model

/** Reglas de acceso según el perfil (separación cliente / administrador / técnico). */
object Permisos {

    /** El cliente solo puede consultar sus propias solicitudes. */
    fun puedeVer(usuario: Usuario, creadaPor: String): Boolean =
        when (usuario.perfil) {
            Perfil.CLIENTE -> creadaPor.equals(usuario.correo, ignoreCase = true)
            Perfil.ADMINISTRADOR, Perfil.TECNICO -> true
        }

    /**
     * Datos básicos de la solicitud (título, descripción, categoría, servicio, evidencia).
     * Cliente: solo sus solicitudes mientras estén "Recibida" (no puede modificar tras el cierre).
     * Administrador: mientras la solicitud no esté cerrada o cancelada.
     * Técnico: nunca; solo gestiona estado y solución (ver [puedeCambiarEstado]).
     */
    fun puedeEditar(usuario: Usuario, creadaPor: String, estado: String): Boolean =
        when (usuario.perfil) {
            Perfil.CLIENTE ->
                creadaPor.equals(usuario.correo, ignoreCase = true) &&
                    estado == EstadoSolicitud.RECIBIDA
            Perfil.ADMINISTRADOR -> !EstadoSolicitud.esFinal(estado)
            Perfil.TECNICO -> false
        }

    fun puedeCrear(usuario: Usuario): Boolean = usuario.perfil != Perfil.TECNICO

    /** Eliminar solicitudes (D del CRUD): solo técnico y administrador; el cliente no puede. */
    fun puedeEliminar(usuario: Usuario): Boolean =
        usuario.perfil == Perfil.ADMINISTRADOR || usuario.perfil == Perfil.TECNICO

    /**
     * Prioridad y técnico asignado: solo los asigna el administrador,
     * mientras la solicitud no esté cerrada o cancelada.
     */
    fun puedeAsignar(usuario: Usuario, estado: String): Boolean =
        usuario.perfil == Perfil.ADMINISTRADOR && !EstadoSolicitud.esFinal(estado)

    /**
     * Estado: lo fija el administrador y lo puede modificar el técnico asignado a la solicitud.
     * Ya cerrada o cancelada, nadie lo cambia.
     */
    fun puedeCambiarEstado(usuario: Usuario, tecnicoAsignado: String?, estado: String): Boolean =
        !EstadoSolicitud.esFinal(estado) &&
            when (usuario.perfil) {
                Perfil.ADMINISTRADOR -> true
                Perfil.TECNICO -> esTecnicoAsignado(usuario, tecnicoAsignado)
                Perfil.CLIENTE -> false
            }

    /** Solución: solo la escribe el técnico asignado, mientras la solicitud siga abierta. */
    fun puedeEscribirSolucion(usuario: Usuario, tecnicoAsignado: String?, estado: String): Boolean =
        !EstadoSolicitud.esFinal(estado) &&
            usuario.perfil == Perfil.TECNICO &&
            esTecnicoAsignado(usuario, tecnicoAsignado)

    private fun esTecnicoAsignado(usuario: Usuario, tecnicoAsignado: String?): Boolean =
        tecnicoAsignado != null && tecnicoAsignado.equals(usuario.correo, ignoreCase = true)
}
