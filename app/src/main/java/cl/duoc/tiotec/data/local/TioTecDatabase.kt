package cl.duoc.tiotec.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos principal de Room en SQLite para TíoTec Preventivo (Actividad 2.5).
 * Centraliza la persistencia relacional con patrón Singleton.
 */
@Database(
    entities = [
        DispositivoEntity::class,
        AccionChecklistEntity::class,
        EvaluacionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TioTecDatabase : RoomDatabase() {

    abstract fun tioTecDao(): TioTecDao

    companion object {
        @Volatile
        private var INSTANCE: TioTecDatabase? = null

        fun getInstance(context: Context): TioTecDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TioTecDatabase::class.java,
                    "tiotec_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
