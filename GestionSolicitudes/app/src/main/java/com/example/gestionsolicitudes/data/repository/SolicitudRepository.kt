package com.example.gestionsolicitudes.data.repository

import com.example.gestionsolicitudes.data.local.SolicitudDao
import com.example.gestionsolicitudes.data.local.SolicitudEntity
import com.example.gestionsolicitudes.model.EstadoSolicitud
import com.example.gestionsolicitudes.model.Perfil
import com.example.gestionsolicitudes.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/** Única puerta de acceso a los datos de solicitudes: el ViewModel nunca habla con el DAO. */
class SolicitudRepository(private val dao: SolicitudDao) {

    /** El cliente solo observa sus solicitudes; administrador y técnico observan todas. */
    fun observarSolicitudes(usuario: Usuario?): Flow<List<SolicitudEntity>> = when {
        usuario == null -> flowOf(emptyList())
        usuario.perfil == Perfil.CLIENTE -> dao.observarPorCreador(usuario.correo)
        else -> dao.observarTodas()
    }

    suspend fun obtenerPorId(id: Long): SolicitudEntity? = dao.obtenerPorId(id)

    /** Búsqueda por código único (usada por el lector QR). */
    suspend fun buscarPorCodigo(codigo: String): SolicitudEntity? = dao.obtenerPorCodigo(codigo)

    /** Crea la solicitud con código único, fecha/hora actual y estado inicial "Recibida". */
    suspend fun crear(
        titulo: String,
        descripcion: String,
        categoria: String,
        servicio: String,
        evidenciaUri: String?,
        creadaPor: String
    ): SolicitudEntity {
        val ahora = System.currentTimeMillis()
        val solicitud = SolicitudEntity(
            codigo = generarCodigoUnico(ahora),
            titulo = titulo.trim(),
            descripcion = descripcion.trim(),
            categoria = categoria,
            servicio = servicio,
            evidenciaUri = evidenciaUri,
            estado = EstadoSolicitud.RECIBIDA,
            fechaCreacion = ahora,
            fechaActualizacion = ahora,
            creadaPor = creadaPor
        )
        val id = dao.insertar(solicitud)
        return solicitud.copy(id = id)
    }

    /** Actualiza los datos básicos conservando código, estado y fecha de creación. */
    suspend fun actualizar(
        original: SolicitudEntity,
        titulo: String,
        descripcion: String,
        categoria: String,
        servicio: String,
        evidenciaUri: String?
    ): SolicitudEntity {
        val actualizada = original.copy(
            titulo = titulo.trim(),
            descripcion = descripcion.trim(),
            categoria = categoria,
            servicio = servicio,
            evidenciaUri = evidenciaUri,
            fechaActualizacion = System.currentTimeMillis()
        )
        dao.actualizar(actualizada)
        return actualizada
    }

    private suspend fun generarCodigoUnico(ahora: Long): String {
        val fecha = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(ahora))
        var codigo = nuevoCodigo(fecha)
        while (dao.contarPorCodigo(codigo) > 0) {
            codigo = nuevoCodigo(fecha)
        }
        return codigo
    }

    private fun nuevoCodigo(fecha: String): String = "SOL-$fecha-${Random.nextInt(1000, 10000)}"
}
