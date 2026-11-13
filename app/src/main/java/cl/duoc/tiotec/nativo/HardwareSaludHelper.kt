package cl.duoc.tiotec.nativo

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import cl.duoc.tiotec.model.DiagnosticoHardware
import cl.duoc.tiotec.model.NivelRiesgo
import java.io.File
import kotlin.math.roundToInt

/**
 * Helper nativo de Hardware y Mantenimiento.
 * Utiliza APIs nativas del SDK de Android:
 *  - [BatteryManager]: Monitoreo de nivel, temperatura, voltaje, estado y salud de la batería.
 *  - [StatFs] / [Environment]: Análisis de espacio total y libre en el almacenamiento interno.
 */
object HardwareSaludHelper {

    /**
     * Obtiene el diagnóstico actual de hardware en tiempo real leyendo los sensores nativos del sistema.
     */
    fun obtenerDiagnostico(context: Context): DiagnosticoHardware {
        val intentBateria = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val nivel = intentBateria?.let { intent ->
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) ((level / scale.toFloat()) * 100).roundToInt() else 100
        } ?: 100

        val estadoBateria = intentBateria?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val estaCargando = estadoBateria == BatteryManager.BATTERY_STATUS_CHARGING ||
                estadoBateria == BatteryManager.BATTERY_STATUS_FULL

        val plugType = intentBateria?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val fuenteEnergia = when (plugType) {
            BatteryManager.BATTERY_PLUGGED_AC -> "Cargador de pared (AC)"
            BatteryManager.BATTERY_PLUGGED_USB -> "Puerto USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Carga inalámbrica"
            else -> if (estaCargando) "Cargando" else "Batería del dispositivo"
        }

        // Temperatura en décimas de grado Celsius (ej: 315 = 31.5 °C)
        val tempRaw = intentBateria?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 280) ?: 280
        val temperaturaCelsius = tempRaw / 10.0f

        val voltajeMv = intentBateria?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4000) ?: 4000

        val healthRaw = intentBateria?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
            ?: BatteryManager.BATTERY_HEALTH_GOOD
        val saludBateria = when (healthRaw) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Buena (Óptima)"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Sobrecalentada"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dañada / Agotada"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Sobretensión"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Falla no especificada"
            BatteryManager.BATTERY_HEALTH_COLD -> "Temperatura muy baja"
            else -> "Normal"
        }

        // Lectura de almacenamiento mediante StatFs
        val rutaDatos: File = Environment.getDataDirectory()
        val statFs = StatFs(rutaDatos.path)
        val bytesTotales = statFs.totalBytes
        val bytesDisponibles = statFs.availableBytes

        val gbTotales = ((bytesTotales / (1024.0 * 1024.0 * 1024.0)) * 10.0).roundToInt() / 10.0
        val gbLibres = ((bytesDisponibles / (1024.0 * 1024.0 * 1024.0)) * 10.0).roundToInt() / 10.0
        val porcentajeUsado = if (bytesTotales > 0) {
            (((bytesTotales - bytesDisponibles) / bytesTotales.toDouble()) * 100).roundToInt()
        } else {
            50
        }

        // Evaluación de reglas de negocio para Cuidado Preventivo
        val (nivelRiesgo, recomendacion) = evaluarCondicionesHardware(
            temperatura = temperaturaCelsius,
            porcentajeAlmacenamiento = porcentajeUsado,
            saludBateria = saludBateria,
            nivelBateria = nivel
        )

        return DiagnosticoHardware(
            nivelBateria = nivel,
            estaCargando = estaCargando,
            temperaturaBateria = temperaturaCelsius,
            voltajeBateria = voltajeMv,
            saludBateria = saludBateria,
            fuenteEnergia = fuenteEnergia,
            almacenamientoTotalGb = gbTotales,
            almacenamientoLibreGb = gbLibres,
            porcentajeAlmacenamientoUsado = porcentajeUsado,
            nivelRiesgo = nivelRiesgo,
            recomendacion = recomendacion
        )
    }

    private fun evaluarCondicionesHardware(
        temperatura: Float,
        porcentajeAlmacenamiento: Int,
        saludBateria: String,
        nivelBateria: Int
    ): Pair<NivelRiesgo, String> {
        return when {
            temperatura >= 43.0f || porcentajeAlmacenamiento >= 92 || saludBateria.contains("Sobrecalentada") -> {
                NivelRiesgo.ALTO to "¡Riesgo crítico de hardware! Temperatura elevada (${temperatura}°C) o almacenamiento al ${porcentajeAlmacenamiento}%. Desconecta el cargador y libera espacio de inmediato."
            }
            temperatura >= 38.0f || porcentajeAlmacenamiento >= 80 || nivelBateria <= 15 -> {
                NivelRiesgo.MEDIO to "Atención preventiva: almacenamiento al ${porcentajeAlmacenamiento}% o temperatura en alza (${temperatura}°C). Se recomienda limpieza de archivos temporales."
            }
            else -> {
                NivelRiesgo.BAJO to "Batería y almacenamiento en estado saludable (${temperatura}°C, ${porcentajeAlmacenamiento}% usado). Mantén buenas prácticas de carga."
            }
        }
    }
}
