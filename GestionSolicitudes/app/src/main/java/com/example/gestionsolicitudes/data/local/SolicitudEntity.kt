package com.example.gestionsolicitudes.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "solicitudes",
    indices = [Index(value = ["codigo"], unique = true)]
)
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Código único de seguimiento, por ejemplo SOL-20261015-4821. */
    val codigo: String,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val servicio: String,
    /** URI (content://) de la fotografía de evidencia guardada en el almacenamiento de la app. */
    val evidenciaUri: String?,
    val estado: String,
    /** Fecha y hora de creación en milisegundos (epoch). */
    val fechaCreacion: Long,
    val fechaActualizacion: Long,
    /** Correo (ficticio) del usuario que registró la solicitud. */
    val creadaPor: String
)
