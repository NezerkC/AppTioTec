package cl.duoc.tiotec.viewmodel

import cl.duoc.tiotec.model.AlmacenamientoInfo
import cl.duoc.tiotec.model.BateriaInfo
import cl.duoc.tiotec.model.DiagnosticoHardwareRegistro
import cl.duoc.tiotec.model.DiagnosticoSistema
import cl.duoc.tiotec.model.InventarioSensores
import cl.duoc.tiotec.model.NivelRiesgo
import cl.duoc.tiotec.model.RamInfo
import cl.duoc.tiotec.model.SensorHardwareInfo
import cl.duoc.tiotec.nativo.TelemetriaSistemaHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TelemetriaSistemaTest {

    @Test
    fun ramInfo_calculaUsoYAlertasCorrectamente() {
        val normal = RamInfo(totalMb = 4096L, disponibleMb = 2048L, memoriaBajaSistema = false)
        assertEquals(2048L, normal.usadaMb)
        assertEquals(50, normal.porcentajeUsado)
        assertFalse(normal.ramAlerta)

        val critico = RamInfo(totalMb = 4000L, disponibleMb = 400L, memoriaBajaSistema = false)
        assertEquals(3600L, critico.usadaMb)
        assertEquals(90, critico.porcentajeUsado)
        assertTrue(critico.ramAlerta)

        val memoriaBaja = RamInfo(totalMb = 4096L, disponibleMb = 2048L, memoriaBajaSistema = true)
        assertTrue(memoriaBaja.ramAlerta)
    }

    @Test
    fun bateriaInfo_calculaVoltajeYAlertaTemperatura() {
        val normal = BateriaInfo(
            nivelPorcentaje = 80,
            temperaturaCelsius = 31.5f,
            voltajeMv = 4120,
            estaCargando = false
        )
        assertFalse(normal.temperaturaAlerta)
        assertEquals(4.12, normal.voltajeVoltios, 0.01)

        val caliente = BateriaInfo(
            nivelPorcentaje = 95,
            temperaturaCelsius = 42.0f,
            voltajeMv = 4200,
            estaCargando = true
        )
        assertTrue(caliente.temperaturaAlerta)
        assertEquals(4.2, caliente.voltajeVoltios, 0.01)
    }

    @Test
    fun almacenamientoInfo_calculaEspacioUsadoYPorcentaje() {
        val storage = AlmacenamientoInfo(totalGb = 128.0, libreGb = 32.0)
        assertEquals(96.0, storage.usadoGb, 0.1)
        assertEquals(75, storage.porcentajeUsado)
        assertFalse(storage.almacenamientoAlerta)

        val lleno = AlmacenamientoInfo(totalGb = 100.0, libreGb = 10.0)
        assertEquals(90.0, lleno.usadoGb, 0.1)
        assertEquals(90, lleno.porcentajeUsado)
        assertTrue(lleno.almacenamientoAlerta)
    }

    @Test
    fun inventarioSensores_calculaConsumoTotalMa() {
        val sensores = listOf(
            SensorHardwareInfo("Acelerómetro", "Bosch", "Acelerómetro", 0.25f),
            SensorHardwareInfo("Giroscopio", "Bosch", "Giroscopio", 1.35f),
            SensorHardwareInfo("Luz", "AMS", "Sensor de Luz", 0.12f)
        )
        val inventario = InventarioSensores(sensores = sensores)

        assertEquals(3, inventario.cantidadSensores)
        assertEquals(1.72, inventario.consumoTotalSensoresMa, 0.01)
    }

    @Test
    fun telemetriaHelper_evaluaNivelRiesgoSegunMetricas() {
        val seguro = TelemetriaSistemaHelper.evaluarNivelRiesgo(
            bateria = BateriaInfo(nivelPorcentaje = 80, temperaturaCelsius = 30f),
            ram = RamInfo(totalMb = 4000L, disponibleMb = 2000L),
            almacenamiento = AlmacenamientoInfo(totalGb = 64.0, libreGb = 32.0)
        )
        assertEquals(NivelRiesgo.BAJO, seguro)

        val medio = TelemetriaSistemaHelper.evaluarNivelRiesgo(
            bateria = BateriaInfo(nivelPorcentaje = 15, temperaturaCelsius = 32f),
            ram = RamInfo(totalMb = 4000L, disponibleMb = 2000L),
            almacenamiento = AlmacenamientoInfo(totalGb = 64.0, libreGb = 32.0)
        )
        assertEquals(NivelRiesgo.MEDIO, medio)

        val critico = TelemetriaSistemaHelper.evaluarNivelRiesgo(
            bateria = BateriaInfo(nivelPorcentaje = 9, temperaturaCelsius = 44f),
            ram = RamInfo(totalMb = 4000L, disponibleMb = 200L, memoriaBajaSistema = true),
            almacenamiento = AlmacenamientoInfo(totalGb = 64.0, libreGb = 2.0)
        )
        assertEquals(NivelRiesgo.ALTO, critico)
    }

    @Test
    fun diagnosticoHardwareRegistro_fromDiagnosticoSistema_convierteCampos() {
        val sistema = DiagnosticoSistema(
            timestampMs = 1700000000000L,
            fechaFormateada = "13/11/2026 12:00:00",
            bateria = BateriaInfo(nivelPorcentaje = 85, temperaturaCelsius = 33f, voltajeMv = 4050),
            ram = RamInfo(totalMb = 6000L, disponibleMb = 3000L),
            almacenamiento = AlmacenamientoInfo(totalGb = 128.0, libreGb = 64.0),
            sensores = InventarioSensores(
                sensores = listOf(SensorHardwareInfo("Proximidad", "Sensortek", "Sensor", 0.5f))
            ),
            nivelRiesgo = NivelRiesgo.BAJO
        )

        val registro = DiagnosticoHardwareRegistro.fromDiagnosticoSistema(sistema)
        assertEquals("diag_1700000000000", registro.id)
        assertEquals(85, registro.nivelBateria)
        assertEquals(33f, registro.temperaturaBateria)
        assertEquals(6000L, registro.ramTotalMb)
        assertEquals(1, registro.totalSensores)
        assertEquals(0.5, registro.consumoSensoresMa, 0.01)
        assertFalse(registro.sincronizadoRemoto)
    }
}
