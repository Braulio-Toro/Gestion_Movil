package com.example.gestionsolicitudes.data.local

import com.example.gestionsolicitudes.model.Solicitud

/** Conversión entre la entidad de Room y el modelo de dominio. */
fun SolicitudEntity.toModel(): Solicitud = Solicitud(
    id = id,
    codigo = codigo,
    titulo = titulo,
    descripcion = descripcion,
    categoria = categoria,
    servicio = servicio,
    evidenciaUri = evidenciaUri,
    estado = estado,
    fechaCreacion = fechaCreacion,
    fechaActualizacion = fechaActualizacion,
    creadaPor = creadaPor,
    prioridad = prioridad,
    tecnicoAsignado = tecnicoAsignado,
    solucion = solucion
)

fun Solicitud.toEntity(): SolicitudEntity = SolicitudEntity(
    id = id,
    codigo = codigo,
    titulo = titulo,
    descripcion = descripcion,
    categoria = categoria,
    servicio = servicio,
    evidenciaUri = evidenciaUri,
    estado = estado,
    fechaCreacion = fechaCreacion,
    fechaActualizacion = fechaActualizacion,
    creadaPor = creadaPor,
    prioridad = prioridad,
    tecnicoAsignado = tecnicoAsignado,
    solucion = solucion
)
