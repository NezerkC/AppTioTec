package cl.duoc.tiotec.viewmodel

import cl.duoc.tiotec.data.remote.EndOfLifeVersionDto
import cl.duoc.tiotec.model.EstadoSoporte
import cl.duoc.tiotec.model.SoporteAndroid
import cl.duoc.tiotec.model.VersionAndroid
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** API externa endoflife.date: lógica de soporte y su uso desde el ViewModel. */
@OptIn(ExperimentalCoroutinesApi::class)
class SoporteAndroidApiExternaTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private val versiones = listOf(
        VersionAndroid("14", "14", conSoporte = true, sinSoporteDesde = null),
        VersionAndroid("12", "12", conSoporte = false, sinSoporteDesde = "2025-03-03")
    )

    @Test
    fun extraerVersion_reconoceDistintasFormas() {
        assertEquals("14", SoporteAndroid.extraerVersion("Android 14"))
        assertEquals("12.1", SoporteAndroid.extraerVersion("android 12.1"))
        assertEquals("15", SoporteAndroid.extraerVersion("Android15"))
        assertNull(SoporteAndroid.extraerVersion("Windows 11"))
        assertNull(SoporteAndroid.extraerVersion("12"))
    }

    @Test
    fun evaluar_versionConSoporte() {
        assertEquals(EstadoSoporte.ConSoporte, SoporteAndroid.evaluar("Android 14", versiones))
    }

    @Test
    fun evaluar_versionSinSoporte_incluyeFecha() {
        val estado = SoporteAndroid.evaluar("Android 12", versiones)
        assertEquals(EstadoSoporte.SinSoporte("2025-03-03"), estado)
        assertEquals("Sin soporte desde 2025-03-03", estado.texto)
    }

    @Test
    fun evaluar_versionMenorUsaLaMayor() {
        assertEquals(EstadoSoporte.SinSoporte("2025-03-03"), SoporteAndroid.evaluar("Android 12.1", versiones))
    }

    @Test
    fun evaluar_casosSinDatos() {
        assertEquals(EstadoSoporte.SinInformacion, SoporteAndroid.evaluar("Android 99", versiones))
        assertEquals(EstadoSoporte.NoEsAndroid, SoporteAndroid.evaluar("Windows 11", versiones))
        assertEquals("Sin soporte de seguridad", EstadoSoporte.SinSoporte(null).texto)
    }

    @Test
    fun desdeDto_invierteIsEol() {
        val v = SoporteAndroid.desdeDto(EndOfLifeVersionDto("13", null, null, isEol = true, eolFrom = "2026-03-02"))
        assertFalse(v.conSoporte)
        assertEquals("13", v.etiqueta)
        assertEquals("2026-03-02", v.sinSoporteDesde)
    }

    @Test
    fun viewModel_consultaLaApiYGuardaLasVersionesEnElEstado() = runTest(testDispatcher) {
        val api = FakeEndOfLifeApi()
        val vm = TioTecViewModel(repository = TioTecRepository(endOfLifeService = api))

        vm.consultarSoporteAndroid()
        advanceUntilIdle()

        assertEquals(1, api.llamadas)
        assertEquals(3, vm.uiState.value.versionesAndroid.size)
        assertFalse(vm.uiState.value.cargandoSoporteAndroid)
        assertNull(vm.uiState.value.errorSoporteAndroid)
    }

    @Test
    fun viewModel_sinInternet_muestraErrorSinTocarLosDispositivos() = runTest(testDispatcher) {
        val vm = TioTecViewModel(repository = TioTecRepository(endOfLifeService = FakeEndOfLifeApi(falla = true)))
        val dispositivosAntes = vm.uiState.value.dispositivos

        vm.consultarSoporteAndroid()
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.errorSoporteAndroid)
        assertTrue(vm.uiState.value.versionesAndroid.isEmpty())
        assertEquals(dispositivosAntes, vm.uiState.value.dispositivos)
    }
}
