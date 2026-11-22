package com.example.gestionsolicitudes.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [SolicitudEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun solicitudDao(): SolicitudDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * v1 → v2: agrega prioridad, técnico asignado y solución. Son columnas opcionales,
         * así que las solicitudes ya guardadas se conservan (quedan "sin asignar").
         */
        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE solicitudes ADD COLUMN prioridad TEXT")
                db.execSQL("ALTER TABLE solicitudes ADD COLUMN tecnicoAsignado TEXT")
                db.execSQL("ALTER TABLE solicitudes ADD COLUMN solucion TEXT")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "solicitudes_database"
                )
                    .addMigrations(MIGRACION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
