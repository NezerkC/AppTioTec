package cl.duoc.tiotec.ui.evaluacion

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.tiotec.model.PrioridadAccion
import cl.duoc.tiotec.viewmodel.TioTecUiState

/**
 * Pestaña "Evaluar": muestra el cuestionario o, si se acaba de terminar,
 * el resultado. El cambio entre ambos es animado (desliza + aparece).
 */
@Composable
fun EvaluacionScreen(
    state: TioTecUiState,
    onSeleccionarRespuesta: (Int, Int) -> Unit,
    onFinalizar: () -> Unit,
    onVerChecklist: () -> Unit,
    onNuevaEvaluacion: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = state.resultadoReciente,
        transitionSpec = {
            (slideInHorizontally { ancho -> ancho / 3 } + fadeIn()) togetherWith fadeOut()
        },
        label = "cuestionarioOResultado",
        modifier = modifier
    ) { resultado ->
        if (resultado == null) {
            CuestionarioScreen(
                preguntas = state.preguntas,
                respuestas = state.respuestasActuales,
                preguntasSinResponder = state.preguntasSinResponder,
                mensajeError = state.errorFormulario,
                onSeleccionarRespuesta = onSeleccionarRespuesta,
                onFinalizar = onFinalizar
            )
        } else {
            ResultadoEvaluacionScreen(
                evaluacion = resultado,
                accionesGeneradas = state.accionesGeneradas,
                accionesUrgentesPendientes = state.checklist.count {
                    !it.completada && it.prioridad == PrioridadAccion.ALTA
                },
                onVerChecklist = onVerChecklist,
                onNuevaEvaluacion = onNuevaEvaluacion
            )
        }
    }
}
