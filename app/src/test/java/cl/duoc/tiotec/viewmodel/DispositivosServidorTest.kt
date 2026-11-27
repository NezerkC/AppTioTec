package cl.duoc.tiotec.viewmodel

import cl.duoc.tiotec.data.local.DispositivoEntity
import cl.duoc.tiotec.data.remote.DispositivoDto
import cl.duoc.tiotec.model.TipoDispositivo
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * CRUD de dispositivos: Room (FakeTioTecDao) + microservicio Spring Boot (FakeTioTecApiService).
 * Cada test sigue el patrón Arrange (preparar) - Act (actuar) - Assert (verificar).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DispositivosServidorTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dao: FakeTioTecDao
    private lateinit var api: FakeTioTecApiService

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dao = FakeTioTecDao()
        api = FakeTioTecApiService()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun crearViewModel(apiService: cl.duoc.tiotec.data.remote.TioTecApiService = api) =
        TioTecViewModel(repository = TioTecRepository(dao = dao, apiService = apiService))

    private fun TioTecViewModel.llenarFormulario(nombre: String, so: String, tipo: TipoDispositivo = TipoDispositivo.SMARTPHONE) {
        onNombreDispositivoChange(nombre)
        onTipoDispositivoChange(tipo)
        onSistemaOperativoChange(so)
    }

    @Test
    fun conRoom_laListaEmpiezaVaciaSinDatosDeEjemplo() = runTest(testDispatcher) {
        val vm = crearViewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.dispositivos.isEmpty())
    }

    @Test
    fun registrar_guardaEnRoomYEnElServidor() = runTest(testDispatcher) {
        val vm = crearViewModel()
        advanceUntilIdle()

        vm.llenarFormulario("Pixel 8", "Android 14")
        vm.registrarDispositivo()
        advanceUntilIdle()

        assertEquals(1, dao.dispositivos.size)
        assertEquals(1, api.llamadasPost)
        assertEquals("Pixel 8", api.servidor.values.first().nombre)
        assertEquals(dao.dispositivos.first().id, api.servidor.keys.first()) // mismo id en ambos lados
        assertEquals(true, vm.uiState.value.servidorDispositivosOk)
        assertEquals(1, vm.uiState.value.dispositivos.size)
    }

    @Test
    fun editar_cargaElFormularioYActualizaConPut() = runTest(testDispatcher) {
        val vm = crearViewModel()
        advanceUntilIdle()
        vm.llenarFormulario("Pixel 8", "Android 14")
        vm.registrarDispositivo()
        advanceUntilIdle()
        val id = dao.dispositivos.first().id

        vm.iniciarEdicionDispositivo(id)
        assertEquals(id, vm.uiState.value.dispositivoEnEdicionId)
        assertEquals("Pixel 8", vm.uiState.value.nombreDispositivoInput)

        vm.onNombreDispositivoChange("Pixel 8 Pro")
        vm.registrarDispositivo()
        advanceUntilIdle()

        assertEquals(1, dao.dispositivos.size) // no se duplicó
        assertEquals("Pixel 8 Pro", dao.dispositivos.first().nombre)
        assertEquals(1, api.llamadasPut)
        assertEquals("Pixel 8 Pro", api.servidor[id]?.nombre)
        assertNull(vm.uiState.value.dispositivoEnEdicionId)
    }

    @Test
    fun editar_siElServidorNoLoTiene_loCreaConPost() = runTest(testDispatcher) {
        dao.insertarDispositivo(DispositivoEntity("local_1", "Tablet vieja", "TABLET", "Android 12"))
        val repo = TioTecRepository(dao = dao, apiService = api)
        val vm = TioTecViewModel(repository = repo)
        advanceUntilIdle()
        api.servidor.clear() // el servidor no conoce este dispositivo
        api.llamadasPost = 0

        vm.iniciarEdicionDispositivo("local_1")
        vm.onNombreDispositivoChange("Tablet del living")
        vm.registrarDispositivo()
        advanceUntilIdle()

        assertEquals(1, api.llamadasPut)
        assertEquals(1, api.llamadasPost)
        assertEquals("Tablet del living", api.servidor["local_1"]?.nombre)
    }

    @Test
    fun cancelarEdicion_limpiaElFormulario() = runTest(testDispatcher) {
        dao.insertarDispositivo(DispositivoEntity("d1", "Notebook", "NOTEBOOK", "Windows 11"))
        val vm = crearViewModel()
        advanceUntilIdle()

        vm.iniciarEdicionDispositivo("d1")
        vm.cancelarEdicionDispositivo()

        assertNull(vm.uiState.value.dispositivoEnEdicionId)
        assertEquals("", vm.uiState.value.nombreDispositivoInput)
    }

    @Test
    fun eliminar_borraDeRoomYDelServidor() = runTest(testDispatcher) {
        val vm = crearViewModel()
        advanceUntilIdle()
        vm.llenarFormulario("Pixel 8", "Android 14")
        vm.registrarDispositivo()
        advanceUntilIdle()
        val id = dao.dispositivos.first().id

        vm.eliminarDispositivo(id)
        advanceUntilIdle()

        assertTrue(dao.dispositivos.isEmpty())
        assertTrue(api.servidor.isEmpty())
        assertEquals(1, api.llamadasDelete)
        assertTrue(vm.uiState.value.dispositivos.isEmpty())
    }

    @Test
    fun alAbrir_traeLosDispositivosDelServidorYConservaLaFotoLocal() = runTest(testDispatcher) {
        dao.insertarDispositivo(DispositivoEntity("d1", "Viejo", "SMARTPHONE", "Android 13", fotoUri = "/fotos/d1.jpg"))
        api.servidor["d1"] = DispositivoDto("d1", "Nombre del servidor", "SMARTPHONE", "Android 14")
        api.servidor["d2"] = DispositivoDto("d2", "Solo en servidor", "NOTEBOOK", "Windows 11")

        val vm = crearViewModel()
        advanceUntilIdle()

        assertEquals(2, dao.dispositivos.size)
        val d1 = dao.dispositivos.first { it.id == "d1" }
        assertEquals("Nombre del servidor", d1.nombre)
        assertEquals("/fotos/d1.jpg", d1.fotoUri)
        assertEquals(2, vm.uiState.value.dispositivos.size)
        assertEquals(true, vm.uiState.value.servidorDispositivosOk)
    }

    @Test
    fun servidorApagado_guardaEnRoomYAvisaSinCaerse() = runTest(testDispatcher) {
        val vm = crearViewModel(apiService = FakeApiCaida())
        advanceUntilIdle()
        assertEquals(false, vm.uiState.value.servidorDispositivosOk) // falló la sincronización inicial

        vm.llenarFormulario("Pixel 8", "Android 14")
        vm.registrarDispositivo()
        advanceUntilIdle()

        assertEquals(1, dao.dispositivos.size) // igual quedó en el teléfono
        assertEquals(false, vm.uiState.value.servidorDispositivosOk)
        assertTrue(vm.uiState.value.estadoServidorDispositivos!!.contains("solo en el teléfono"))
    }

    @Test
    fun sincronizarManual_muestraMensaje() = runTest(testDispatcher) {
        val vm = crearViewModel()
        advanceUntilIdle()
        vm.limpiarMensajes()

        vm.sincronizarDispositivosConServidor()
        advanceUntilIdle()

        assertEquals("Dispositivos sincronizados con el servidor", vm.uiState.value.mensajeExito)
        assertFalse(vm.uiState.value.sincronizandoDispositivos)
    }

    @Test
    fun registrar_conNombreCorto_noGuardaNada() = runTest(testDispatcher) {
        val vm = crearViewModel()
        advanceUntilIdle()

        vm.llenarFormulario("PC", "Windows 11", TipoDispositivo.PC_ESCRITORIO)
        vm.registrarDispositivo()
        advanceUntilIdle()

        assertTrue(dao.dispositivos.isEmpty())
        assertEquals(0, api.llamadasPost)
        assertTrue(vm.uiState.value.errorNombreDispositivo != null)
    }
}
