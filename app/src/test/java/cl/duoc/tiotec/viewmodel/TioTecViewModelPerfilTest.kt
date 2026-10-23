package cl.duoc.tiotec.viewmodel

import cl.duoc.tiotec.model.NivelExperiencia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TioTecViewModelPerfilTest {

    @Test
    fun nombreCorto_muestraErrorEnTiempoReal() {
        val vm = TioTecViewModel()
        vm.onNombrePerfilChange("Al")
        assertNotNull(vm.uiState.value.errorNombrePerfil)
    }

    @Test
    fun alCorregirNombre_elErrorDesaparece() {
        val vm = TioTecViewModel()
        vm.onNombrePerfilChange("Al")
        vm.onNombrePerfilChange("Ana")
        assertNull(vm.uiState.value.errorNombrePerfil)
    }

    @Test
    fun guardarPerfilValido_actualizaUsuario() {
        val vm = TioTecViewModel()
        vm.onNombrePerfilChange("Daniela")
        vm.onNivelExperienciaChange(NivelExperiencia.AVANZADO)
        vm.guardarPerfil()

        val usuario = vm.uiState.value.usuario
        assertEquals("Daniela", usuario.nombre)
        assertEquals(NivelExperiencia.AVANZADO, usuario.nivelExperiencia)
    }

    @Test
    fun guardarPerfilInvalido_noCambiaUsuario() {
        val vm = TioTecViewModel()
        val nombreAntes = vm.uiState.value.usuario.nombre
        vm.guardarPerfil() // campo vacío

        assertNotNull(vm.uiState.value.errorNombrePerfil)
        assertEquals(nombreAntes, vm.uiState.value.usuario.nombre)
    }

    @Test
    fun registrarDispositivoVacio_muestraAmbosErrores() {
        val vm = TioTecViewModel()
        val cantidadAntes = vm.uiState.value.dispositivos.size
        vm.registrarDispositivo()

        val state = vm.uiState.value
        assertNotNull(state.errorNombreDispositivo)
        assertNotNull(state.errorSistemaOperativo)
        assertEquals(cantidadAntes, state.dispositivos.size)
    }

    @Test
    fun registrarDispositivoValido_loAgregaYLimpiaCampos() {
        val vm = TioTecViewModel()
        val cantidadAntes = vm.uiState.value.dispositivos.size
        vm.onNombreDispositivoChange("Notebook")
        vm.onSistemaOperativoChange("Windows 11")
        vm.registrarDispositivo()

        val state = vm.uiState.value
        assertEquals(cantidadAntes + 1, state.dispositivos.size)
        assertEquals("", state.nombreDispositivoInput)
        assertEquals("", state.sistemaOperativoInput)
    }
}
