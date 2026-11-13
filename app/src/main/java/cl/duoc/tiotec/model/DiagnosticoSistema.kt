package cl.duoc.tiotec.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Métricas estables de la batería del dispositivo obtenidas mediante APIs públicas de Android.
 */
data class BateriaInfo(
    val nivelPorcentaje: Int = 100,
    val temperaturaCelsius: Float = 28.0f,
    val voltajeMv: Int = 4000,
    val estaCargando: Boolean = false,
    val fuenteEnergia: String = "Batería"
) {
    val temperaturaAlerta: Boolean
        get() = temperaturaCelsius >= 40.0f

    val voltajeVoltios: Double
        get() = ((voltajeMv.toDouble() / 1000.0) * 100.0).roundToInt() / 100.0
}

/**
 * Métricas estables de la memoria RAM del sistema obtenidas mediante [android.app.ActivityManager.MemoryInfo].
 */
data class RamInfo(
    val totalMb: Long = 4096L,
    val disponibleMb: Long = 2048L,
    val memoriaBajaSistema: Boolean = false
) {
    val usadaMb: Long
        get() = (totalMb - disponibleMb).coerceAtLeast(0L)

    val porcentajeUsado: Int
        get() = if (totalMb > 0) {
            ((usadaMb.toDouble() / totalMb.toDouble()) * 100.0).roundToInt().coerceIn(0, 100)
        } else {
            0
        }

    val ramAlerta: Boolean
        get() = memoriaBajaSistema || porcentajeUsado >= 85
}

/**
 * Métricas estables de almacenamiento interno obtenidas mediante [android.os.StatFs].
 */
data class AlmacenamientoInfo(
    val totalGb: Double = 64.0,
    val libreGb: Double = 32.0
) {
    val usadoGb: Double
        get() = ((totalGb - libreGb).coerceAtLeast(0.0) * 10.0).roundToInt() / 10.0

    val porcentajeUsado: Int
        get() = if (totalGb > 0.0) {
            ((usadoGb / totalGb) * 100.0).roundToInt().coerceIn(0, 100)
        } else {
            0
        }

    val almacenamientoAlerta: Boolean
        get() = porcentajeUsado >= 85
}

/**
 * Información técnica y consumo eléctrico teórico de un sensor físico ([android.hardware.Sensor]).
 */
data class SensorHardwareInfo(
    val nombre: String,
    val fabricante: String,
    val tipo: String,
    val consumoMa: Float,
    val rangoMaximo: Float = 0f,
    val resolucion: Float = 0f
)

/**
 * Inventario de sensores físicos integrados y suma de consumo teórico.
 */
data class InventarioSensores(
    val sensores: List<SensorHardwareInfo> = emptyList()
) {
    val cantidadSensores: Int
        get() = sensores.size

    val consumoTotalSensoresMa: Double
        get() = (sensores.sumOf { it.consumoMa.toDouble() } * 100.0).roundToLong() / 100.0
}

/**
 * Diagnóstico consolidado de hardware estable para Room y Spring Boot.
 */
data class DiagnosticoSistema(
    val idLocal: Long = 0L,
    val timestampMs: Long = System.currentTimeMillis(),
    val fechaFormateada: String = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(timestampMs)),
    val bateria: BateriaInfo = BateriaInfo(),
    val ram: RamInfo = RamInfo(),
    val almacenamiento: AlmacenamientoInfo = AlmacenamientoInfo(),
    val sensores: InventarioSensores = InventarioSensores(),
    val nivelRiesgo: NivelRiesgo = NivelRiesgo.BAJO,
    val sincronizadoRemoto: Boolean = false,
    val idRemoto: Long? = null
)
