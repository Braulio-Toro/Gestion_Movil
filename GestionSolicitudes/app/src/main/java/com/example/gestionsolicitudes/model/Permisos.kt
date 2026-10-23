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
     * Cliente: solo sus solicitudes mientras estén "Recibida" (no puede modificar tras el cierre).
     * Administrador: mientras la solicitud no esté cerrada o cancelada.
     * Técnico: solo consulta en esta primera entrega.
     */
    fun puedeEditar(usuario: Usuario, creadaPor: String, estado: String): Boolean =
        when (usuario.perfil) {
            Perfil.CLIENTE ->
                creadaPor.equals(usuario.correo, ignoreCase = true) &&
                    estado == EstadoSolicitud.RECIBIDA
            Perfil.ADMINISTRADOR ->
                estado != EstadoSolicitud.CERRADA && estado != EstadoSolicitud.CANCELADA
            Perfil.TECNICO -> false
        }

    fun puedeCrear(usuario: Usuario): Boolean = usuario.perfil != Perfil.TECNICO
}
