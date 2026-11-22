package com.example.gestionsolicitudes.data.repository

import com.example.gestionsolicitudes.model.CodigoSolicitud
import com.example.gestionsolicitudes.model.Permisos
import com.example.gestionsolicitudes.model.QrResult
import com.example.gestionsolicitudes.model.Usuario

/**
 * Procesa el contenido de un QR (como processQrContent en camara_prueba): extrae el código
 * único, busca la solicitud y comprueba que el usuario tenga acceso. El ViewModel solo
 * traduce el [QrResult] a estado de pantalla.
 */
open class QrRepository(private val solicitudes: SolicitudRepository) {

    open suspend fun procesarContenido(contenido: String, usuario: Usuario?): QrResult {
        val codigo = CodigoSolicitud.extraer(contenido) ?: return QrResult.NoValido
        val solicitud = solicitudes.buscarPorCodigo(codigo)
        return if (solicitud != null && usuario != null && Permisos.puedeVer(usuario, solicitud.creadaPor)) {
            QrResult.Encontrada(solicitudId = solicitud.id, codigo = codigo)
        } else {
            QrResult.NoEncontrada(codigo)
        }
    }
}
