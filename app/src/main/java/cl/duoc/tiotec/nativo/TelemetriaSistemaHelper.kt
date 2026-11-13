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
import android.net.TrafficStats
import android.os.BatteryManager
import android.os.Build
import android.os.Debug
import android.os.Environment
import android.os.PowerManager
import android.os.Process
import android.os.StatFs
import android.os.SystemClock
import android.util.DisplayMetrics
import cl.duoc.tiotec.model.CamaraHardwareInfo
import cl.duoc.tiotec.model.ConsumoPropiaApp
import cl.duoc.tiotec.model.EstadoGlobalHardware
import cl.duoc.tiotec.model.FlujoEnergeticoDispositivo
import cl.duoc.tiotec.model.InventarioHardware
import cl.duoc.tiotec.model.NivelRiesgo
import cl.duoc.tiotec.model.PantallaHardwareInfo
import cl.duoc.tiotec.model.SensorHardwareInfo
import cl.duoc.tiotec.model.TelemetriaSistema
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Helper nativo de telemetría del sistema, inventario físico y perfilado del propio proceso.
 * Utiliza exclusivamente APIs públicas del SDK de Android (sin reflexión ni APIs ocultas):
 *  - [ActivityManager.MemoryInfo] y [StatFs]: Memoria RAM y almacenamiento interno.
 *  - [PowerManager]: Modo de ahorro, estado térmico (API 29+) y thermal headroom (API 30+).
 *  - [BatteryManager] + [Intent.ACTION_BATTERY_CHANGED]: Corriente en µA/mA, capacidad, voltaje y temperatura.
 *  - [TrafficStats], [Process], [Runtime] y [Debug.MemoryInfo]: Red global/UID, CPU % y memoria propia.
 *  - [DisplayManager], [CameraManager] y [SensorManager]: Inventario de pantallas, cámaras y sensores.
 */
object TelemetriaSistemaHelper {

    private const val BYTES_POR_MB_LONG = 1024L * 1024L
    private const val BYTES_POR_MB_DOUBLE = 1024.0 * 1024.0
    private const val BYTES_POR_GB_DOUBLE = 1024.0 * 1024.0 * 1024.0
    private const val INTERVALO_MINIMO_MUESTRA_MS = 200L

    private var ultimoTimestampMuestraMs: Long = -1L
    private var ultimoTotalRxBytes: Long = 0L
    private var ultimoTotalTxBytes: Long = 0L
    private var ultimoAppRxBytes: Long = 0L
    private var ultimoAppTxBytes: Long = 0L
    private var ultimoTiempoCpuAppMs: Long = 0L

    private var ultimaRedGlobalBajadaKbps: Double = 0.0
    private var ultimaRedGlobalSubidaKbps: Double = 0.0
    private var ultimaRedAppRxKbps: Double = 0.0
    private var ultimaRedAppTxKbps: Double = 0.0
    private var ultimoPorcentajeCpuApp: Float = 0.0f
    private var consumoBateriaSesionMahAcumulado: Double = 0.0

    @Volatile
    private var inventarioCache: InventarioHardware? = null

    private data class InfoTermica(
        val codigo: Int,
        val etiqueta: String,
        val margenTermico: Float,
        val alertaThrottling: Boolean
    )

    private data class InfoAlmacenamiento(
        val totalGb: Double,
        val libreGb: Double,
        val porcentajeUsado: Int
    )

    private data class MuestraRendimientoVivo(
        val redBajadaKbps: Double,
        val redSubidaKbps: Double,
        val redTotalRxMb: Double,
        val redTotalTxMb: Double,
        val redAppRxKbps: Double,
        val redAppTxKbps: Double,
        val redTotalAppKb: Double,
        val porcentajeCpuApp: Float,
        val tiempoCpuMs: Long,
        val potenciaEstimadaMw: Double,
        val consumoBateriaSesionMah: Double
    )

