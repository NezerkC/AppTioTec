package cl.duoc.tiotec.viewmodel

import cl.duoc.tiotec.repository.TioTecRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Corrección del error de los datos de ejemplo que desaparecían y de las acciones duplicadas. */
@OptIn(ExperimentalCoroutinesApi::class)
class RoomSinDatosFantasmaTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun TioTecViewModel.responderTodoMal() {
        uiState.value.preguntas.forEach { seleccionarRespuesta(it.id, 0) }
    }

    @Test
    fun primeraApertura_guardaElChecklistSugeridoEnRoom() = runTest(testDispatcher) {
        val dao = FakeTioTecDao()
        val vm = TioTecViewModel(repository = TioTecRepository(dao = dao))
        advanceUntilIdle()

        assertEquals(3, dao.acciones.size)
        assertEquals(3, vm.uiState.value.checklist.size)
    }

    @Test
    fun conRoom_historialYSolicitudesEmpiezanVacios() = runTest(testDispatcher) {
        val vm = TioTecViewModel(repository = TioTecRepository(dao = FakeTioTecDao()))
        advanceUntilIdle()

        assertTrue(vm.uiState.value.historial.isEmpty())
        assertNull(vm.uiState.value.ultimaEvaluacion)
        assertTrue(vm.uiState.value.solicitudesAsistencia.isEmpty())
    }

    @Test
    fun dosEvaluacionesIguales_noDuplicanAccionesPendientes() = runTest(testDispatcher) {
        val dao = FakeTioTecDao()
        val vm = TioTecViewModel(repository = TioTecRepository(dao = dao))
        advanceUntilIdle()

        vm.responderTodoMal()
        vm.finalizarEvaluacion()
        advanceUntilIdle()
        val despuesDeLaPrimera = dao.acciones.size

        vm.responderTodoMal()
        vm.finalizarEvaluacion()
        advanceUntilIdle()

        assertEquals(despuesDeLaPrimera, dao.acciones.size)
        assertEquals(2, dao.evaluaciones.size)
        val titulos = vm.uiState.value.checklist.filter { !it.completada }.map { it.titulo }
        assertEquals(titulos.size, titulos.toSet().size)
    }
}
