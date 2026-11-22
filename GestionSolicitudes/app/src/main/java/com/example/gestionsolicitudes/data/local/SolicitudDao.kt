package com.example.gestionsolicitudes.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SolicitudDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(solicitud: SolicitudEntity): Long

    @Update
    suspend fun actualizar(solicitud: SolicitudEntity)

    @Delete
    suspend fun eliminar(solicitud: SolicitudEntity)

    @Query("SELECT * FROM solicitudes ORDER BY fechaCreacion DESC")
    fun observarTodas(): Flow<List<SolicitudEntity>>

    @Query("SELECT * FROM solicitudes WHERE creadaPor = :correo ORDER BY fechaCreacion DESC")
    fun observarPorCreador(correo: String): Flow<List<SolicitudEntity>>

    @Query("SELECT * FROM solicitudes WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): SolicitudEntity?

    @Query("SELECT * FROM solicitudes WHERE codigo = :codigo LIMIT 1")
    suspend fun obtenerPorCodigo(codigo: String): SolicitudEntity?

    @Query("SELECT COUNT(*) FROM solicitudes WHERE codigo = :codigo")
    suspend fun contarPorCodigo(codigo: String): Int
}
