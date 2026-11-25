package com.mpazpro3.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(
    entities = [
        UsuarioEntity::class,
        EstudianteEntity::class,
        ContenidoEntity::class,
        RecursoEntity::class,
        ActividadEntity::class,
        AlternativaEntity::class,
        AsignacionEntity::class,
        SeguimientoEntity::class,
        RespuestaEntity::class,
        InformeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun estudianteDao(): EstudianteDao
    abstract fun contenidoDao(): ContenidoDao
    abstract fun recursoDao(): RecursoDao
    abstract fun actividadDao(): ActividadDao
    abstract fun asignacionDao(): AsignacionDao
    abstract fun seguimientoDao(): SeguimientoDao
    abstract fun respuestaDao(): RespuestaDao
    abstract fun informeDao(): InformeDao

    companion object {


        const val NOMBRE_SERVIDOR = "mpazpro3.db"
        const val NOMBRE_LOCAL = "mpazpro3_local.db"
        private val instancias = mutableMapOf<String, AppDatabase>()


        fun obtener(context: Context, nombre: String = NOMBRE_SERVIDOR): AppDatabase =
            synchronized(this) {
                instancias.getOrPut(nombre) {
                    Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, nombre).build()
                }
            }
    }
}
