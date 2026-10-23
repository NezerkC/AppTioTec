package cl.duoc.tiotec.validation

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorFormulariosTest {

    // --- Nombre de perfil ---

    @Test
    fun nombrePerfil_conTresLetras_esValido() {
        assertNull(ValidadorFormularios.validarNombrePerfil("Ana"))
    }

    @Test
    fun nombrePerfil_cortoConEspacios_esInvalido() {
        assertNotNull(ValidadorFormularios.validarNombrePerfil("  Al  "))
    }

    @Test
    fun nombrePerfil_vacio_esInvalido() {
        assertNotNull(ValidadorFormularios.validarNombrePerfil(""))
    }

    // --- Nombre de dispositivo ---

    @Test
    fun nombreDispositivo_corto_esInvalido() {
        assertNotNull(ValidadorFormularios.validarNombreDispositivo("PC"))
    }

    @Test
    fun nombreDispositivo_valido() {
        assertNull(ValidadorFormularios.validarNombreDispositivo("Notebook"))
    }

    // --- Sistema operativo ---

    @Test
    fun sistemaOperativo_vacio_esInvalido() {
        assertNotNull(ValidadorFormularios.validarSistemaOperativo(""))
    }

    @Test
    fun sistemaOperativo_soloEspacios_esInvalido() {
        assertNotNull(ValidadorFormularios.validarSistemaOperativo("   "))
    }

    @Test
    fun sistemaOperativo_valido() {
        assertNull(ValidadorFormularios.validarSistemaOperativo("Android 14"))
    }
}
