package cl.duoc.tiotec.model

/**
 * Representa el tiempo invertido en una aplicación específica durante el día.
 */
data class AppUsoInfo(
    val nombreApp: String,
    val paquete: String,
    val tiempoMinutos: Long
) {
    val tiempoFormateado: String
        get() {
            val horas = tiempoMinutos / 60
            val minutos = tiempoMinutos % 60
            return if (horas > 0) "${horas}h ${minutos}m" else "${minutos}m"
        }
}

/**
 * Modelo de datos inmutable para el bienestar digital y tiempo en pantalla del usuario.
 * Recopilado mediante APIs nativas de Android: UsageStatsManager y AppOpsManager.
 */
data class MetricasBienestarDigital(
    val permisoConcedido: Boolean = false,
    val tiempoTotalPantallaMinutos: Long = 0,
    val appsMasUsadas: List<AppUsoInfo> = emptyList(),
    val nivelRiesgo: NivelRiesgo = NivelRiesgo.BAJO,
    val recomendacion: String = "Tiempo en pantalla equilibrado. Mantén hábitos de desconexión periódicos."
) {
    val horasTotales: Long
        get() = tiempoTotalPantallaMinutos / 60

    val minutosRestantes: Long
        get() = tiempoTotalPantallaMinutos % 60

    val tiempoTotalFormateado: String
        get() = if (horasTotales > 0) "${horasTotales}h ${minutosRestantes}m hoy" else "${minutosRestantes}m hoy"

    val alertaUsoExcesivo: Boolean
        get() = tiempoTotalPantallaMinutos >= 300 // más de 5 horas diarias
}
