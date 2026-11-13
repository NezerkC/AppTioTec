package cl.duoc.tiotec.model

import java.util.Locale
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
    val nucleosCpu: Int = 8,
    val arquitecturaCpu: String = "arm64-v8a",
    val modeloDispositivo: String = "Android Device",
    val versionAndroid: String = "14 (API 34)",
    val modoAhorroEnergia: Boolean = false,
    val modeloSoC: String = "ARM / SoC Desconocido",
    val estadoTermicoCodigo: Int = 0,
    val estadoTermicoEtiqueta: String = "Normal (Sin estrés térmico)",
    val margenTermico: Float = 0.25f,
    val alertaThrottling: Boolean = false,
    val almacenamientoTotalGb: Double = 64.0,
    val almacenamientoLibreGb: Double = 32.0,
    val porcentajeAlmacenamientoUsado: Int = 50,
    val redBajadaKbps: Double = 0.0,
    val redSubidaKbps: Double = 0.0,
    val redTotalRxMb: Double = 0.0,
    val redTotalTxMb: Double = 0.0
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

    val redBajadaFormateada: String
        get() = formatearVelocidadRed(redBajadaKbps)

    val redSubidaFormateada: String
        get() = formatearVelocidadRed(redSubidaKbps)
}

/**
 * Flujo energético instantáneo y promedio de la batería del dispositivo.
 */
data class FlujoEnergeticoDispositivo(
    val corrienteInstantaneaMa: Int = -250,
    val corrientePromedioMa: Int = -220,
    val capacidadRemanenteMah: Int = 3200,
    val voltajeMv: Int = 4000,
    val temperaturaCelsius: Float = 28.0f,
    val estaCargando: Boolean = false,
    val fuenteEnergia: String = "Batería del dispositivo",
    val corrienteInstantaneaUa: Int = -250_000,
    val corrientePromedioUa: Int = -220_000
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
 * Métricas de consumo del propio proceso de la aplicación dentro del sandbox de Android.
 */
data class ConsumoPropiaApp(
    val pidProceso: Int = 1000,
    val uidAplicacion: Int = 10150,
    val heapUsadoMb: Double = 18.5,
    val heapAsignadoMb: Double = 32.0,
    val heapMaximoMb: Double = 256.0,
    val memoriaPssTotalMb: Double = 42.0,
    val tiempoCpuMs: Long = 1250L,
    val hilosActivos: Int = 14,
    val porcentajeCpuApp: Float = 0.0f,
    val redRxKbps: Double = 0.0,
    val redTxKbps: Double = 0.0,
    val redTotalAppKb: Double = 0.0,
    val potenciaEstimadaMw: Double = 0.0,
    val consumoBateriaSesionMah: Double = 0.0
) {
    val porcentajeHeapUsado: Int
        get() = if (heapMaximoMb > 0.0) {
            ((heapUsadoMb / heapMaximoMb) * 100.0).roundToInt().coerceIn(0, 100)
        } else {
            0
        }
}

/**
 * Información técnica y consumo eléctrico teórico de un sensor físico integrado.
 */
data class SensorHardwareInfo(
    val nombre: String,
    val fabricante: String,
    val tipo: String,
    val consumoMa: Float,
    val rangoMaximo: Float,
    val resolucion: Float
)

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
 * Snapshot unificado de telemetría del sistema, flujo energético, inventario y consumo propio de la app.
 */
data class TelemetriaSistema(
    val estadoGlobal: EstadoGlobalHardware = EstadoGlobalHardware(),
    val flujoEnergetico: FlujoEnergeticoDispositivo = FlujoEnergeticoDispositivo(),
    val consumoApp: ConsumoPropiaApp = ConsumoPropiaApp(),
    val nivelRiesgo: NivelRiesgo = NivelRiesgo.BAJO,
    val notaSandboxAndroid: String = "Solo con APIs públicas del SDK de Android. Desde Android 8.0, el aislamiento del sistema (sandbox) no permite leer las métricas de otras aplicaciones.",
    val inventarioHardware: InventarioHardware = InventarioHardware(),
    val timestampMs: Long = 0L
)

private fun formatearVelocidadRed(kbps: Double): String {
    val seguro = kbps.coerceAtLeast(0.0)
    return if (seguro >= 1024.0) {
        String.format(Locale.US, "%.2f MB/s", seguro / 1024.0)
    } else {
        String.format(Locale.US, "%.1f KB/s", seguro)
    }
}
