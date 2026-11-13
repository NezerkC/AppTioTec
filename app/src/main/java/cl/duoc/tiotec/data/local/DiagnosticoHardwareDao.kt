package cl.duoc.tiotec.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO para persistencia local de snapshots de diagnóstico de hardware en Room.
 */
@Dao
interface DiagnosticoHardwareDao {

    @Query("SELECT * FROM diagnosticos_hardware ORDER BY timestampMs DESC")
    fun obtenerHistorialDiagnosticos(): Flow<List<DiagnosticoHardwareEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDiagnostico(diagnostico: DiagnosticoHardwareEntity): Long

    @Query("UPDATE diagnosticos_hardware SET sincronizadoRemoto = 1 WHERE id = :id")
    suspend fun marcarSincronizado(id: String): Int

    @Query("DELETE FROM diagnosticos_hardware")
    suspend fun eliminarHistorialDiagnosticos(): Int
}
