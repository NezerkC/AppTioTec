package cl.duoc.tiotec.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import cl.duoc.tiotec.theme.AppTioTecTheme
import cl.duoc.tiotec.viewmodel.TioTecUiState
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Pruebas UI para [cl.duoc.tiotec.ui.main.MainScreenContent]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent {
      AppTioTecTheme {
        MainScreenContent(state = TioTecUiState())
      }
    }
  }

  @Test
  fun dashboardPrincipal_muestraResumenDeSaludYCuidado() {
    composeTestRule.onNodeWithText("Salud del Dispositivo y Uso").assertExists()
    composeTestRule.onNodeWithText("Ver Cuidado").assertExists()
  }
}
