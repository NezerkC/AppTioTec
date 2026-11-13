package cl.duoc.tiotec.data.remote

import cl.duoc.tiotec.model.DiagnosticoHardwareRegistro
import cl.duoc.tiotec.model.NivelRiesgo
import com.google.gson.annotations.SerializedName

data class DiagnosticoHardwareRequestDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("fechaRegistro")
    val fechaRegistro: String,
    @SerializedName("timestampMs")
    val timestampMs: Long,
    @SerializedName("nivelBateria")
    val nivelBateria: Int,
    @SerializedName("estaCargando")
    val estaCargando: Boolean,
    @SerializedName("temperaturaBateria")
    val temperaturaBateria: Float,
    @SerializedName("voltajeBateria")
    val voltajeBateria: Int,
    @SerializedName("saludBateria")
    val saludBateria: String,
    @SerializedName("fuenteEnergia")
    val fuenteEnergia: String,
    @SerializedName("almacenamientoTotalGb")
    val almacenamientoTotalGb: Double,
    @SerializedName("almacenamientoLibreGb")
    val almacenamientoLibreGb: Double,
    @SerializedName("porcentajeAlmacenamientoUsado")
    val porcentajeAlmacenamientoUsado: Int,
    @SerializedName("ramTotalMb")
    val ramTotalMb: Long,
    @SerializedName("ramDisponibleMb")
    val ramDisponibleMb: Long,
    @SerializedName("totalSensores")
    val totalSensores: Int,
    @SerializedName("consumoSensoresMa")
    val consumoSensoresMa: Double,
    @SerializedName("nivelRiesgo")
    val nivelRiesgo: String,
    @SerializedName("recomendacion")
    val recomendacion: String
)

data class DiagnosticoHardwareResponseDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("fechaRegistro")
    val fechaRegistro: String,
    @SerializedName("timestampMs")
    val timestampMs: Long,
    @SerializedName("nivelBateria")
    val nivelBateria: Int,
    @SerializedName("estaCargando")
    val estaCargando: Boolean,
    @SerializedName("temperaturaBateria")
    val temperaturaBateria: Float,
    @SerializedName("voltajeBateria")
    val voltajeBateria: Int,
    @SerializedName("saludBateria")
    val saludBateria: String,
    @SerializedName("fuenteEnergia")
    val fuenteEnergia: String,
    @SerializedName("almacenamientoTotalGb")
    val almacenamientoTotalGb: Double,
    @SerializedName("almacenamientoLibreGb")
    val almacenamientoLibreGb: Double,
    @SerializedName("porcentajeAlmacenamientoUsado")
    val porcentajeAlmacenamientoUsado: Int,
    @SerializedName("ramTotalMb")
    val ramTotalMb: Long,
    @SerializedName("ramDisponibleMb")
    val ramDisponibleMb: Long,
    @SerializedName("totalSensores")
    val totalSensores: Int,
    @SerializedName("consumoSensoresMa")
    val consumoSensoresMa: Double,
    @SerializedName("nivelRiesgo")
    val nivelRiesgo: String,
    @SerializedName("recomendacion")
    val recomendacion: String,
    @SerializedName("sincronizadoRemoto")
    val sincronizadoRemoto: Boolean = true
)

fun DiagnosticoHardwareRegistro.toRequestDto(): DiagnosticoHardwareRequestDto =
    DiagnosticoHardwareRequestDto(
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
        nivelRiesgo = nivelRiesgo.name,
        recomendacion = recomendacion
    )

fun DiagnosticoHardwareResponseDto.toDomain(): DiagnosticoHardwareRegistro {
    val nivelEnum = runCatching { NivelRiesgo.valueOf(nivelRiesgo.uppercase()) }
        .getOrDefault(NivelRiesgo.BAJO)
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
