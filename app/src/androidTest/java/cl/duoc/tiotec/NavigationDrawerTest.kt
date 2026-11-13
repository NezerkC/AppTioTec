package cl.duoc.tiotec

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import cl.duoc.tiotec.theme.AppTioTecTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NavigationDrawerTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun barraInferior_muestraSoloCuatroDestinosPrincipales() {
        composeRule.setContent {
            AppTioTecTheme {
                NavigationScaffoldConDrawer(
                    seccionActual = SeccionNavegacion.INICIO,
                    accionesPendientes = 5,
                    nombreUsuario = "Victor",
                    onSeleccionarSeccion = {}
                ) { modifier ->
                    Text("Contenido Inicio", modifier = modifier)
                }
            }
        }

        composeRule.onNodeWithTag("bottom_navigation_bar").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom_nav_INICIO").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom_nav_CUIDADO").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom_nav_EVALUAR").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom_nav_CHECKLIST").assertIsDisplayed()

        composeRule.onNodeWithTag("bottom_nav_HISTORIAL").assertDoesNotExist()
        composeRule.onNodeWithTag("bottom_nav_PERFIL").assertDoesNotExist()
        composeRule.onNodeWithTag("bottom_nav_ASISTENCIA").assertDoesNotExist()
    }

    @Test
    fun menuHamburguesa_abreDrawerYPermiteNavegarASeccionesSecundarias() {
        var destinoSeleccionado: SeccionNavegacion? = null

        composeRule.setContent {
            AppTioTecTheme {
                var seccionActual by remember { mutableStateOf(SeccionNavegacion.INICIO) }
                NavigationScaffoldConDrawer(
                    seccionActual = seccionActual,
                    accionesPendientes = 3,
                    nombreUsuario = "Victor",
                    onSeleccionarSeccion = {
                        seccionActual = it
                        destinoSeleccionado = it
                    }
                ) { modifier ->
                    Text("Pantalla ${seccionActual.titulo}", modifier = modifier)
                }
            }
        }

        composeRule.onNodeWithContentDescription("Abrir menú de navegación").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("drawer_item_HISTORIAL").assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_PERFIL").assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_ASISTENCIA").assertIsDisplayed()

        composeRule.onNodeWithTag("drawer_item_ASISTENCIA").performClick()
        composeRule.waitForIdle()

        assertEquals(SeccionNavegacion.ASISTENCIA, destinoSeleccionado)
        composeRule.onNodeWithText("Asistencia Técnica Simulada").assertIsDisplayed()
        composeRule.onNodeWithText("Pantalla Asistencia").assertIsDisplayed()
    }
}