    /**
     * Recopila en tiempo real la telemetría global del dispositivo, su flujo energético,
     * el consumo de memoria/CPU/red de la propia aplicación y el inventario de hardware físico.
     */
    fun obtenerTelemetria(context: Context): TelemetriaSistema {
        val flujoEnergetico = obtenerFlujoEnergetico(context)
        val nucleosCpu = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        val muestraViva = capturarMuestraRendimientoVivo(
            voltajeMv = flujoEnergetico.voltajeMv,
            nucleosCpu = nucleosCpu
        )
        val estadoGlobal = obtenerEstadoGlobalHardware(
            context = context,
            nucleosCpu = nucleosCpu,
            temperaturaBateriaCelsius = flujoEnergetico.temperaturaCelsius,
            muestraViva = muestraViva
        )
        val consumoApp = obtenerConsumoPropiaApp(muestraViva)
        val inventarioHardware = obtenerInventarioHardware(context)
        val nivelRiesgo = evaluarNivelRiesgo(
            estadoGlobal = estadoGlobal,
            flujoEnergetico = flujoEnergetico,
            consumoApp = consumoApp
        )

        return TelemetriaSistema(
            estadoGlobal = estadoGlobal,
            flujoEnergetico = flujoEnergetico,
            consumoApp = consumoApp,
            nivelRiesgo = nivelRiesgo,
            inventarioHardware = inventarioHardware,
            timestampMs = System.currentTimeMillis()
        )
    }

    private fun obtenerEstadoGlobalHardware(
        context: Context,
        nucleosCpu: Int,
        temperaturaBateriaCelsius: Float,
        muestraViva: MuestraRendimientoVivo
    ): EstadoGlobalHardware {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()

        val ramTotalMb: Long
        val ramDisponibleMb: Long
        val memoriaBajaSistema: Boolean
        val umbralMemoriaBajaMb: Long

        if (activityManager != null) {
            activityManager.getMemoryInfo(memoryInfo)
            ramTotalMb = memoryInfo.totalMem / BYTES_POR_MB_LONG
            ramDisponibleMb = memoryInfo.availMem / BYTES_POR_MB_LONG
            memoriaBajaSistema = memoryInfo.lowMemory
            umbralMemoriaBajaMb = memoryInfo.threshold / BYTES_POR_MB_LONG
        } else {
            ramTotalMb = 4096L
            ramDisponibleMb = 2048L
            memoriaBajaSistema = false
            umbralMemoriaBajaMb = 512L
        }

        val arquitecturaCpu = Build.SUPPORTED_ABIS.firstOrNull()?.takeIf { it.isNotBlank() } ?: "arm64-v8a"
        val modeloDispositivo = formatearModeloDispositivo()
        val modeloSoC = formatearModeloSoC()
        val versionAndroid = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val modoAhorroEnergia = powerManager?.isPowerSaveMode ?: false
        val infoTermica = obtenerInfoTermica(powerManager, temperaturaBateriaCelsius)
        val almacenamiento = obtenerAlmacenamientoInterno()

        return EstadoGlobalHardware(
            ramTotalMb = ramTotalMb,
            ramDisponibleMb = ramDisponibleMb,
            memoriaBajaSistema = memoriaBajaSistema,
            umbralMemoriaBajaMb = umbralMemoriaBajaMb,
            nucleosCpu = nucleosCpu,
            arquitecturaCpu = arquitecturaCpu,
            modeloDispositivo = modeloDispositivo,
            versionAndroid = versionAndroid,
            modoAhorroEnergia = modoAhorroEnergia,
            modeloSoC = modeloSoC,
            estadoTermicoCodigo = infoTermica.codigo,
            estadoTermicoEtiqueta = infoTermica.etiqueta,
            margenTermico = infoTermica.margenTermico,
            alertaThrottling = infoTermica.alertaThrottling,
            almacenamientoTotalGb = almacenamiento.totalGb,
            almacenamientoLibreGb = almacenamiento.libreGb,
            porcentajeAlmacenamientoUsado = almacenamiento.porcentajeUsado,
            redBajadaKbps = muestraViva.redBajadaKbps,
            redSubidaKbps = muestraViva.redSubidaKbps,
            redTotalRxMb = muestraViva.redTotalRxMb,
            redTotalTxMb = muestraViva.redTotalTxMb
        )
    }

