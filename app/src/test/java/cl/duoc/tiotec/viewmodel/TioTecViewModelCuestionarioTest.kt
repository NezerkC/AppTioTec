package cl.duoc.tiotec.viewmodel

import cl.duoc.tiotec.model.NivelRiesgo
import cl.duoc.tiotec.model.PrioridadAccion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TioTecViewModelCuestionarioTest {

    /** Responde todas las preguntas con el mismo puntaje (0, 1 o 2). */
    private fun responderTodas(vm: TioTecViewModel, puntaje: Int) {
        vm.uiState.value.preguntas.forEach { vm.seleccionarRespuesta(it.id, puntaje) }
    }

    @Test
    fun finalizarIncompleto_marcaPreguntasPendientes() {
        val vm = TioTecViewModel()
        vm.seleccionarRespuesta(1, 2)

        val finalizo = vm.finalizarEvaluacion()

        assertFalse(finalizo)
        assertEquals(setOf(2, 3, 4), vm.uiState.value.preguntasSinResponder)
        assertNotNull(vm.uiState.value.errorFormulario)
    }

    @Test
    fun responderPreguntaPendiente_laQuitaDeLasPendientes() {
        val vm = TioTecViewModel()
        vm.finalizarEvaluacion()
        vm.seleccionarRespuesta(2, 1)

        assertFalse(2 in vm.uiState.value.preguntasSinResponder)
    }

    @Test
    fun todoEnMaximo_esRiesgoBajo_sinAcciones() {
        val vm = TioTecViewModel()
        responderTodas(vm, 2)

        assertTrue(vm.finalizarEvaluacion())
        val resultado = vm.uiState.value.resultadoReciente
        assertEquals(NivelRiesgo.BAJO, resultado?.nivelRiesgo)
        assertTrue(vm.uiState.value.accionesGeneradas.isEmpty())
    }

    @Test
    fun todoEnCero_esRiesgoAlto_conAccionesUrgentes() {
        val vm = TioTecViewModel()
        responderTodas(vm, 0)
        vm.finalizarEvaluacion()

        val state = vm.uiState.value
        assertEquals(NivelRiesgo.ALTO, state.resultadoReciente?.nivelRiesgo)
        assertEquals(4, state.accionesGeneradas.size)
        assertTrue(state.accionesGeneradas.all { it.prioridad == PrioridadAccion.ALTA })
    }

    @Test
    fun finalizar_agregaAlHistorial() {
        val vm = TioTecViewModel()
        val antes = vm.uiState.value.historial.size
        responderTodas(vm, 1)
        vm.finalizarEvaluacion()

        assertEquals(antes + 1, vm.uiState.value.historial.size)
        assertEquals(NivelRiesgo.MEDIO, vm.uiState.value.historial.first().nivelRiesgo)
    }

    @Test
    fun cerrarResultado_limpiaElResultado() {
        val vm = TioTecViewModel()
        responderTodas(vm, 2)
        vm.finalizarEvaluacion()
        vm.cerrarResultado()

        assertNull(vm.uiState.value.resultadoReciente)
    }
}
