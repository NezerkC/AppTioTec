package cl.duoc.tiotec.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.duoc.tiotec.model.EvaluacionSeguridad
import cl.duoc.tiotec.model.NivelRiesgo

@Entity(tableName = "evaluaciones")
data class EvaluacionEntity(
    @PrimaryKey
    val id: String,
    val fecha: String,
    val puntajeTotal: Int,
    val puntajeMaximo: Int,
    val nivelRiesgo: String
) {
    fun toDomain(): EvaluacionSeguridad {
        val nivelEnum = runCatching { NivelRiesgo.valueOf(nivelRiesgo) }.getOrDefault(NivelRiesgo.MEDIO)
        return EvaluacionSeguridad(
            id = id,
            fecha = fecha,
            puntajeTotal = puntajeTotal,
            puntajeMaximo = puntajeMaximo,
            nivelRiesgo = nivelEnum
        )
    }

    companion object {
        fun fromDomain(e: EvaluacionSeguridad): EvaluacionEntity = EvaluacionEntity(
            id = e.id,
            fecha = e.fecha,
            puntajeTotal = e.puntajeTotal,
            puntajeMaximo = e.puntajeMaximo,
            nivelRiesgo = e.nivelRiesgo.name
        )
    }
}
