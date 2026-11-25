package com.mpazpro3.app.data.local

import androidx.room.withTransaction
import com.mpazpro3.app.data.AlmacenLocal
import com.mpazpro3.app.data.DatosLocales
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.InformeDocente
import com.mpazpro3.app.model.SeguimientoAvance
import com.mpazpro3.app.model.Usuario


class AlmacenRoom(
    private val db: AppDatabase,
    private val sembrar: suspend () -> Unit
) : AlmacenLocal {

    override suspend fun <T> transaccion(bloque: suspend () -> T): T = db.withTransaction { bloque() }

    override suspend fun leer(): DatosLocales {
        val respuestas = db.respuestaDao().todas().groupBy { it.estudianteId to it.contenidoId }
        return DatosLocales(
            contenidos = db.contenidoDao().todos().map { it.aDominio() },
            actividades = db.actividadDao().todas().map { it.aDominio() },
            asignaciones = db.asignacionDao().todas().map { it.aDominio() },
            estudiantes = db.estudianteDao().todos().map { it.aDominio() },
            seguimientos = db.seguimientoDao().todos().map { seguimiento ->
                val propias = respuestas[seguimiento.estudianteId to seguimiento.contenidoId].orEmpty()
                seguimiento.aDominio(propias.map { it.aDominio() })
            },
            informes = db.informeDao().todos().map { it.aDominio() }
        )
    }

    override suspend fun autenticar(usuarioNormalizado: String, claveHash: String): Usuario? =
        db.usuarioDao().buscarPorCredenciales(usuarioNormalizado, claveHash)?.aDominio()


    override suspend fun actualizarContenido(contenidoId: String, estado: EstadoAprobacion, observacion: String) {
        db.contenidoDao().actualizarEstado(contenidoId, estado.name, observacion)
    }

    override suspend fun reemplazarAsignacionesDelCurso(curso: String, asignaciones: List<Asignacion>) {
        db.withTransaction {
            db.asignacionDao().borrarDelCurso(curso)
            db.asignacionDao().insertarTodas(asignaciones.map { it.aEntidad() })
        }
    }

    override suspend fun guardarSeguimiento(seguimiento: SeguimientoAvance) {
        db.withTransaction {
            db.seguimientoDao().guardar(seguimiento.aEntidad())
            db.respuestaDao().borrarDe(seguimiento.estudianteId, seguimiento.contenidoId)
            db.respuestaDao().insertarTodas(seguimiento.respuestasAEntidad())
        }
    }

    override suspend fun guardarInforme(informe: InformeDocente) {
        db.informeDao().guardar(informe.aEntidad())
    }

    override suspend fun restablecer() {
        db.withTransaction {
            db.respuestaDao().borrarTodas()
            db.seguimientoDao().borrarTodos()
            db.informeDao().borrarTodos()
            db.asignacionDao().borrarTodas()
            db.actividadDao().borrarAlternativas()
            db.actividadDao().borrarActividades()
            db.recursoDao().borrarTodos()
            db.contenidoDao().borrarTodos()
            db.estudianteDao().borrarTodos()
            db.usuarioDao().borrarTodos()
            sembrar()
        }
    }
}