    private fun obtenerInfoTermica(
        powerManager: PowerManager?,
        temperaturaBateriaCelsius: Float
    ): InfoTermica {
        val codigoTermico = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            runCatching { powerManager.currentThermalStatus }
                .getOrElse { fallbackCodigoTermicoPorTemperatura(temperaturaBateriaCelsius) }
        } else {
            fallbackCodigoTermicoPorTemperatura(temperaturaBateriaCelsius)
        }

        val margenTermico = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && powerManager != null) {
            val raw = runCatching { powerManager.getThermalHeadroom(0) }.getOrDefault(Float.NaN)
            if (raw.isNaN() || raw < 0f) -1f else raw
        } else {
            -1f
        }

        val alertaThrottling = codigoTermico >= 2 || (margenTermico >= 0.90f)
        return InfoTermica(
            codigo = codigoTermico,
            etiqueta = etiquetaTermicaDesdeCodigo(codigoTermico),
            margenTermico = margenTermico,
            alertaThrottling = alertaThrottling
        )
    }

    private fun fallbackCodigoTermicoPorTemperatura(temperaturaCelsius: Float): Int = when {
        temperaturaCelsius >= 42f -> 3
        temperaturaCelsius >= 38f -> 2
        else -> 0
    }

    @Suppress("InlinedApi")
    private fun etiquetaTermicaDesdeCodigo(codigo: Int): String = when (codigo) {
        PowerManager.THERMAL_STATUS_NONE -> "Normal (Sin estrés térmico)"
        PowerManager.THERMAL_STATUS_LIGHT -> "Estrés térmico leve"
        PowerManager.THERMAL_STATUS_MODERATE -> "Moderado · Riesgo de thermal throttling"
        PowerManager.THERMAL_STATUS_SEVERE -> "Severo · Thermal throttling activo"
        PowerManager.THERMAL_STATUS_CRITICAL,
        PowerManager.THERMAL_STATUS_EMERGENCY,
        PowerManager.THERMAL_STATUS_SHUTDOWN -> "Crítico · Estrés térmico máximo"
        else -> "Desconocido"
    }

    private fun obtenerAlmacenamientoInterno(): InfoAlmacenamiento {
        return runCatching {
            val statFs = StatFs(Environment.getDataDirectory().path)
            val totalBytes = statFs.totalBytes
            val freeBytes = statFs.availableBytes
            val totalGb = redondearA1Decimal(totalBytes / BYTES_POR_GB_DOUBLE)
            val libreGb = redondearA1Decimal(freeBytes / BYTES_POR_GB_DOUBLE)
            val usadoPorcentaje = if (totalBytes > 0L) {
                (((totalBytes - freeBytes).toDouble() / totalBytes.toDouble()) * 100.0)
                    .roundToInt()
                    .coerceIn(0, 100)
            } else {
                0
            }
            InfoAlmacenamiento(totalGb = totalGb, libreGb = libreGb, porcentajeUsado = usadoPorcentaje)
        }.getOrElse {
            InfoAlmacenamiento(totalGb = 64.0, libreGb = 32.0, porcentajeUsado = 50)
        }
    }

    private fun obtenerFlujoEnergetico(context: Context): FlujoEnergeticoDispositivo {
        val intentBateria = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

        val nivelBateria = intentBateria?.let { intent ->
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

        val tempRaw = intentBateria?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 280) ?: 280
        val temperaturaCelsius = tempRaw / 10.0f
        val voltajeMv = intentBateria?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4000) ?: 4000

        val corrienteAhoraRaw = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            ?: Int.MIN_VALUE
        val corrienteInstantaneaUa = normalizarCorrienteUa(
            raw = corrienteAhoraRaw,
            fallbackUa = if (estaCargando) 1_200_000 else -240_000
        )

        val corrientePromedioRaw = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
            ?: Int.MIN_VALUE
        val corrientePromedioUa = normalizarCorrienteUa(
            raw = corrientePromedioRaw,
            fallbackUa = if (estaCargando) 1_100_000 else -210_000
        )

        val corrienteInstantaneaMa = corrienteInstantaneaUa / 1000
        val corrientePromedioMa = corrientePromedioUa / 1000

        val capacidadRemanenteMah = obtenerCapacidadRemanenteMah(
            batteryManager = batteryManager,
            nivelBateria = nivelBateria
        )

        return FlujoEnergeticoDispositivo(
            corrienteInstantaneaMa = corrienteInstantaneaMa,
            corrientePromedioMa = corrientePromedioMa,
            capacidadRemanenteMah = capacidadRemanenteMah,
            voltajeMv = voltajeMv,
            temperaturaCelsius = temperaturaCelsius,
            estaCargando = estaCargando,
            fuenteEnergia = fuenteEnergia,
            corrienteInstantaneaUa = corrienteInstantaneaUa,
            corrientePromedioUa = corrientePromedioUa
        )
    }

    private fun obtenerConsumoPropiaApp(muestraViva: MuestraRendimientoVivo): ConsumoPropiaApp {
        val runtime = Runtime.getRuntime()
        val heapAsignadoBytes = runtime.totalMemory()
        val heapLibreBytes = runtime.freeMemory()
        val heapUsadoBytes = (heapAsignadoBytes - heapLibreBytes).coerceAtLeast(0L)
        val heapMaximoBytes = runtime.maxMemory()

        val heapUsadoMb = redondearA1Decimal(heapUsadoBytes / BYTES_POR_MB_DOUBLE)
        val heapAsignadoMb = redondearA1Decimal(heapAsignadoBytes / BYTES_POR_MB_DOUBLE)
        val heapMaximoMb = redondearA1Decimal(heapMaximoBytes / BYTES_POR_MB_DOUBLE)

        val debugMemoryInfo = Debug.MemoryInfo().also { Debug.getMemoryInfo(it) }
        val pssRawMb = debugMemoryInfo.totalPss / 1024.0
        val memoriaPssTotalMb = if (pssRawMb > 0.0) {
            redondearA1Decimal(pssRawMb)
        } else {
            redondearA1Decimal(heapAsignadoMb + 18.0)
        }

        return ConsumoPropiaApp(
            pidProceso = Process.myPid(),
            uidAplicacion = Process.myUid(),
            heapUsadoMb = heapUsadoMb,
            heapAsignadoMb = heapAsignadoMb,
            heapMaximoMb = heapMaximoMb,
            memoriaPssTotalMb = memoriaPssTotalMb,
            tiempoCpuMs = muestraViva.tiempoCpuMs,
            hilosActivos = Thread.activeCount(),
            porcentajeCpuApp = muestraViva.porcentajeCpuApp,
            redRxKbps = muestraViva.redAppRxKbps,
            redTxKbps = muestraViva.redAppTxKbps,
            redTotalAppKb = muestraViva.redTotalAppKb,
            potenciaEstimadaMw = muestraViva.potenciaEstimadaMw,
            consumoBateriaSesionMah = muestraViva.consumoBateriaSesionMah
        )
    }

    private fun capturarMuestraRendimientoVivo(
        voltajeMv: Int,
        nucleosCpu: Int
    ): MuestraRendimientoVivo {
        val nowElapsedMs = SystemClock.elapsedRealtime()
        val uid = Process.myUid()
        val tiempoCpuMs = Process.getElapsedCpuTime().coerceAtLeast(0L)

        val totalRxBytes = leerTrafficBytesSeguro { TrafficStats.getTotalRxBytes() }
        val totalTxBytes = leerTrafficBytesSeguro { TrafficStats.getTotalTxBytes() }
        val appRxBytes = leerTrafficBytesSeguro { TrafficStats.getUidRxBytes(uid) }
        val appTxBytes = leerTrafficBytesSeguro { TrafficStats.getUidTxBytes(uid) }

        return synchronized(this) {
            if (ultimoTimestampMuestraMs < 0L) {
                ultimoTimestampMuestraMs = nowElapsedMs
                ultimoTotalRxBytes = totalRxBytes
                ultimoTotalTxBytes = totalTxBytes
                ultimoAppRxBytes = appRxBytes
                ultimoAppTxBytes = appTxBytes
                ultimoTiempoCpuAppMs = tiempoCpuMs
            } else {
                val deltaWallMs = nowElapsedMs - ultimoTimestampMuestraMs
                if (deltaWallMs >= INTERVALO_MINIMO_MUESTRA_MS) {
                    val deltaSec = deltaWallMs / 1000.0
                    val deltaTotalRx = (totalRxBytes - ultimoTotalRxBytes).coerceAtLeast(0L)
                    val deltaTotalTx = (totalTxBytes - ultimoTotalTxBytes).coerceAtLeast(0L)
                    val deltaAppRx = (appRxBytes - ultimoAppRxBytes).coerceAtLeast(0L)
                    val deltaAppTx = (appTxBytes - ultimoAppTxBytes).coerceAtLeast(0L)
                    val deltaCpuMs = (tiempoCpuMs - ultimoTiempoCpuAppMs).coerceAtLeast(0L)

                    ultimaRedGlobalBajadaKbps = redondearA1Decimal((deltaTotalRx / 1024.0) / deltaSec)
                    ultimaRedGlobalSubidaKbps = redondearA1Decimal((deltaTotalTx / 1024.0) / deltaSec)
                    ultimaRedAppRxKbps = redondearA1Decimal((deltaAppRx / 1024.0) / deltaSec)
                    ultimaRedAppTxKbps = redondearA1Decimal((deltaAppTx / 1024.0) / deltaSec)

                    val cpuRawPct = ((deltaCpuMs.toDouble() / (deltaWallMs.toDouble() * nucleosCpu.toDouble())) * 100.0)
                        .toFloat()
                        .coerceIn(0.0f, 100.0f)
                    ultimoPorcentajeCpuApp = (cpuRawPct * 10f).roundToInt() / 10f

                    val potenciaActualMw = calcularPotenciaEstimadaAppMw(
                        porcentajeCpuApp = ultimoPorcentajeCpuApp,
                        redAppRxKbps = ultimaRedAppRxKbps,
                        redAppTxKbps = ultimaRedAppTxKbps
                    )
                    val voltajeVolts = voltajeMv.coerceAtLeast(3000) / 1000.0
                    val corrienteAppMa = potenciaActualMw / voltajeVolts
                    val deltaHoras = deltaWallMs / 3_600_000.0
                    consumoBateriaSesionMahAcumulado += corrienteAppMa * deltaHoras

                    ultimoTimestampMuestraMs = nowElapsedMs
                    ultimoTotalRxBytes = totalRxBytes
                    ultimoTotalTxBytes = totalTxBytes
                    ultimoAppRxBytes = appRxBytes
                    ultimoAppTxBytes = appTxBytes
                    ultimoTiempoCpuAppMs = tiempoCpuMs
                }
            }

            val potenciaEstimadaMw = calcularPotenciaEstimadaAppMw(
                porcentajeCpuApp = ultimoPorcentajeCpuApp,
                redAppRxKbps = ultimaRedAppRxKbps,
                redAppTxKbps = ultimaRedAppTxKbps
            )

            MuestraRendimientoVivo(
                redBajadaKbps = ultimaRedGlobalBajadaKbps,
                redSubidaKbps = ultimaRedGlobalSubidaKbps,
                redTotalRxMb = redondearA2Decimales(totalRxBytes / BYTES_POR_MB_DOUBLE),
                redTotalTxMb = redondearA2Decimales(totalTxBytes / BYTES_POR_MB_DOUBLE),
                redAppRxKbps = ultimaRedAppRxKbps,
                redAppTxKbps = ultimaRedAppTxKbps,
                redTotalAppKb = redondearA1Decimal((appRxBytes + appTxBytes) / 1024.0),
                porcentajeCpuApp = ultimoPorcentajeCpuApp,
                tiempoCpuMs = tiempoCpuMs,
                potenciaEstimadaMw = potenciaEstimadaMw,
                consumoBateriaSesionMah = redondearA3Decimales(consumoBateriaSesionMahAcumulado)
            )
        }
    }

    private inline fun leerTrafficBytesSeguro(lector: () -> Long): Long {
        val raw = runCatching(lector).getOrDefault(TrafficStats.UNSUPPORTED.toLong())
        return if (raw == TrafficStats.UNSUPPORTED.toLong() || raw < 0L) 0L else raw
    }

    private fun calcularPotenciaEstimadaAppMw(
        porcentajeCpuApp: Float,
        redAppRxKbps: Double,
        redAppTxKbps: Double
    ): Double {
        val baseProcesoMw = 12.0
        val cpuMw = porcentajeCpuApp.toDouble() * 28.0
        val redMw = ((redAppRxKbps + redAppTxKbps) * 0.15).coerceAtMost(450.0)
        return redondearA1Decimal(baseProcesoMw + cpuMw + redMw)
    }

    private fun obtenerInventarioHardware(context: Context): InventarioHardware {
        inventarioCache?.let { return it }
        return synchronized(this) {
            inventarioCache?.let { return it }
            val inventario = InventarioHardware(
                pantallas = leerPantallas(context),
                camaras = leerCamaras(context),
                sensores = leerSensores(context)
            )
            inventarioCache = inventario
            inventario
        }
    }

    private fun leerPantallas(context: Context): List<PantallaHardwareInfo> {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            ?: return emptyList()
        return runCatching {
            displayManager.displays.map { display ->
                val mode = runCatching { display.mode }.getOrNull()
                val resolucion = if (mode != null) {
                    "${mode.physicalWidth} × ${mode.physicalHeight}"
                } else {
                    val metrics = DisplayMetrics()
                    @Suppress("DEPRECATION")
                    runCatching { display.getRealMetrics(metrics) }
                    "${metrics.widthPixels} × ${metrics.heightPixels}"
                }
                val tasaRefrescoHz = mode?.refreshRate ?: display.refreshRate
                val tasasSoportadasHz = runCatching {
                    display.supportedModes
                        .map { it.refreshRate }
                        .distinct()
                        .sortedDescending()
                }.getOrDefault(listOf(tasaRefrescoHz)).ifEmpty { listOf(tasaRefrescoHz) }

                PantallaHardwareInfo(
                    id = display.displayId,
                    nombre = display.name?.takeIf { it.isNotBlank() } ?: "Pantalla ${display.displayId}",
                    resolucion = resolucion,
                    tasaRefrescoHz = tasaRefrescoHz,
                    tasasSoportadasHz = tasasSoportadasHz
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun leerCamaras(context: Context): List<CamaraHardwareInfo> {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            ?: return emptyList()
        return runCatching {
            cameraManager.cameraIdList.mapNotNull { id ->
                runCatching {
                    val chars = cameraManager.getCameraCharacteristics(id)
                    val ubicacion = when (chars.get(CameraCharacteristics.LENS_FACING)) {
                        CameraCharacteristics.LENS_FACING_BACK -> "Trasera"
                        CameraCharacteristics.LENS_FACING_FRONT -> "Frontal"
                        CameraCharacteristics.LENS_FACING_EXTERNAL -> "Externa"
                        else -> "Desconocida"
                    }
                    val hardwareLevel = chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                    val nivelHardware = when {
                        hardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "Legacy"
                        hardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "Limited"
                        hardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "Full"
                        hardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "Level 3"
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                            hardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "External"
                        else -> "Estándar"
                    }
                    val tieneFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                    CamaraHardwareInfo(
                        id = id,
                        ubicacion = ubicacion,
                        nivelHardware = nivelHardware,
                        tieneFlash = tieneFlash
                    )
                }.getOrNull()
            }
        }.getOrDefault(emptyList())
    }

    private fun leerSensores(context: Context): List<SensorHardwareInfo> {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            ?: return emptyList()
        return runCatching {
            sensorManager.getSensorList(Sensor.TYPE_ALL).map { sensor ->
                SensorHardwareInfo(
                    nombre = sensor.name?.takeIf { it.isNotBlank() } ?: "Sensor desconocido",
                    fabricante = sensor.vendor?.takeIf { it.isNotBlank() } ?: "Desconocido",
                    tipo = sensor.stringType?.takeIf { it.isNotBlank() } ?: "Tipo ${sensor.type}",
                    consumoMa = sensor.power,
                    rangoMaximo = sensor.maximumRange,
                    resolucion = sensor.resolution
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun evaluarNivelRiesgo(
        estadoGlobal: EstadoGlobalHardware,
        flujoEnergetico: FlujoEnergeticoDispositivo,
        consumoApp: ConsumoPropiaApp
    ): NivelRiesgo {
        return when {
            estadoGlobal.memoriaBajaSistema ||
                estadoGlobal.porcentajeRamUsado >= 90 ||
                estadoGlobal.estadoTermicoCodigo >= 3 ||
                flujoEnergetico.temperaturaCelsius >= 43.0f -> NivelRiesgo.ALTO
            estadoGlobal.porcentajeRamUsado >= 80 ||
                estadoGlobal.alertaThrottling ||
                flujoEnergetico.descargaAltaAlerta ||
                consumoApp.porcentajeHeapUsado >= 80 -> NivelRiesgo.MEDIO
            else -> NivelRiesgo.BAJO
        }
    }

    private fun normalizarCorrienteUa(raw: Int, fallbackUa: Int): Int {
        if (raw == Int.MIN_VALUE || raw == 0) return fallbackUa
        return if (abs(raw) in 1..10_000) raw * 1000 else raw
    }

    private fun obtenerCapacidadRemanenteMah(
        batteryManager: BatteryManager?,
        nivelBateria: Int
    ): Int {
        val raw = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            ?: Int.MIN_VALUE
        return when {
            raw == Int.MIN_VALUE || raw <= 0 -> nivelBateria * 40
            raw > 10_000 -> raw / 1000
            else -> raw
        }
    }

    private fun formatearModeloDispositivo(): String {
        val fabricante = Build.MANUFACTURER.orEmpty().trim()
        val modelo = Build.MODEL.orEmpty().trim()
        return when {
            modelo.isBlank() && fabricante.isBlank() -> "Android Device"
            fabricante.isBlank() -> modelo
            modelo.startsWith(fabricante, ignoreCase = true) -> modelo
            else -> "$fabricante $modelo"
        }
    }

    private fun formatearModeloSoC(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val fabricante = Build.SOC_MANUFACTURER.orEmpty().trim()
                .takeIf { it.isNotBlank() && !it.equals(Build.UNKNOWN, ignoreCase = true) }
            val modelo = Build.SOC_MODEL.orEmpty().trim()
                .takeIf { it.isNotBlank() && !it.equals(Build.UNKNOWN, ignoreCase = true) }
            when {
                fabricante != null && modelo != null -> return if (modelo.startsWith(fabricante, ignoreCase = true)) {
                    modelo
                } else {
                    "$fabricante $modelo"
                }
                modelo != null -> return modelo
                fabricante != null -> return fabricante
            }
        }
        return "${Build.HARDWARE} (${Build.BOARD})"
    }

    private fun redondearA1Decimal(valor: Double): Double = (valor * 10.0).roundToLong() / 10.0

    private fun redondearA2Decimales(valor: Double): Double = (valor * 100.0).roundToLong() / 100.0

    private fun redondearA3Decimales(valor: Double): Double = (valor * 1000.0).roundToLong() / 1000.0
}
