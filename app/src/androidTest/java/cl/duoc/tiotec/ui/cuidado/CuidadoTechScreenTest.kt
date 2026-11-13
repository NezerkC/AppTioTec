package cl.duoc.tiotec.ui.cuidado

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import cl.duoc.tiotec.model.AlmacenamientoInfo
import cl.duoc.tiotec.model.BateriaInfo
import cl.duoc.tiotec.model.DiagnosticoSistema
import cl.duoc.tiotec.model.InventarioSensores
import cl.duoc.tiotec.model.NivelRiesgo
import cl.duoc.tiotec.model.RamInfo
import cl.duoc.tiotec.model.SensorHardwareInfo
import cl.duoc.tiotec.theme.AppTioTecTheme
import cl.duoc.tiotec.viewmodel.TioTecUiState
import org.junit.Rule
import org.junit.Test

class CuidadoTechScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun cuidadoTechScreen_muestraTarjetasDeHardwareEstable() {
        val diagnosticoMock = DiagnosticoSistema(
            timestampMs = 1700000000000L,
            fechaFormateada = "13/11/2026 12:00:00",
            bateria = BateriaInfo(
                nivelPorcentaje = 88,
                temperaturaCelsius = 31.0f,
                voltajeMv = 4100,
                estaCargando = false,
                fuenteEnergia = "Batería"
            ),
            ram = RamInfo(
                totalMb = 6144L,
                disponibleMb = 3072L,
                memoriaBajaSistema = false
            ),
            almacenamiento = AlmacenamientoInfo(
                totalGb = 128.0,
                libreGb = 64.0
            ),
            sensores = InventarioSensores(
                sensores = listOf(
                    SensorHardwareInfo(
                        nombre = "Sensor de Luz Ambiental",
                        fabricante = "TíoTec Hardware",
                        tipo = "Sensor de Luz",
                        consumoMa = 0.45f
                    )
                )
            ),
            nivelRiesgo = NivelRiesgo.BAJO
        )

        val stateMock = TioTecUiState(
            diagnosticoSistema = diagnosticoMock
        )

        composeTestRule.setContent {
            AppTioTecTheme {
                CuidadoTechScreen(
                    state = stateMock,
                    onEscanear = {},
                    onGenerarAccionesChecklist = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Diagnóstico Preventivo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Batería y Energía").assertIsDisplayed()
        composeTestRule.onNodeWithText("Memoria RAM").assertIsDisplayed()
        composeTestRule.onNodeWithText("Almacenamiento Interno").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sensores Físicos").assertIsDisplayed()
    }
}
