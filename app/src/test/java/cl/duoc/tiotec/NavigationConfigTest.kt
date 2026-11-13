package cl.duoc.tiotec

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationConfigTest {

    @Test
    fun barraInferior_contieneExactamenteCuatroDestinosPrincipales() {
        val esperados = listOf(
            SeccionNavegacion.INICIO,
            SeccionNavegacion.CUIDADO,
            SeccionNavegacion.EVALUAR,
            SeccionNavegacion.CHECKLIST
        )

        assertEquals(4, SeccionNavegacion.DESTINOS_BARRA_INFERIOR.size)
        assertEquals(esperados, SeccionNavegacion.DESTINOS_BARRA_INFERIOR)
        assertTrue(SeccionNavegacion.DESTINOS_BARRA_INFERIOR.all { it.enBarraInferior })
    }

    @Test
    fun seccionesSecundarias_quedanExcluidasDeBarraInferiorYAgrupadasEnSoporte() {
        val secundarias = listOf(
            SeccionNavegacion.HISTORIAL,
            SeccionNavegacion.PERFIL,
            SeccionNavegacion.ASISTENCIA
        )

        secundarias.forEach { seccion ->
            assertFalse(
                "${seccion.name} no debe ocupar espacio en la barra inferior",
                seccion.enBarraInferior
            )
            assertEquals(GrupoMenuNavegacion.SOPORTE, seccion.grupoMenu)
        }
    }

    @Test
    fun menuHamburguesa_agrupaTodasLasSeccionesConTitulosUnicosYSubtitulosValidos() {
        val seccionesPorGrupo = GrupoMenuNavegacion.entries.flatMap { grupo ->
            SeccionNavegacion.entries.filter { it.grupoMenu == grupo }
        }

        assertEquals(SeccionNavegacion.entries.size, seccionesPorGrupo.size)
        assertEquals(
            SeccionNavegacion.entries.size,
            SeccionNavegacion.entries.map { it.titulo }.toSet().size
        )
        assertTrue(SeccionNavegacion.entries.all { it.subtitulo.isNotBlank() })
    }
}
