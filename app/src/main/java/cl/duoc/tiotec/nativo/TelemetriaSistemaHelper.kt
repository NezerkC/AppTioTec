package cl.duoc.tiotec.nativo

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.util.DisplayMetrics
import cl.duoc.tiotec.model.AlmacenamientoInfo
import cl.duoc.tiotec.model.BateriaInfo
import cl.duoc.tiotec.model.CamaraHardwareInfo
import cl.duoc.tiotec.model.DiagnosticoSistema
import cl.duoc.tiotec.model.EstadoGlobalHardware
import cl.duoc.tiotec.model.FlujoEnergeticoDispositivo
import cl.duoc.tiotec.model.InventarioHardware
import cl.duoc.tiotec.model.InventarioSensores
import cl.duoc.tiotec.model.NivelRiesgo
import cl.duoc.tiotec.model.PantallaHardwareInfo
import cl.duoc.tiotec.model.RamInfo
import cl.duoc.tiotec.model.SensorHardwareInfo
import cl.duoc.tiotec.model.TelemetriaSistema
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Helper nativo de telemetría de hardware estable (Caso TíoTec Preventivo).
 * Proporciona métodos para capturar datos de Batería, RAM, Almacenamiento y Sensores.
 */
object TelemetriaSistemaHelper {

    private const val BYTES_POR_MB_LONG = 1024L * 1024L
    private const val BYTES_POR_GB_DOUBLE = 1024.0 * 1024.0 * 1024.0

    /**
     * Captura el diagnóstico completo y estable del hardware del dispositivo.
     */
    fun capturarDiagnostico(context: Context): DiagnosticoSistema {
        val bateria = obtenerBateria(context)
        val ram = obtenerRam(context)
        val almacenamiento = obtenerAlmacenamiento()
        val sensores = obtenerSensores(context)
        val nivelRiesgo = evaluarNivelRiesgo(bateria, ram, almacenamiento)
        val timestamp = System.currentTimeMillis()
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

        return DiagnosticoSistema(
            timestampMs = timestamp,
            fechaFormateada = fecha,
            bateria = bateria,
            ram = ram,
            almacenamiento = almacenamiento,
            sensores = sensores,
            nivelRiesgo = nivelRiesgo,
            sincronizadoRemoto = false,
            idRemoto = null
        )
    }

    /**
     * Lee métricas de batería con [IntentFilter(Intent.ACTION_BATTERY_CHANGED)].
     */
    fun obtenerBateria(context: Context): BateriaInfo {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)

