package cl.duoc.tiotec.viewmodel

import cl.duoc.tiotec.data.local.AccionChecklistEntity
import cl.duoc.tiotec.data.local.DiagnosticoHardwareDao
import cl.duoc.tiotec.data.local.DiagnosticoHardwareEntity
import cl.duoc.tiotec.data.local.DispositivoEntity
import cl.duoc.tiotec.data.local.EvaluacionEntity
import cl.duoc.tiotec.data.local.SolicitudAsistenciaEntity
import cl.duoc.tiotec.data.local.TioTecDao
import cl.duoc.tiotec.data.remote.DiagnosticoHardwareRequestDto
import cl.duoc.tiotec.data.remote.DiagnosticoHardwareResponseDto
import cl.duoc.tiotec.data.remote.DispositivoDto
import cl.duoc.tiotec.data.remote.TioTecApiService
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import cl.duoc.tiotec.model.DiagnosticoHardwareRegistro
import cl.duoc.tiotec.model.NivelRiesgo
import cl.duoc.tiotec.model.TipoDispositivo
import cl.duoc.tiotec.repository.TioTecRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TioTecRepositoryCrudYApiTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun registrarYEliminarDispositivo_persisteEnTioTecDaoFake() = runTest(testDispatcher) {
        val fakeDao = FakeTioTecDao()
        val repo = TioTecRepository(dao = fakeDao)
        val vm = TioTecViewModel(repository = repo)
        advanceUntilIdle()

        vm.onNombreDispositivoChange("Pixel 8")
        vm.onTipoDispositivoChange(TipoDispositivo.SMARTPHONE)
        vm.onSistemaOperativoChange("Android 14")
        vm.agregarDispositivo()
        advanceUntilIdle()

        assertEquals(1, fakeDao.dispositivos.size)
        val dispRegistrado = fakeDao.dispositivos.first()
        assertEquals("Pixel 8", dispRegistrado.nombre)
        assertEquals("Android 14", dispRegistrado.sistemaOperativo)

        vm.eliminarDispositivo(dispRegistrado.id)
        advanceUntilIdle()

        assertTrue(fakeDao.dispositivos.isEmpty())
        assertTrue(vm.uiState.value.dispositivos.none { it.id == dispRegistrado.id })
    }

    @Test
    fun finalizarEvaluacionYAlternarAccion_persisteEnTioTecDaoFake() = runTest(testDispatcher) {
        val fakeDao = FakeTioTecDao()
        val repo = TioTecRepository(dao = fakeDao)
        val vm = TioTecViewModel(repository = repo)
        advanceUntilIdle()

        // Responder con falla en una pregunta para generar acción preventiva
        vm.seleccionarRespuesta(1, 0) // Respaldos fallido (puntaje 0)
        vm.seleccionarRespuesta(2, 2)
        vm.seleccionarRespuesta(3, 2)
        vm.seleccionarRespuesta(4, 2)

        val resultado = vm.finalizarEvaluacion()
        advanceUntilIdle()

        assertTrue(resultado)
        assertEquals(1, fakeDao.evaluaciones.size)
        val generadas = fakeDao.acciones.filter { it.id.startsWith("chk_1_") }
        assertEquals("Debió guardarse la acción generada por la pregunta 1", 1, generadas.size)

        val accion = generadas.first()
        assertEquals(false, accion.completada)

        vm.alternarEstadoAccion(accion.id)
        advanceUntilIdle()

        val accionActualizada = fakeDao.acciones.first { it.id == accion.id }
        assertEquals(true, accionActualizada.completada)
    }

    @Test
    fun eliminarDiagnosticoEnRoom_eliminaPorIdEnDiagnosticoHardwareDaoFake() = runTest(testDispatcher) {
        val fakeDiagDao = FakeDiagnosticoHardwareDao()
        val repo = TioTecRepository(diagnosticoDao = fakeDiagDao)
        val vm = TioTecViewModel(repository = repo)
        advanceUntilIdle()

        val id = "diag_test_123"
        repo.guardarDiagnosticoHardware(
            DiagnosticoHardwareRegistro(
                id = id,
                fechaRegistro = "20/11/2026 10:00",
                timestampMs = 123456789L,
                nivelBateria = 80,
                estaCargando = false,
                temperaturaBateria = 30.0f,
                voltajeBateria = 4000,
                saludBateria = "Buena",
                fuenteEnergia = "Batería",
                almacenamientoTotalGb = 128.0,
                almacenamientoLibreGb = 64.0,
                porcentajeAlmacenamientoUsado = 50,
                ramTotalMb = 8192L,
                ramDisponibleMb = 4096L,
                totalSensores = 10,
                consumoSensoresMa = 5.0,
                nivelRiesgo = NivelRiesgo.BAJO,
                recomendacion = "Ok",
                sincronizadoRemoto = false
            )
        )
        assertEquals(1, fakeDiagDao.diagnosticos.size)

        vm.eliminarDiagnosticoEnRoom(id)
        advanceUntilIdle()

        assertTrue(fakeDiagDao.diagnosticos.isEmpty())
        assertEquals("Diagnóstico eliminado de Room", vm.uiState.value.mensajeExito)
    }

    @Test
    fun enviarDiagnosticoRemoto_usaApiServiceInyectadoConUrlDefecto() = runTest(testDispatcher) {
        val fakeApi = FakeTioTecApiService()
        val repo = TioTecRepository(apiService = fakeApi)

        val registro = DiagnosticoHardwareRegistro(
            id = "diag_remote_1",
            fechaRegistro = "20/11/2026 12:00",
            timestampMs = 123456789L,
            nivelBateria = 90,
            estaCargando = true,
            temperaturaBateria = 31.0f,
            voltajeBateria = 4200,
            saludBateria = "Buena",
            fuenteEnergia = "AC",
            almacenamientoTotalGb = 256.0,
            almacenamientoLibreGb = 120.0,
            porcentajeAlmacenamientoUsado = 53,
            ramTotalMb = 8192L,
            ramDisponibleMb = 5000L,
            totalSensores = 12,
            consumoSensoresMa = 6.2,
            nivelRiesgo = NivelRiesgo.BAJO,
            recomendacion = "Ok",
            sincronizadoRemoto = false
        )

        val resultado = repo.enviarDiagnosticoRemoto(registro)
        assertTrue(resultado.isSuccess)
        assertEquals(1, fakeApi.llamadasEnviar)
        assertEquals("diag_remote_1", resultado.getOrNull()?.id)
    }

    @Test
    fun viewModel_conUrlInvalida_capturaErrorYSeteaErrorFormularioSinCrash() = runTest(testDispatcher) {
        val fakeApi = FakeTioTecApiService()
        val repo = TioTecRepository(apiService = fakeApi)
        val vm = TioTecViewModel(repository = repo)
        advanceUntilIdle()

        // Seteamos una URL malformada
        vm.onUrlServidorSpringChange("url_invalida")
        vm.sincronizarDiagnosticoConBackend()
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.errorFormulario)
        assertTrue(
            "Debió fallar con error de conexión o parseo sin lanzar excepción no capturada",
            vm.uiState.value.errorFormulario!!.contains("Fallo al conectar con Spring Boot")
        )
        // El apiService por defecto no debió usarse porque se intentó resolver con la URL dinámica
        assertEquals(0, fakeApi.llamadasEnviar)
    }
}
