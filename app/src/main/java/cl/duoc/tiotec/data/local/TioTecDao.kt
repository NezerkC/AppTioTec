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
    suspend fun insertarDispositivo(dispositivo: DispositivoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDispositivos(dispositivos: List<DispositivoEntity>): List<Long>

    @Delete
    suspend fun eliminarDispositivo(dispositivo: DispositivoEntity): Int

    @Query("DELETE FROM dispositivos WHERE id = :id")
    suspend fun eliminarDispositivoPorId(id: String): Int

    // -------------------------------------------------------------------------
    // Checklist de Acciones Preventivas (RF-05 / RN-02)
    // -------------------------------------------------------------------------

    @Query("SELECT * FROM checklist_acciones ORDER BY id ASC")
    fun obtenerChecklist(): Flow<List<AccionChecklistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAccion(accion: AccionChecklistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAcciones(acciones: List<AccionChecklistEntity>): List<Long>

    @Update
    suspend fun actualizarAccion(accion: AccionChecklistEntity): Int

    @Query("UPDATE checklist_acciones SET completada = :completada WHERE id = :id")
    suspend fun alternarEstadoAccion(id: String, completada: Boolean): Int

    @Delete
    suspend fun eliminarAccion(accion: AccionChecklistEntity): Int

    // -------------------------------------------------------------------------
    // Historial de Evaluaciones Preventivas (RF-06)
    // -------------------------------------------------------------------------

    @Query("SELECT * FROM evaluaciones ORDER BY id DESC")
    fun obtenerHistorialEvaluaciones(): Flow<List<EvaluacionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEvaluacion(evaluacion: EvaluacionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEvaluaciones(evaluaciones: List<EvaluacionEntity>): List<Long>

    @Query("SELECT * FROM evaluaciones ORDER BY id DESC LIMIT 1")
    fun obtenerUltimaEvaluacion(): Flow<EvaluacionEntity?>

    // -------------------------------------------------------------------------
    // Solicitudes de Asistencia Simulada (Issue #12 · Caso TíoTec 3.2)
    // -------------------------------------------------------------------------

    @Query("SELECT * FROM solicitudes_asistencia ORDER BY id DESC")
    fun obtenerSolicitudesAsistencia(): Flow<List<SolicitudAsistenciaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSolicitudAsistencia(solicitud: SolicitudAsistenciaEntity): Long

    @Query("UPDATE solicitudes_asistencia SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstadoAsistencia(id: String, estado: String): Int
}
