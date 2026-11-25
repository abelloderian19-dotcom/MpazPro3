package com.mpazpro3.app.data.local

import com.mpazpro3.app.data.CacheLocal
import com.mpazpro3.app.data.Instantanea
import com.mpazpro3.app.data.RespuestaLocal
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.InformeDocente
import com.mpazpro3.app.model.SeguimientoAvance
import com.mpazpro3.app.model.Usuario

cheRoom(private val db: AppDatabase) : CacheLocal {
    override suspend fun reemplazarTodo(instantanea: Instantanea) {
        TODO("E3-6a: borrar e insertar todas las tablas dentro de una transacción")
    }

    override suspend fun guardarContenido(contenido: Contenido) {
        TODO("E3-6b: actualizar el estado del contenido con UPDATE (no REPLACE)")
    }

    override suspend fun guardarAsignaciones(curso: String, asignaciones: List<Asignacion>) {
        TODO("E3-6c: reemplazar las asignaciones del curso")
    }

    override suspend fun guardarSeguimiento(seguimiento: SeguimientoAvance) {
        TODO("E3-6d: reemplazar seguimiento y respuestas")
    }

    override suspend fun guardarInforme(informe: InformeDocente) {
        TODO("E3-6e: guardar el informe en Room")
    }

    override suspend fun guardarUsuario(usuario: Usuario) {
        TODO("E3-8a: guardar el usuario en Room")
    }

    override suspend fun autenticarLocal(usuarioNormalizado: String, claveHash: String): Usuario? =
        TODO("E3-8b: buscar el usuario por credenciales en Room")

    // ---- Respaldo sin conexión (opcional: se reenvía al servidor cuando vuelve la red) ----

    override suspend fun marcarMaterialLocal(estudianteId: String, contenidoId: String): Boolean =
        TODO("E3-8c: (avanzado) marcar el material como revisado en Room")

    override suspend fun registrarRespuestaLocal(estudianteId: String, actividadId: String, alternativa: Int): RespuestaLocal? =
        TODO("E3-8d: (avanzado) registrar la respuesta en Room")
}
