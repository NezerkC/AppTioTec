package cl.duoc.tiotec.model

/**
 * Registro persistente e intercambiable del diagnóstico técnico de hardware.
 * Modela tanto el snapshot almacenado localmente en Room como el recurso
 * sincronizado con la API REST Spring Boot.
 */
data class DiagnosticoHardwareRegistro(
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
    val nivelRiesgo: NivelRiesgo,
    val recomendacion: String,
    val sincronizadoRemoto: Boolean = false
) {
    companion object {
        fun fromDiagnosticoSistema(
            sistema: DiagnosticoSistema,
            recomendacion: String = "Diagnóstico estable de hardware preventivo"
        ): DiagnosticoHardwareRegistro {
            return DiagnosticoHardwareRegistro(
                id = "diag_${sistema.timestampMs}",
                fechaRegistro = sistema.fechaFormateada,
                timestampMs = sistema.timestampMs,
                nivelBateria = sistema.bateria.nivelPorcentaje,
                estaCargando = sistema.bateria.estaCargando,
                temperaturaBateria = sistema.bateria.temperaturaCelsius,
                voltajeBateria = sistema.bateria.voltajeMv,
                saludBateria = if (sistema.bateria.temperaturaAlerta) "Atención" else "Buena",
                fuenteEnergia = sistema.bateria.fuenteEnergia,
                almacenamientoTotalGb = sistema.almacenamiento.totalGb,
                almacenamientoLibreGb = sistema.almacenamiento.libreGb,
                porcentajeAlmacenamientoUsado = sistema.almacenamiento.porcentajeUsado,
                ramTotalMb = sistema.ram.totalMb,
                ramDisponibleMb = sistema.ram.disponibleMb,
                totalSensores = sistema.sensores.cantidadSensores,
                consumoSensoresMa = sistema.sensores.consumoTotalSensoresMa,
                nivelRiesgo = sistema.nivelRiesgo,
                recomendacion = recomendacion,
                sincronizadoRemoto = sistema.sincronizadoRemoto
            )
        }
    }
}
