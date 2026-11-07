package cl.duoc.tiotec.model

/**
 * Modelo de datos inmutable que representa el estado técnico y físico del dispositivo.
 * Recopilado mediante APIs nativas de Android: BatteryManager y StorageManager / StatFs.
 */
data class DiagnosticoHardware(
    val nivelBateria: Int = 100,
    val estaCargando: Boolean = false,
    val temperaturaBateria: Float = 28.0f, // en grados Celsius (°C)
    val voltajeBateria: Int = 4000,        // en milivoltios (mV)
    val saludBateria: String = "Buena",
    val fuenteEnergia: String = "Batería",
    val almacenamientoTotalGb: Double = 64.0,
    val almacenamientoLibreGb: Double = 32.0,
    val porcentajeAlmacenamientoUsado: Int = 50,
    val nivelRiesgo: NivelRiesgo = NivelRiesgo.BAJO,
    val recomendacion: String = "El hardware y la batería se encuentran en condiciones óptimas de funcionamiento."
) {
    val almacenamientoUsadoGb: Double
        get() = (almacenamientoTotalGb - almacenamientoLibreGb).coerceAtLeast(0.0)

    val temperaturaAlerta: Boolean
        get() = temperaturaBateria >= 40.0f

    val almacenamientoAlerta: Boolean
        get() = porcentajeAlmacenamientoUsado >= 85
}
