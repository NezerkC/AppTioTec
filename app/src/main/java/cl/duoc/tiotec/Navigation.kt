package cl.duoc.tiotec

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.tiotec.model.PrioridadAccion
import cl.duoc.tiotec.ui.evaluacion.EvaluacionScreen
import cl.duoc.tiotec.ui.historial.HistorialScreen
import cl.duoc.tiotec.ui.main.MainScreenContent
import cl.duoc.tiotec.ui.perfil.PerfilScreen
import cl.duoc.tiotec.viewmodel.TioTecUiState
import cl.duoc.tiotec.viewmodel.TioTecViewModel

enum class SeccionNavegacion(
    val titulo: String,
    val icon: ImageVector,
    val subtitulo: String
) {
    INICIO("Inicio", Icons.Default.Home, "Dashboard de Seguridad"),
    EVALUAR("Evaluar", Icons.Default.Info, "Diagnóstico de 4 Pilares"),
    CHECKLIST("Checklist", Icons.Default.Check, "Plan de Acciones"),
    HISTORIAL("Historial", Icons.Default.DateRange, "Evaluaciones Anteriores"),
    PERFIL("Perfil", Icons.Default.Person, "Perfil y Dispositivos")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    viewModel: TioTecViewModel = viewModel { TioTecViewModel() }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var seccionActual by rememberSaveable { mutableStateOf(SeccionNavegacion.INICIO) }

    val accionesPendientes = state.checklist.count { !it.completada }

    // Mensajes de éxito (perfil guardado, dispositivo registrado...) como Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.mensajeExito) {
        state.mensajeExito?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.limpiarMensajes()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "TíoTec Preventivo · MCmovil",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = seccionActual.subtitulo,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                SeccionNavegacion.entries.forEach { seccion ->
                    val isSelected = seccionActual == seccion
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { seccionActual = seccion },
                        icon = {
                            if (seccion == SeccionNavegacion.CHECKLIST && accionesPendientes > 0) {
                                BadgedBox(badge = { Badge { Text("$accionesPendientes") } }) {
                                    Icon(
                                        imageVector = seccion.icon,
                                        contentDescription = seccion.titulo,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = seccion.icon,
                                    contentDescription = seccion.titulo,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = seccion.titulo,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (seccionActual) {
                SeccionNavegacion.INICIO -> {
                    MainScreenContent(
                        state = state,
                        onToggleAccion = { id -> viewModel.alternarEstadoAccion(id) },
                        onEvaluarClick = { seccionActual = SeccionNavegacion.EVALUAR },
                        onChecklistClick = { seccionActual = SeccionNavegacion.CHECKLIST },
                        onPerfilClick = { seccionActual = SeccionNavegacion.PERFIL }
                    )
                }
                SeccionNavegacion.EVALUAR -> {
                    EvaluacionScreen(
                        state = state,
                        onSeleccionarRespuesta = viewModel::seleccionarRespuesta,
                        onFinalizar = { viewModel.finalizarEvaluacion() },
                        onVerChecklist = {
                            viewModel.cerrarResultado()
                            seccionActual = SeccionNavegacion.CHECKLIST
                        },
                        onNuevaEvaluacion = viewModel::cerrarResultado
                    )
                }
                SeccionNavegacion.CHECKLIST -> {
                    PantallaSeccionChecklist(
                        state = state,
                        onToggleAccion = { id -> viewModel.alternarEstadoAccion(id) }
                    )
                }
                SeccionNavegacion.HISTORIAL -> {
                    HistorialScreen(historial = state.historial)
                }
                SeccionNavegacion.PERFIL -> {
                    PerfilScreen(
                        state = state,
                        onNombrePerfilChange = viewModel::onNombrePerfilChange,
                        onNivelExperienciaChange = viewModel::onNivelExperienciaChange,
                        onGuardarPerfil = viewModel::guardarPerfil,
                        onNombreDispositivoChange = viewModel::onNombreDispositivoChange,
                        onTipoDispositivoChange = viewModel::onTipoDispositivoChange,
                        onSistemaOperativoChange = viewModel::onSistemaOperativoChange,
                        onRegistrarDispositivo = viewModel::registrarDispositivo
                    )
                }
            }
        }
    }
}

@Composable
private fun PantallaSeccionChecklist(
    state: TioTecUiState,
    onToggleAccion: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Checklist de Acciones Preventivas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Tareas priorizadas según tu diagnóstico de seguridad digital.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        items(state.checklist, key = { it.id }) { accion ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (accion.completada)
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = accion.completada,
                        onCheckedChange = { onToggleAccion(accion.id) }
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        Text(
                            text = accion.titulo,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (accion.completada) FontWeight.Normal else FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${accion.categoria.titulo} · ${accion.prioridad.etiqueta} · ${accion.fechaSugerida}",
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
    }
}
