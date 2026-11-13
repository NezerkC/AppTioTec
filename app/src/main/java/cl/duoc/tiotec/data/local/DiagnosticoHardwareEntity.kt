package cl.duoc.tiotec.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.duoc.tiotec.model.DiagnosticoHardwareRegistro
import cl.duoc.tiotec.model.NivelRiesgo

@Entity(tableName = "diagnosticos_hardware")
data class DiagnosticoHardwareEntity(
    @PrimaryKey
    val id: String,
    val fechaRegistro: String,
    val timestampMs: Long,
    val nivelBateria: Int,
    val estaCargando: Boolean,
    val temperaturaBateria: Float,
    val voltajeBateria: Int,
    val saludBateria: String,
    val fuenteEnergia: String,
    val almacenamientoTotalGb: Double,
    val almacenamientoLibreGb: Double,
    val porcentajeAlmacenamientoUsado: Int,
    val ramTotalMb: Long,
    val ramDisponibleMb: Long,
    val totalSensores: Int,
    val consumoSensoresMa: Double,
    val nivelRiesgo: String,
    val recomendacion: String,
    val sincronizadoRemoto: Boolean = false
) {
    fun toDomain(): DiagnosticoHardwareRegistro {
        val nivelEnum = runCatching { NivelRiesgo.valueOf(nivelRiesgo) }.getOrDefault(NivelRiesgo.BAJO)
        return DiagnosticoHardwareRegistro(
            id = id,
            fechaRegistro = fechaRegistro,
            timestampMs = timestampMs,
            nivelBateria = nivelBateria,
            estaCargando = estaCargando,
            temperaturaBateria = temperaturaBateria,
            voltajeBateria = voltajeBateria,
            saludBateria = saludBateria,
            fuenteEnergia = fuenteEnergia,
            almacenamientoTotalGb = almacenamientoTotalGb,
            almacenamientoLibreGb = almacenamientoLibreGb,
            porcentajeAlmacenamientoUsado = porcentajeAlmacenamientoUsado,
            ramTotalMb = ramTotalMb,
            ramDisponibleMb = ramDisponibleMb,
            totalSensores = totalSensores,
            consumoSensoresMa = consumoSensoresMa,
            nivelRiesgo = nivelEnum,
            recomendacion = recomendacion,
            sincronizadoRemoto = sincronizadoRemoto
        )
    }

    companion object {
        fun fromDomain(registro: DiagnosticoHardwareRegistro): DiagnosticoHardwareEntity =
            DiagnosticoHardwareEntity(
                id = registro.id,
                fechaRegistro = registro.fechaRegistro,
                timestampMs = registro.timestampMs,
                nivelBateria = registro.nivelBateria,
                estaCargando = registro.estaCargando,
                temperaturaBateria = registro.temperaturaBateria,
                voltajeBateria = registro.voltajeBateria,
                saludBateria = registro.saludBateria,
                fuenteEnergia = registro.fuenteEnergia,
                almacenamientoTotalGb = registro.almacenamientoTotalGb,
                almacenamientoLibreGb = registro.almacenamientoLibreGb,
                porcentajeAlmacenamientoUsado = registro.porcentajeAlmacenamientoUsado,
                ramTotalMb = registro.ramTotalMb,
                ramDisponibleMb = registro.ramDisponibleMb,
                totalSensores = registro.totalSensores,
                consumoSensoresMa = registro.consumoSensoresMa,
                nivelRiesgo = registro.nivelRiesgo.name,
                recomendacion = registro.recomendacion,
                sincronizadoRemoto = registro.sincronizadoRemoto
            )
    }
}
