package cl.duoc.tiotec.viewmodel

import cl.duoc.tiotec.model.CategoriaSeguridad
import cl.duoc.tiotec.model.EstadoAsistencia
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Solicitudes de asistencia (Room) y diagnósticos enviados/consultados al microservicio. */
@OptIn(ExperimentalCoroutinesApi::class)
class AsistenciaYDiagnosticosRemotosTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun registrarSolicitud_valida_seGuardaEnRoom() = runTest(testDispatcher) {
        val dao = FakeTioTecDao()
        val vm = TioTecViewModel(repository = TioTecRepository(dao = dao))
        advanceUntilIdle()

        vm.onCategoriaAsistenciaChange(CategoriaSeguridad.AUTENTICACION)
        vm.onDescripcionAsistenciaChange("No sé cómo activar la verificación en dos pasos")
        vm.registrarSolicitudAsistencia()
        advanceUntilIdle()

        assertEquals(1, dao.solicitudes.size)
        assertEquals(CategoriaSeguridad.AUTENTICACION.name, dao.solicitudes.first().categoria)
        assertEquals(1, vm.uiState.value.solicitudesAsistencia.size)
        assertEquals("", vm.uiState.value.descripcionAsistenciaInput)
    }

    @Test
    fun registrarSolicitud_vacia_muestraErrorYNoGuarda() = runTest(testDispatcher) {
        val dao = FakeTioTecDao()
        val vm = TioTecViewModel(repository = TioTecRepository(dao = dao))
        advanceUntilIdle()

        vm.registrarSolicitudAsistencia()
        advanceUntilIdle()

        assertTrue(dao.solicitudes.isEmpty())
        assertNotNull(vm.uiState.value.errorDescripcionAsistencia)
    }

    @Test
    fun alternarSolicitud_cambiaEstadoEnRoom() = runTest(testDispatcher) {
        val dao = FakeTioTecDao()
        val vm = TioTecViewModel(repository = TioTecRepository(dao = dao))
        advanceUntilIdle()
        vm.onDescripcionAsistenciaChange("Necesito ayuda para respaldar mis fotos")
        vm.registrarSolicitudAsistencia()
        advanceUntilIdle()
        val id = dao.solicitudes.first().id

        vm.alternarEstadoSolicitudAsistencia(id)
        advanceUntilIdle()
        assertEquals(EstadoAsistencia.ATENDIDA.name, dao.solicitudes.first().estado)

        vm.alternarEstadoSolicitudAsistencia(id)
        advanceUntilIdle()
        assertEquals(EstadoAsistencia.PENDIENTE.name, dao.solicitudes.first().estado)
    }

    @Test
    fun subirDiagnostico_alServidor_muestraExitoYMarcaSincronizado() = runTest(testDispatcher) {
        val api = FakeTioTecApiService()
        val diagDao = FakeDiagnosticoHardwareDao()
        val vm = TioTecViewModel(repository = TioTecRepository(diagnosticoDao = diagDao, apiService = api))
        advanceUntilIdle()

        vm.guardarDiagnosticoEnRoom()
        advanceUntilIdle()
        val id = diagDao.diagnosticos.first().id

        vm.sincronizarDiagnosticoConBackend(id)
        advanceUntilIdle()

        assertEquals(1, api.llamadasEnviar)
        assertTrue(diagDao.diagnosticos.first().sincronizadoRemoto)
        assertTrue(vm.uiState.value.mensajeExito!!.startsWith("Sincronizado con Spring Boot"))
        assertFalse(vm.uiState.value.sincronizandoApi)
        assertEquals(1, vm.uiState.value.historialDiagnosticos.size)
    }

    @Test
    fun consultarDiagnosticosRemotos_ok() = runTest(testDispatcher) {
        val api = FakeTioTecApiService()
        val vm = TioTecViewModel(repository = TioTecRepository(apiService = api))

        vm.consultarDiagnosticosRemotos()
        advanceUntilIdle()

        assertEquals(1, api.llamadasObtener)
        assertEquals("Se consultaron 0 diagnósticos desde Spring Boot", vm.uiState.value.mensajeExito)
    }

    @Test
    fun consultarDiagnosticosRemotos_servidorApagado_muestraError() = runTest(testDispatcher) {
        val vm = TioTecViewModel(repository = TioTecRepository(apiService = FakeApiCaida()))

        vm.consultarDiagnosticosRemotos()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.errorFormulario!!.startsWith("Error al consultar Spring Boot"))
        assertFalse(vm.uiState.value.sincronizandoApi)
    }

    @Test
    fun guardarPerfil_validoActualizaUsuario() = runTest(testDispatcher) {
        val vm = TioTecViewModel()
        vm.onNombrePerfilChange("  Daken  ")
        vm.guardarPerfil()
        assertEquals("Daken", vm.uiState.value.usuario.nombre)
        assertEquals("Perfil actualizado correctamente", vm.uiState.value.mensajeExito)
    }
}
