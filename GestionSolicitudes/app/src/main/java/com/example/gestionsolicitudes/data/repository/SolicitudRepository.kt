package com.example.gestionsolicitudes.data.repository

import com.example.gestionsolicitudes.data.local.SolicitudDao
import com.example.gestionsolicitudes.data.local.toEntity
import com.example.gestionsolicitudes.data.local.toModel
import com.example.gestionsolicitudes.model.EstadoSolicitud
import com.example.gestionsolicitudes.model.Perfil
import com.example.gestionsolicitudes.model.Solicitud
import com.example.gestionsolicitudes.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * Única puerta de acceso a los datos de solicitudes: el ViewModel nunca habla con el DAO
 * ni conoce la entidad de Room; recibe y entrega el modelo de dominio [Solicitud].
 */
class SolicitudRepository(private val dao: SolicitudDao) {

    /** El cliente solo observa sus solicitudes; administrador y técnico observan todas. */
    fun observarSolicitudes(usuario: Usuario?): Flow<List<Solicitud>> {
        val entidades = when {
            usuario == null -> flowOf(emptyList())
            usuario.perfil == Perfil.CLIENTE -> dao.observarPorCreador(usuario.correo)
            else -> dao.observarTodas()
        }
        return entidades.map { lista -> lista.map { it.toModel() } }
    }

    suspend fun obtenerPorId(id: Long): Solicitud? = dao.obtenerPorId(id)?.toModel()

    /** Búsqueda por código único (usada por el lector QR). */
    suspend fun buscarPorCodigo(codigo: String): Solicitud? = dao.obtenerPorCodigo(codigo)?.toModel()

    /** Crea la solicitud con código único, fecha/hora actual y estado inicial "Recibida". */
    suspend fun crear(
        titulo: String,
        descripcion: String,
        categoria: String,
        servicio: String,
        evidenciaUri: String?,
        creadaPor: String
    ): Solicitud {
        val ahora = System.currentTimeMillis()
        val solicitud = Solicitud(
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
        val id = dao.insertar(solicitud.toEntity())
        return solicitud.copy(id = id)
    }

    /**
     * Guarda los cambios sobre una solicitud existente, conservando código y fecha de creación.
     * Qué campos puede cambiar cada perfil lo decide el ViewModel con [com.example.gestionsolicitudes.model.Permisos];
     * aquí solo se persiste el resultado.
     */
    suspend fun actualizar(
        original: Solicitud,
        titulo: String,
        descripcion: String,
        categoria: String,
        servicio: String,
        evidenciaUri: String?,
        prioridad: String?,
        tecnicoAsignado: String?,
        estado: String,
        solucion: String?
    ): Solicitud {
        val actualizada = original.copy(
            titulo = titulo.trim(),
            descripcion = descripcion.trim(),
            categoria = categoria,
            servicio = servicio,
            evidenciaUri = evidenciaUri,
            prioridad = prioridad?.takeIf { it.isNotBlank() },
            tecnicoAsignado = tecnicoAsignado?.takeIf { it.isNotBlank() },
            estado = estado,
            solucion = solucion?.trim()?.takeIf { it.isNotEmpty() },
            fechaActualizacion = System.currentTimeMillis()
        )
        dao.actualizar(actualizada.toEntity())
        return actualizada
    }

    /** Elimina la solicitud de Room (la lista observada se actualiza sola). */
    suspend fun eliminar(solicitud: Solicitud) = dao.eliminar(solicitud.toEntity())

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
