package cl.duoc.tiotec.ui.evaluacion

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.tiotec.model.AccionChecklist
import cl.duoc.tiotec.model.EvaluacionSeguridad
import cl.duoc.tiotec.model.PrioridadAccion
import cl.duoc.tiotec.nativo.BotonRecordatorio
import cl.duoc.tiotec.ui.components.coloresDe

/**
 * Resultado de la evaluación (RF-04): nivel de riesgo con su color,
 * porcentaje de cumplimiento y las acciones que se agregaron al checklist (RN-02).
 */
@Composable
fun ResultadoEvaluacionScreen(
    evaluacion: EvaluacionSeguridad,
    accionesGeneradas: List<AccionChecklist>,
    accionesUrgentesPendientes: Int,
    onVerChecklist: () -> Unit,
    onNuevaEvaluacion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = coloresDe(evaluacion.nivelRiesgo)

    // La barra de cumplimiento se "llena" desde 0 al mostrar el resultado
    var objetivo by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(evaluacion.id) { objetivo = (evaluacion.porcentajeCumplimiento / 100.0).toFloat() }
    val progreso by animateFloatAsState(
        targetValue = objetivo,
        animationSpec = tween(durationMillis = 1200),
        label = "barraCumplimiento"
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colores.fondo)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Tu diagnóstico",
                        style = MaterialTheme.typography.labelLarge,
                        color = colores.texto
                    )
                    Text(
                        text = evaluacion.nivelRiesgo.etiqueta,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = colores.acento
                    )
                    Text(
                        text = evaluacion.nivelRiesgo.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colores.texto,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    LinearProgressIndicator(
                        progress = { progreso },
                        color = colores.acento,
                        trackColor = colores.acento.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth().height(10.dp)
                    )
                    Text(
                        text = "Cumplimiento: ${(progreso * 100).toInt()}% · ${evaluacion.puntajeTotal}/${evaluacion.puntajeMaximo} puntos",
                        style = MaterialTheme.typography.labelMedium,
                        color = colores.texto,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        item {
            Text(
                text = if (accionesGeneradas.isEmpty()) "¡Sin brechas detectadas!" else "Acciones agregadas a tu checklist",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (accionesGeneradas.isEmpty()) {
                Text(
                    text = "Mantén tus hábitos y vuelve a evaluarte en un mes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(accionesGeneradas, key = { it.id }) { accion ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    val urgente = accion.prioridad == PrioridadAccion.ALTA
                    Icon(
                        imageVector = if (urgente) Icons.Default.Warning else Icons.Default.Check,
                        contentDescription = null,
                        tint = if (urgente) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                    )
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(text = accion.titulo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${accion.prioridad.etiqueta} · ${accion.fechaSugerida}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            // Recurso nativo: notificación local (#9)
            BotonRecordatorio(accionesUrgentes = accionesUrgentesPendientes)
            Button(onClick = onVerChecklist, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text("Ir a mi checklist")
            }
            OutlinedButton(onClick = onNuevaEvaluacion, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text("Nueva evaluación")
            }
        }
    }
}
