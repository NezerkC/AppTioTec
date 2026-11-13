package cl.duoc.tiotec.viewmodel

import cl.duoc.tiotec.model.CamaraHardwareInfo
import cl.duoc.tiotec.model.ConsumoPropiaApp
import cl.duoc.tiotec.model.EstadoGlobalHardware
import cl.duoc.tiotec.model.FlujoEnergeticoDispositivo
import cl.duoc.tiotec.model.InventarioHardware
import cl.duoc.tiotec.model.NivelRiesgo
import cl.duoc.tiotec.model.PantallaHardwareInfo
import cl.duoc.tiotec.model.SensorHardwareInfo
import cl.duoc.tiotec.model.TelemetriaSistema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TelemetriaSistemaTest {

    @Test
    fun estadoGlobalHardware_calculaPorcentajeRamYAlertaCorrectamente() {
        val normal = EstadoGlobalHardware(
            ramTotalMb = 4096L,
            ramDisponibleMb = 2048L,
            memoriaBajaSistema = false
        )

        assertEquals(2048L, normal.ramUsadaMb)
        assertEquals(50, normal.porcentajeRamUsado)
        assertFalse(normal.ramAlerta)

        val ramAltaPorPorcentaje = EstadoGlobalHardware(
            ramTotalMb = 4000L,
            ramDisponibleMb = 400L,
            memoriaBajaSistema = false
        )

        assertEquals(3600L, ramAltaPorPorcentaje.ramUsadaMb)
        assertEquals(90, ramAltaPorPorcentaje.porcentajeRamUsado)
        assertTrue(ramAltaPorPorcentaje.ramAlerta)

        val ramBajaPorSistema = EstadoGlobalHardware(
            ramTotalMb = 4096L,
            ramDisponibleMb = 2048L,
            memoriaBajaSistema = true
        )

        assertTrue(ramBajaPorSistema.ramAlerta)
    }

    @Test
    fun flujoEnergetico_calculaPotenciaWattsYDireccionFlujo() {
        val descargaNormal = FlujoEnergeticoDispositivo(
            corrienteInstantaneaMa = -500,
            voltajeMv = 4000,
            estaCargando = false
        )

        assertEquals(2.0, descargaNormal.potenciaWatts, 0.01)
        assertEquals("Salida (Descarga)", descargaNormal.direccionFlujo)
        assertFalse(descargaNormal.descargaAltaAlerta)

        val descargaAlta = FlujoEnergeticoDispositivo(
            corrienteInstantaneaMa = -1000,
            voltajeMv = 3850,
            estaCargando = false
        )

        assertEquals(3.85, descargaAlta.potenciaWatts, 0.01)
        assertEquals("Salida (Descarga)", descargaAlta.direccionFlujo)
        assertTrue(descargaAlta.descargaAltaAlerta)

        val cargando = FlujoEnergeticoDispositivo(
            corrienteInstantaneaMa = 1500,
            voltajeMv = 4200,
            estaCargando = true
        )

        assertEquals(6.3, cargando.potenciaWatts, 0.01)
        assertEquals("Entrada (Carga)", cargando.direccionFlujo)
        assertFalse(cargando.descargaAltaAlerta)
    }

    @Test
    fun consumoPropiaApp_calculaPorcentajeHeapYSandboxNota() {
        val consumo = ConsumoPropiaApp(
            pidProceso = 2450,
            uidAplicacion = 10200,
            heapUsadoMb = 64.0,
            heapAsignadoMb = 128.0,
            heapMaximoMb = 256.0,
            memoriaPssTotalMb = 96.5,
            tiempoCpuMs = 1800L,
            hilosActivos = 18
        )

        assertEquals(25, consumo.porcentajeHeapUsado)

        val telemetria = TelemetriaSistema(consumoApp = consumo)
        assertTrue(
            telemetria.notaSandboxAndroid.contains(
                "Desde Android 8.0, el aislamiento del sistema (sandbox) no permite leer las métricas de otras aplicaciones."
            )
        )
    }

    @Test
    fun viewModel_estadoInicialIncluyeTelemetriaYChecklistConsideraAlertaRam() {
        val vm = TioTecViewModel()
        val estadoInicial = vm.uiState.value

        assertEquals(50, estadoInicial.telemetriaSistema.estadoGlobal.porcentajeRamUsado)
        assertFalse(estadoInicial.telemetriaSistema.estadoGlobal.ramAlerta)
        assertEquals(NivelRiesgo.BAJO, estadoInicial.telemetriaSistema.nivelRiesgo)
        assertTrue(
            estadoInicial.telemetriaSistema.notaSandboxAndroid.contains(
                "Desde Android 8.0, el aislamiento del sistema (sandbox) no permite leer las métricas de otras aplicaciones."
            )
        )

        val estadoConAlertaRam = estadoInicial.copy(
            telemetriaSistema = TelemetriaSistema(
                estadoGlobal = EstadoGlobalHardware(
                    ramTotalMb = 4096L,
                    ramDisponibleMb = 400L,
                    memoriaBajaSistema = true
                ),
                nivelRiesgo = NivelRiesgo.ALTO
            )
        )
        assertTrue(estadoConAlertaRam.telemetriaSistema.estadoGlobal.ramAlerta)
        assertEquals(NivelRiesgo.ALTO, estadoConAlertaRam.telemetriaSistema.nivelRiesgo)

        val totalAntes = estadoInicial.checklist.size
        vm.generarAccionesCuidadoEnChecklist()

        val checklistDespues = vm.uiState.value.checklist
        val nuevasAcciones = checklistDespues.drop(totalAntes)
        assertTrue("El checklist debió incrementarse con las tareas de cuidado", checklistDespues.size > totalAntes)
        assertFalse(
            "Sin alerta de RAM activa en el estado inicial, no debe agregarse tarea de optimización de RAM",
            nuevasAcciones.any { it.titulo.contains("Optimización de RAM") }
        )
        assertTrue(vm.uiState.value.mensajeExito?.contains("Checklist") == true)
    }

    @Test
    fun flujoEnergetico_exponeMicroamperiosYPotencia() {
        val defecto = FlujoEnergeticoDispositivo()
        assertEquals(-250_000, defecto.corrienteInstantaneaUa)
        assertEquals(-220_000, defecto.corrientePromedioUa)
        assertEquals(1.0, defecto.potenciaWatts, 0.01)

        val medido = FlujoEnergeticoDispositivo(
            corrienteInstantaneaMa = -650,
            corrientePromedioMa = -580,
            voltajeMv = 4000,
            estaCargando = false,
            corrienteInstantaneaUa = -650_000,
            corrientePromedioUa = -580_000
        )
        assertEquals(-650_000, medido.corrienteInstantaneaUa)
        assertEquals(-580_000, medido.corrientePromedioUa)
        assertEquals(2.6, medido.potenciaWatts, 0.01)
    }

    @Test
    fun estadoGlobal_formateaRedYDetectaAlertaThrottling() {
        val enKbps = EstadoGlobalHardware(
            redBajadaKbps = 512.4,
            redSubidaKbps = 128.0,
            estadoTermicoCodigo = 0,
            margenTermico = 0.25f,
            alertaThrottling = false
        )
        assertEquals("512.4 KB/s", enKbps.redBajadaFormateada)
        assertEquals("128.0 KB/s", enKbps.redSubidaFormateada)
        assertFalse(enKbps.alertaThrottling)

        val enMbpsConThrottling = EstadoGlobalHardware(
            redBajadaKbps = 2048.0,
            redSubidaKbps = 1536.0,
            estadoTermicoCodigo = 3,
            estadoTermicoEtiqueta = "Severo · Thermal throttling activo",
            margenTermico = 0.92f,
            alertaThrottling = true
        )
        assertEquals("2.00 MB/s", enMbpsConThrottling.redBajadaFormateada)
        assertEquals("1.50 MB/s", enMbpsConThrottling.redSubidaFormateada)
        assertTrue(enMbpsConThrottling.alertaThrottling)
    }

    @Test
    fun inventarioHardware_sumaConsumoTeoricoSensores() {
        val inventario = InventarioHardware(
            pantallas = listOf(
                PantallaHardwareInfo(
                    id = 0,
                    nombre = "Pantalla integrada",
                    resolucion = "1080x2400",
                    tasaRefrescoHz = 120f,
                    tasasSoportadasHz = listOf(120f, 60f)
                )
            ),
            camaras = listOf(
                CamaraHardwareInfo(
                    id = "0",
                    ubicacion = "Trasera",
                    nivelHardware = "Full",
                    tieneFlash = true
                )
            ),
            sensores = listOf(
                SensorHardwareInfo(
                    nombre = "Acelerómetro",
                    fabricante = "Bosch",
                    tipo = "android.sensor.accelerometer",
                    consumoMa = 0.25f,
                    rangoMaximo = 78.4f,
                    resolucion = 0.01f
                ),
                SensorHardwareInfo(
                    nombre = "Giroscopio",
                    fabricante = "Bosch",
                    tipo = "android.sensor.gyroscope",
                    consumoMa = 1.35f,
                    rangoMaximo = 34.9f,
                    resolucion = 0.001f
                ),
                SensorHardwareInfo(
                    nombre = "Magnetómetro",
                    fabricante = "AKM",
                    tipo = "android.sensor.magnetic_field",
                    consumoMa = 0.123f,
                    rangoMaximo = 4900f,
                    resolucion = 0.15f
                )
            )
        )

        assertEquals(1, inventario.pantallas.size)
        assertEquals(1, inventario.camaras.size)
        assertEquals(3, inventario.sensores.size)
        assertEquals(1.72, inventario.consumoTotalSensoresMa, 0.01)
    }

    @Test
    fun viewModel_alternarMonitoreoEnVivo_cambiaEstado() {
        val vm = TioTecViewModel()
        val estadoInicial = vm.uiState.value

        assertTrue(estadoInicial.monitoreoEnVivoActivo)
        assertTrue(estadoInicial.historialRamPorcentaje.isEmpty())
        assertTrue(estadoInicial.historialAlmacenamientoPorcentaje.isEmpty())
        assertTrue(estadoInicial.historialRedBajadaKbps.isEmpty())
        assertTrue(estadoInicial.historialRedSubidaKbps.isEmpty())
        assertTrue(estadoInicial.historialCpuAppPorcentaje.isEmpty())
        assertTrue(estadoInicial.historialPotenciaWatts.isEmpty())

        vm.alternarMonitoreoEnVivo()
        assertFalse(vm.uiState.value.monitoreoEnVivoActivo)

        vm.alternarMonitoreoEnVivo()
        assertTrue(vm.uiState.value.monitoreoEnVivoActivo)
    }
}
