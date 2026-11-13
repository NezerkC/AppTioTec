package cl.duoc.tiotec.model

import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Estado global del hardware del dispositivo obtenido mediante APIs públicas del SDK de Android.
 */
data class EstadoGlobalHardware(
    val ramTotalMb: Long = 4096,
    val ramDisponibleMb: Long = 2048,
    val memoriaBajaSistema: Boolean = false,
    val umbralMemoriaBajaMb: Long = 512,
    val modeloDispositivo: String = "Android Device",
    val versionAndroid: String = "14 (API 34)",
    val modoAhorroEnergia: Boolean = false,
    val estadoTermicoCodigo: Int = 0,
    val estadoTermicoEtiqueta: String = "Normal (Sin estrés térmico)",
    val margenTermico: Float = 0.25f,
    val alertaThrottling: Boolean = false,
    val almacenamientoTotalGb: Double = 64.0,
    val almacenamientoLibreGb: Double = 32.0,
    val porcentajeAlmacenamientoUsado: Int = 50
) {
    val ramUsadaMb: Long
        get() = (ramTotalMb - ramDisponibleMb).coerceAtLeast(0L)

    val porcentajeRamUsado: Int
        get() = if (ramTotalMb > 0) {
            ((ramUsadaMb.toDouble() / ramTotalMb.toDouble()) * 100.0).roundToInt().coerceIn(0, 100)
        } else {
            0
        }

    val ramAlerta: Boolean
        get() = memoriaBajaSistema || porcentajeRamUsado >= 85
}

/**
 * Flujo energético instantáneo y promedio de la batería del dispositivo.
 */
data class FlujoEnergeticoDispositivo(
    val nivelBateria: Int = 100,
    val corrienteInstantaneaMa: Int = -250,
    val corrientePromedioMa: Int = -220,
    val capacidadRemanenteMah: Int = 3200,
    val voltajeMv: Int = 4000,
    val temperaturaCelsius: Float = 28.0f,
    val estaCargando: Boolean = false,
    val fuenteEnergia: String = "Batería del dispositivo"
) {
    val potenciaWatts: Double
        get() {
            val watts = (voltajeMv.toDouble() * abs(corrienteInstantaneaMa).toDouble()) / 1_000_000.0
            return (watts * 100.0).roundToInt() / 100.0
        }

    val direccionFlujo: String
        get() = if (estaCargando || corrienteInstantaneaMa > 0) {
            "Entrada (Carga)"
        } else {
            "Salida (Descarga)"
        }

    val descargaAltaAlerta: Boolean
        get() = !estaCargando && abs(corrienteInstantaneaMa) >= 800
}

/**
 * Información descriptiva de una cámara física expuesta por Camera2.
 */
data class CamaraHardwareInfo(
    val id: String,
    val ubicacion: String,
    val nivelHardware: String,
    val tieneFlash: Boolean
)

/**
 * Información de pantalla y tasas de refresco soportadas por el dispositivo.
 */
data class PantallaHardwareInfo(
    val id: Int,
    val nombre: String,
    val resolucion: String,
    val tasaRefrescoHz: Float,
    val tasasSoportadasHz: List<Float>
)

/**
 * Inventario de hardware físico (pantallas, cámaras y sensores) del dispositivo.
 */
data class InventarioHardware(
    val pantallas: List<PantallaHardwareInfo> = emptyList(),
    val camaras: List<CamaraHardwareInfo> = emptyList(),
    val sensores: List<SensorHardwareInfo> = emptyList()
) {
    val consumoTotalSensoresMa: Double
        get() = (sensores.sumOf { it.consumoMa.toDouble() } * 100.0).roundToLong() / 100.0
}

/**
 * Snapshot unificado de telemetría del sistema, flujo energético e inventario físico de hardware.
 */
data class TelemetriaSistema(
    val estadoGlobal: EstadoGlobalHardware = EstadoGlobalHardware(),
    val flujoEnergetico: FlujoEnergeticoDispositivo = FlujoEnergeticoDispositivo(),
    val nivelRiesgo: NivelRiesgo = NivelRiesgo.BAJO,
    val inventarioHardware: InventarioHardware = InventarioHardware(),
    val timestampMs: Long = 0L
)
