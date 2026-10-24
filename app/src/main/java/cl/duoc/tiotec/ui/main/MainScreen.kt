package cl.duoc.tiotec.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import cl.duoc.tiotec.model.AccionChecklist
import cl.duoc.tiotec.model.Dispositivo
import cl.duoc.tiotec.model.EvaluacionSeguridad
import cl.duoc.tiotec.model.NivelExperiencia
import cl.duoc.tiotec.model.NivelRiesgo
import cl.duoc.tiotec.model.PrioridadAccion
import cl.duoc.tiotec.model.TipoDispositivo
import cl.duoc.tiotec.model.Usuario
import cl.duoc.tiotec.theme.AppTioTecTheme
import cl.duoc.tiotec.viewmodel.TioTecUiState
import cl.duoc.tiotec.repository.PreferenciasDataStore
import cl.duoc.tiotec.viewmodel.TioTecViewModel

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: TioTecViewModel = viewModel {
        val app = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
        if (app != null) TioTecViewModel(preferenciasDataStore = PreferenciasDataStore(app))
        else TioTecViewModel()
    }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MainScreenContent(
        state = state,
        onToggleAccion = { id -> viewModel.alternarEstadoAccion(id) },
        onEvaluarClick = { /* Navegación a evaluación (Guía 10) */ },
        onChecklistClick = { /* Navegación a checklist (Guía 10) */ },
        onPerfilClick = { /* Navegación a perfil (Guía 10) */ },
        modifier = modifier
    )
}

@Composable
internal fun MainScreenContent(
    state: TioTecUiState,
    onToggleAccion: (String) -> Unit = {},
    onEvaluarClick: () -> Unit = {},
    onChecklistClick: () -> Unit = {},
    onPerfilClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderUsuario(usuario = state.usuario, onPerfilClick = onPerfilClick)
        }

        item {
            SemaforoRiesgoCard(
                evaluacion = state.ultimaEvaluacion,
                onNuevaEvaluacion = onEvaluarClick
            )
        }

        item {
            MetricasRapidasRow(
                totalDispositivos = state.dispositivos.size,
                accionesPendientes = state.checklist.count { !it.completada }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEvaluarClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Evaluar Ahora", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onChecklistClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ver Checklist", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        item {
            Text(
                text = "Acciones Preventivas Sugeridas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        val accionesUrgentes = state.checklist.take(3)
        if (accionesUrgentes.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "No hay acciones pendientes en este momento.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(accionesUrgentes, key = { it.id }) { accion ->
                AccionResumenCard(
                    accion = accion,
                    onToggle = { onToggleAccion(accion.id) }
                )
            }
        }

        item {
            Text(
                text = "Dispositivos de Prueba Registrados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (state.dispositivos.isEmpty()) {
            item {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Aún no se registran dispositivos. Agrega uno en la pestaña de Perfil.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(state.dispositivos, key = { it.id }) { disp ->
                DispositivoCard(dispositivo = disp)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeaderUsuario(
    usuario: Usuario,
    onPerfilClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "TíoTec Preventivo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Hola, ${usuario.nombre} (${usuario.nivelExperiencia.etiqueta})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        FilledTonalButton(
            onClick = onPerfilClick,
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Perfil", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SemaforoRiesgoCard(
    evaluacion: EvaluacionSeguridad?,
    onNuevaEvaluacion: () -> Unit
) {
    val nivel = evaluacion?.nivelRiesgo ?: NivelRiesgo.MEDIO

    val (cardColor, contentColor, badgeColor) = when (nivel) {
        NivelRiesgo.BAJO -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF1B5E20),
            Color(0xFF2E7D32)
        )
        NivelRiesgo.MEDIO -> Triple(
            Color(0xFFFFF8E1),
            Color(0xFFE65100),
            Color(0xFFF57C00)
        )
        NivelRiesgo.ALTO -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFB71C1C),
            Color(0xFFD32F2F)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ESTADO DE SEGURIDAD",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    letterSpacing = 1.sp
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor
                ) {
                    Text(
                        text = nivel.etiqueta,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (evaluacion != null) {
                Text(
                    text = "${evaluacion.porcentajeCumplimiento.toInt()}% Cumplimiento Preventivo",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor
                )
                Text(
                    text = nivel.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.9f)
                )
                HorizontalDivider(color = contentColor.copy(alpha = 0.2f), thickness = 1.dp)
                Text(
                    text = "Última evaluación registrada: ${evaluacion.fecha} (${evaluacion.puntajeTotal}/${evaluacion.puntajeMaximo} pts)",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            } else {
                Text(
                    text = "Sin evaluaciones previas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = "Realiza tu primer diagnóstico de 4 pilares para calcular tu nivel de riesgo digital.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.85f)
                )
                Button(
                    onClick = onNuevaEvaluacion,
                    colors = ButtonDefaults.buttonColors(containerColor = badgeColor)
                ) {
                    Text("Comenzar Evaluación", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun MetricasRapidasRow(
    totalDispositivos: Int,
    accionesPendientes: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ElevatedCard(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Dispositivos",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$totalDispositivos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Equipos de prueba",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        ElevatedCard(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Checklist",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$accionesPendientes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (accionesPendientes > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Tareas por atender",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun AccionResumenCard(
    accion: AccionChecklist,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (accion.completada) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = accion.completada,
                onCheckedChange = { onToggle() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = accion.titulo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (accion.completada) FontWeight.Normal else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${accion.categoria.titulo} · ${accion.prioridad.etiqueta}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (accion.prioridad) {
                        PrioridadAccion.ALTA -> MaterialTheme.colorScheme.error
                        PrioridadAccion.MEDIA -> MaterialTheme.colorScheme.tertiary
                        PrioridadAccion.BAJA -> MaterialTheme.colorScheme.outline
                    }
                )
            }
        }
    }
}

@Composable
private fun DispositivoCard(dispositivo: Dispositivo) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dispositivo.nombre.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = dispositivo.nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${dispositivo.tipo.etiqueta} · ${dispositivo.sistemaOperativo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            SuggestionChip(
                onClick = {},
                label = { Text(dispositivo.sistemaOperativo, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AppTioTecTheme {
        MainScreenContent(
            state = TioTecUiState(
                usuario = Usuario(nombre = "Constanza Pérez", nivelExperiencia = NivelExperiencia.INTERMEDIO),
                ultimaEvaluacion = EvaluacionSeguridad(
                    id = "eval_preview",
                    fecha = "14-10-2026",
                    puntajeTotal = 5,
                    puntajeMaximo = 8,
                    nivelRiesgo = NivelRiesgo.MEDIO,
                    respuestas = emptyMap()
                ),
                dispositivos = listOf(
                    Dispositivo(id = "1", nombre = "Pixel 7 Pro", tipo = TipoDispositivo.SMARTPHONE, sistemaOperativo = "Android 14")
                )
            )
        )
    }
}
