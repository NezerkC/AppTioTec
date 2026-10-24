package cl.duoc.tiotec.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para el acceso reactivo y transaccional
 * a las tablas SQLite de Room (Actividad 2.5).
 */
@Dao
interface TioTecDao {

    // -------------------------------------------------------------------------
    // Dispositivos (RF-02)
    // -------------------------------------------------------------------------

    @Query("SELECT * FROM dispositivos ORDER BY id ASC")
    fun obtenerTodosLosDispositivos(): Flow<List<DispositivoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDispositivo(dispositivo: DispositivoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDispositivos(dispositivos: List<DispositivoEntity>)

    @Delete
    suspend fun eliminarDispositivo(dispositivo: DispositivoEntity)

    @Query("DELETE FROM dispositivos WHERE id = :id")
    suspend fun eliminarDispositivoPorId(id: String)

    // -------------------------------------------------------------------------
    // Checklist de Acciones Preventivas (RF-05 / RN-02)
    // -------------------------------------------------------------------------

    @Query("SELECT * FROM checklist_acciones ORDER BY id ASC")
    fun obtenerChecklist(): Flow<List<AccionChecklistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAccion(accion: AccionChecklistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAcciones(acciones: List<AccionChecklistEntity>)

    @Update
    suspend fun actualizarAccion(accion: AccionChecklistEntity)

    @Query("UPDATE checklist_acciones SET completada = :completada WHERE id = :id")
    suspend fun alternarEstadoAccion(id: String, completada: Boolean)

    @Delete
    suspend fun eliminarAccion(accion: AccionChecklistEntity)

    // -------------------------------------------------------------------------
    // Historial de Evaluaciones Preventivas (RF-06)
    // -------------------------------------------------------------------------

    @Query("SELECT * FROM evaluaciones ORDER BY id DESC")
    fun obtenerHistorialEvaluaciones(): Flow<List<EvaluacionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEvaluacion(evaluacion: EvaluacionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEvaluaciones(evaluaciones: List<EvaluacionEntity>)

    @Query("SELECT * FROM evaluaciones ORDER BY id DESC LIMIT 1")
    fun obtenerUltimaEvaluacion(): Flow<EvaluacionEntity?>
}
