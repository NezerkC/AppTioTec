package cl.duoc.tiotec.ui.evaluacion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.tiotec.model.PreguntaEvaluacion

/**
 * Cuestionario preventivo de los 4 pilares (RF-03).
 *
 * Muestra cada pregunta con sus opciones. Si el usuario intenta finalizar con
 * preguntas sin responder, esas tarjetas se marcan en rojo con ícono y mensaje.
 */
@Composable
fun CuestionarioScreen(
    preguntas: List<PreguntaEvaluacion>,
    respuestas: Map<Int, Int>,
    preguntasSinResponder: Set<Int>,
    mensajeError: String?,
    onSeleccionarRespuesta: (preguntaId: Int, puntaje: Int) -> Unit,
    onFinalizar: () -> Unit,
    modifier: Modifier = Modifier
) {
    // La barra de progreso avanza con animación cada vez que se responde una pregunta
    val progreso by animateFloatAsState(
        targetValue = if (preguntas.isEmpty()) 0f else respuestas.size.toFloat() / preguntas.size,
        label = "progresoCuestionario"
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Cuestionario preventivo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Responde con sinceridad: no hay respuestas buenas ni malas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.size(12.dp))
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${respuestas.size} de ${preguntas.size} respondidas",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(preguntas, key = { it.id }) { pregunta ->
            TarjetaPregunta(
                pregunta = pregunta,
                puntajeElegido = respuestas[pregunta.id],
                pendiente = pregunta.id in preguntasSinResponder,
                onSeleccionar = { puntaje -> onSeleccionarRespuesta(pregunta.id, puntaje) }
            )
        }

        item {
            AnimatedVisibility(visible = mensajeError != null, enter = fadeIn(), exit = fadeOut()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text(
                        text = mensajeError.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            Button(onClick = onFinalizar, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text("Ver mi diagnóstico")
            }
        }
    }
}

@Composable
private fun TarjetaPregunta(
    pregunta: PreguntaEvaluacion,
    puntajeElegido: Int?,
    pendiente: Boolean,
    onSeleccionar: (Int) -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = if (pendiente) BorderStroke(2.dp, MaterialTheme.colorScheme.error) else CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = pregunta.categoria.titulo.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = pregunta.enunciado,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            pregunta.opciones.forEach { opcion ->
                val elegida = puntajeElegido == opcion.puntaje
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = elegida,
                            onClick = { onSeleccionar(opcion.puntaje) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = elegida, onClick = null)
                    Text(
                        text = opcion.texto,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            AnimatedVisibility(visible = pendiente) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Responde esta pregunta",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
    }
}
