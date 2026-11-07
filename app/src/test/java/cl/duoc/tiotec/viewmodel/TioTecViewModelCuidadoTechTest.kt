package cl.duoc.tiotec.viewmodel

import cl.duoc.tiotec.model.AppUsoInfo
import cl.duoc.tiotec.model.DiagnosticoHardware
import cl.duoc.tiotec.model.MetricasBienestarDigital
import cl.duoc.tiotec.model.NivelRiesgo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TioTecViewModelCuidadoTechTest {

    @Test
    fun diagnosticoHardware_temperaturaNormal_noAlerta() {
        val hardware = DiagnosticoHardware(
            nivelBateria = 85,
            temperaturaBateria = 32.5f,
            almacenamientoTotalGb = 128.0,
            almacenamientoLibreGb = 64.0,
            porcentajeAlmacenamientoUsado = 50
        )

        assertFalse(hardware.temperaturaAlerta)
        assertFalse(hardware.almacenamientoAlerta)
        assertEquals(64.0, hardware.almacenamientoUsadoGb, 0.1)
    }

    @Test
    fun diagnosticoHardware_temperaturaElevada_activaAlerta() {
        val hardwareCritico = DiagnosticoHardware(
            nivelBateria = 90,
            temperaturaBateria = 43.5f,
            porcentajeAlmacenamientoUsado = 94,
            nivelRiesgo = NivelRiesgo.ALTO
        )

        assertTrue(hardwareCritico.temperaturaAlerta)
        assertTrue(hardwareCritico.almacenamientoAlerta)
        assertEquals(NivelRiesgo.ALTO, hardwareCritico.nivelRiesgo)
    }

    @Test
    fun bienestarDigital_calculaHorasYMinutosCorrectamente() {
        val metricas = MetricasBienestarDigital(
            permisoConcedido = true,
            tiempoTotalPantallaMinutos = 185,
            appsMasUsadas = listOf(
                AppUsoInfo("Navegador Web", "com.android.chrome", 95),
                AppUsoInfo("YouTube", "com.google.android.youtube", 90)
            )
        )

        assertEquals(3L, metricas.horasTotales)
        assertEquals(5L, metricas.minutosRestantes)
        assertEquals("3h 5m hoy", metricas.tiempoTotalFormateado)
        assertFalse(metricas.alertaUsoExcesivo)
    }

    @Test
    fun bienestarDigital_usoMayorA5Horas_activaAlertaExcesiva() {
        val metricas = MetricasBienestarDigital(
            permisoConcedido = true,
            tiempoTotalPantallaMinutos = 320,
            nivelRiesgo = NivelRiesgo.ALTO
        )

        assertTrue(metricas.alertaUsoExcesivo)
        assertEquals(NivelRiesgo.ALTO, metricas.nivelRiesgo)
    }

    @Test
    fun generarAccionesCuidado_agregaTareasAlChecklist() {
        val vm = TioTecViewModel()
        val totalAntes = vm.uiState.value.checklist.size

        vm.generarAccionesCuidadoEnChecklist()

        val totalDespues = vm.uiState.value.checklist.size
        assertTrue("El checklist debió incrementarse con las tareas de cuidado", totalDespues > totalAntes)
        assertTrue(vm.uiState.value.mensajeExito?.contains("Checklist") == true)
    }
}