        val nivel = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val escala = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val nivelPorcentaje = if (nivel >= 0 && escala > 0) {
            ((nivel.toFloat() / escala.toFloat()) * 100f).roundToInt()
        } else {
            100
        }

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val estaCargando = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val tempDecimas = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 280) ?: 280
        val temperaturaCelsius = tempDecimas / 10.0f
        val voltajeMv = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4000) ?: 4000

        val plugged = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val fuente = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "Cargador CA"
            BatteryManager.BATTERY_PLUGGED_USB -> "Puerto USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Carga Inalámbrica"
            else -> "Batería"
        }

        return BateriaInfo(
            nivelPorcentaje = nivelPorcentaje,
            temperaturaCelsius = temperaturaCelsius,
            voltajeMv = voltajeMv,
            estaCargando = estaCargando,
            fuenteEnergia = fuente
        )
    }

    /**
     * Lee el estado de la memoria RAM con [ActivityManager.MemoryInfo].
     */
    fun obtenerRam(context: Context): RamInfo {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        return if (am != null) {
            am.getMemoryInfo(memoryInfo)
            RamInfo(
                totalMb = memoryInfo.totalMem / BYTES_POR_MB_LONG,
                disponibleMb = memoryInfo.availMem / BYTES_POR_MB_LONG,
                memoriaBajaSistema = memoryInfo.lowMemory
            )
        } else {
            RamInfo(totalMb = 4096L, disponibleMb = 2048L, memoriaBajaSistema = false)
        }
    }

    /**
     * Lee almacenamiento interno con [StatFs].
     */
    fun obtenerAlmacenamiento(): AlmacenamientoInfo {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes = stat.blockCountLong * stat.blockSizeLong
            val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
            val totalGb = ((totalBytes.toDouble() / BYTES_POR_GB_DOUBLE) * 10.0).roundToInt() / 10.0
            val libreGb = ((freeBytes.toDouble() / BYTES_POR_GB_DOUBLE) * 10.0).roundToInt() / 10.0
            AlmacenamientoInfo(totalGb = totalGb, libreGb = libreGb)
        } catch (_: Exception) {
            AlmacenamientoInfo(totalGb = 64.0, libreGb = 32.0)
        }
    }

    /**
     * Lista los sensores de hardware físicos y su consumo eléctrico teórico en mA ([Sensor.getPower]).
     */
    fun obtenerSensores(context: Context): InventarioSensores {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            ?: return InventarioSensores()

        val sensorList = sm.getSensorList(Sensor.TYPE_ALL) ?: emptyList()
        val listaSensores = sensorList.map { sensor ->
            SensorHardwareInfo(
                nombre = sensor.name ?: "Sensor Desconocido",
                fabricante = sensor.vendor ?: "Desconocido",
                tipo = mapearTipoSensor(sensor.type),
                consumoMa = sensor.power
            )
        }
        return InventarioSensores(sensores = listaSensores)
    }

    /**
     * Evalúa el nivel de riesgo en base a los umbrales de seguridad preventiva.
     */
    fun evaluarNivelRiesgo(
        bateria: BateriaInfo,
        ram: RamInfo,
        almacenamiento: AlmacenamientoInfo
    ): NivelRiesgo {
        val alertasCriticas = (if (bateria.temperaturaAlerta) 1 else 0) +
                (if (bateria.nivelPorcentaje <= 10 && !bateria.estaCargando) 1 else 0) +
                (if (ram.ramAlerta) 1 else 0) +
                (if (almacenamiento.almacenamientoAlerta) 1 else 0)

        return when {
            alertasCriticas >= 2 -> NivelRiesgo.ALTO
            alertasCriticas == 1 || bateria.nivelPorcentaje <= 20 -> NivelRiesgo.MEDIO
            else -> NivelRiesgo.BAJO
        }
    }

    /**
     * Proporciona la estructura legacy de TelemetriaSistema para compatibilidad hacia atrás.
     */
    fun obtenerTelemetria(context: Context): TelemetriaSistema {
        val bateria = obtenerBateria(context)
        val ram = obtenerRam(context)
        val almacenamiento = obtenerAlmacenamiento()
        val sensores = obtenerSensores(context)
        val nivelRiesgo = evaluarNivelRiesgo(bateria, ram, almacenamiento)

        val estadoGlobal = EstadoGlobalHardware(
            ramTotalMb = ram.totalMb,
            ramDisponibleMb = ram.disponibleMb,
            memoriaBajaSistema = ram.memoriaBajaSistema,
            almacenamientoTotalGb = almacenamiento.totalGb,
            almacenamientoLibreGb = almacenamiento.libreGb,
            porcentajeAlmacenamientoUsado = almacenamiento.porcentajeUsado
        )

        val flujo = FlujoEnergeticoDispositivo(
            nivelBateria = bateria.nivelPorcentaje,
            voltajeMv = bateria.voltajeMv,
            temperaturaCelsius = bateria.temperaturaCelsius,
            estaCargando = bateria.estaCargando,
            fuenteEnergia = bateria.fuenteEnergia
        )

        return TelemetriaSistema(
            estadoGlobal = estadoGlobal,
            flujoEnergetico = flujo,
            nivelRiesgo = nivelRiesgo,
            inventarioHardware = InventarioHardware(sensores = sensores.sensores),
            timestampMs = System.currentTimeMillis()
        )
    }

    private fun mapearTipoSensor(type: Int): String = when (type) {
        Sensor.TYPE_ACCELEROMETER -> "Acelerómetro"
        Sensor.TYPE_MAGNETIC_FIELD -> "Magnetómetro (Brújula)"
        Sensor.TYPE_GYROSCOPE -> "Giroscopio"
        Sensor.TYPE_LIGHT -> "Sensor de Luz Ambiental"
        Sensor.TYPE_PRESSURE -> "Barómetro"
        Sensor.TYPE_PROXIMITY -> "Sensor de Proximidad"
        Sensor.TYPE_GRAVITY -> "Sensor de Gravedad"
        Sensor.TYPE_LINEAR_ACCELERATION -> "Aceleración Lineal"
        Sensor.TYPE_ROTATION_VECTOR -> "Vector de Rotación"
        Sensor.TYPE_STEP_COUNTER -> "Contador de Pasos"
        Sensor.TYPE_STEP_DETECTOR -> "Detector de Pasos"
        Sensor.TYPE_HEART_RATE -> "Sensor de Ritmo Cardíaco"
        else -> "Sensor Físico (Tipo $type)"
    }
}
