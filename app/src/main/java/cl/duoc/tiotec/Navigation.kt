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
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import cl.duoc.tiotec.model.PrioridadAccion
import cl.duoc.tiotec.ui.evaluacion.EvaluacionScreen
import cl.duoc.tiotec.ui.historial.HistorialScreen
import cl.duoc.tiotec.ui.main.MainScreenContent
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import cl.duoc.tiotec.ui.asistencia.AsistenciaScreen
import cl.duoc.tiotec.ui.cuidado.CuidadoTechScreen
import cl.duoc.tiotec.ui.perfil.PerfilScreen
import cl.duoc.tiotec.viewmodel.TioTecUiState
import cl.duoc.tiotec.data.local.TioTecDatabase
import cl.duoc.tiotec.repository.TioTecRepository
import cl.duoc.tiotec.repository.PreferenciasDataStore
import cl.duoc.tiotec.viewmodel.TioTecViewModel

enum class GrupoMenuNavegacion(val titulo: String) {
    PRINCIPAL("Navegación principal"),
    SOPORTE("Gestión y soporte")
}

enum class SeccionNavegacion(
    val titulo: String,
    val icon: ImageVector,
    val subtitulo: String,
    val enBarraInferior: Boolean,
    val grupoMenu: GrupoMenuNavegacion
) {
    INICIO("Inicio", Icons.Default.Home, "Dashboard de Seguridad", true, GrupoMenuNavegacion.PRINCIPAL),
    CUIDADO("Cuidado", Icons.Default.Favorite, "Salud de Hardware y Bienestar", true, GrupoMenuNavegacion.PRINCIPAL),
    EVALUAR("Evaluar", Icons.Default.Info, "Diagnóstico de 4 Pilares", true, GrupoMenuNavegacion.PRINCIPAL),
    CHECKLIST("Checklist", Icons.Default.Check, "Plan de Acciones", true, GrupoMenuNavegacion.PRINCIPAL),
    HISTORIAL("Historial", Icons.Default.DateRange, "Evaluaciones Anteriores", false, GrupoMenuNavegacion.SOPORTE),
    PERFIL("Perfil", Icons.Default.Person, "Perfil y Dispositivos", false, GrupoMenuNavegacion.SOPORTE),
    ASISTENCIA("Asistencia", Icons.Default.Build, "Asistencia Técnica Simulada", false, GrupoMenuNavegacion.SOPORTE);

    companion object {
        val DESTINOS_BARRA_INFERIOR: List<SeccionNavegacion> = entries.filter { it.enBarraInferior }
    }
}

@Composable
fun MainNavigation(
    viewModel: TioTecViewModel = viewModel {
        val app = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
        if (app != null) {
            val db = TioTecDatabase.getInstance(app)
            val repo = TioTecRepository(
                dao = db.tioTecDao(),
                diagnosticoDao = db.diagnosticoHardwareDao(),
                apiService = cl.duoc.tiotec.data.remote.RetrofitClient.crearServicio()
            )
            val ds = PreferenciasDataStore(app)
            TioTecViewModel(repository = repo, preferenciasDataStore = ds)
        } else {
            TioTecViewModel()
        }
    }
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

    NavigationScaffoldConDrawer(
        seccionActual = seccionActual,
        accionesPendientes = accionesPendientes,
        nombreUsuario = state.usuario.nombre,
        snackbarHostState = snackbarHostState,
        onSeleccionarSeccion = { seccionActual = it }
    ) { contentModifier ->
        Box(modifier = contentModifier) {
            when (seccionActual) {
                SeccionNavegacion.INICIO -> {
                    MainScreenContent(
                        state = state,
                        onToggleAccion = { id -> viewModel.alternarEstadoAccion(id) },
                        onEvaluarClick = { seccionActual = SeccionNavegacion.EVALUAR },
                        onChecklistClick = { seccionActual = SeccionNavegacion.CHECKLIST },
                        onPerfilClick = { seccionActual = SeccionNavegacion.PERFIL },
                        onCuidadoClick = { seccionActual = SeccionNavegacion.CUIDADO }
                    )
                }
                SeccionNavegacion.CUIDADO -> {
                    CuidadoTechScreen(
                        state = state,
                        onEscanear = { ctx -> viewModel.ejecutarEscaneoMantenimiento(ctx) },
                        onGenerarAccionesChecklist = { viewModel.generarAccionesCuidadoEnChecklist() },
                        onRefrescarEnVivo = { ctx -> viewModel.refrescarTelemetriaEnVivo(ctx) },
                        onAlternarEnVivo = { viewModel.alternarMonitoreoEnVivo() },
                        onGuardarEnRoom = { viewModel.guardarDiagnosticoEnRoom() },
                        onSincronizarConBackend = { id -> viewModel.sincronizarDiagnosticoConBackend(id) },
                        onConsultarRemoto = { viewModel.consultarDiagnosticosRemotos() },
                        onUrlServidorChange = { url -> viewModel.onUrlServidorSpringChange(url) }
                    )
                }
                SeccionNavegacion.EVALUAR -> {
                    EvaluacionScreen(
                        state = state,
                        onSeleccionarRespuesta = { id, puntaje -> viewModel.seleccionarRespuesta(id, puntaje) },
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
                        onGuardarPerfil = { viewModel.actualizarPerfil() },
                        onNombreDispositivoChange = viewModel::onNombreDispositivoChange,
                        onTipoDispositivoChange = viewModel::onTipoDispositivoChange,
                        onSistemaOperativoChange = viewModel::onSistemaOperativoChange,
                        onFotoDispositivoChange = viewModel::onFotoDispositivoChange,
                        onRegistrarDispositivo = { viewModel.agregarDispositivo() }
                    )
                }
                SeccionNavegacion.ASISTENCIA -> {
                    AsistenciaScreen(
                        state = state,
                        onCategoriaChange = viewModel::onCategoriaAsistenciaChange,
                        onDescripcionChange = viewModel::onDescripcionAsistenciaChange,
                        onEnviarSolicitud = viewModel::registrarSolicitudAsistencia,
                        onToggleEstado = viewModel::alternarEstadoSolicitudAsistencia
                    )
                }
            }
        }
    }
}

/**
 * Contenedor principal de navegación con menú hamburguesa lateral (todas las secciones)
 * y barra inferior compacta limitada a los 4 destinos de uso frecuente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationScaffoldConDrawer(
    seccionActual: SeccionNavegacion,
    accionesPendientes: Int,
    nombreUsuario: String,
    onSeleccionarSeccion: (SeccionNavegacion) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    content: @Composable (Modifier) -> Unit
) {
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ContenidoMenuHamburguesa(
                seccionActual = seccionActual,
                accionesPendientes = accionesPendientes,
                nombreUsuario = nombreUsuario,
                onSeleccionarSeccion = { destino ->
                    onSeleccionarSeccion(destino)
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("hamburger_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Abrir menú de navegación"
                            )
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = "TíoTec Preventivo · MCmovil",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = seccionActual.subtitulo,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.testTag("bottom_navigation_bar"),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    SeccionNavegacion.DESTINOS_BARRA_INFERIOR.forEach { seccion ->
                        val isSelected = seccionActual == seccion
                        NavigationBarItem(
                            modifier = Modifier.testTag("bottom_nav_${seccion.name}"),
                            selected = isSelected,
                            onClick = { onSeleccionarSeccion(seccion) },
                            icon = {
                                IconoSeccionConBadge(
                                    seccion = seccion,
                                    accionesPendientes = accionesPendientes
                                )
                            },
                            label = {
                                Text(
                                    text = seccion.titulo,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            content(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
private fun ContenidoMenuHamburguesa(
    seccionActual: SeccionNavegacion,
    accionesPendientes: Int,
    nombreUsuario: String,
    onSeleccionarSeccion: (SeccionNavegacion) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .testTag("drawer_navigation_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "TíoTec Preventivo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "MCmovil · ${nombreUsuario.ifBlank { "Usuario" }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val grupos = GrupoMenuNavegacion.entries
            grupos.forEachIndexed { index, grupo ->
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = grupo.titulo,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                SeccionNavegacion.entries
                    .filter { it.grupoMenu == grupo }
                    .forEach { seccion ->
                        NavigationDrawerItem(
                            modifier = Modifier
                                .padding(NavigationDrawerItemDefaults.ItemPadding)
                                .testTag("drawer_item_${seccion.name}"),
                            label = {
                                Text(
                                    text = seccion.titulo,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            selected = seccionActual == seccion,
                            onClick = { onSeleccionarSeccion(seccion) },
                            icon = {
                                IconoSeccionConBadge(
                                    seccion = seccion,
                                    accionesPendientes = accionesPendientes
                                )
                            }
                        )
                    }
                if (index == grupos.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun IconoSeccionConBadge(
    seccion: SeccionNavegacion,
    accionesPendientes: Int
) {
    val icono = @Composable {
        Icon(
            imageVector = seccion.icon,
            contentDescription = seccion.titulo,
            modifier = Modifier.size(24.dp)
        )
    }

    if (seccion == SeccionNavegacion.CHECKLIST && accionesPendientes > 0) {
        BadgedBox(badge = { Badge { Text("$accionesPendientes") } }) {
            icono()
        }
    } else {
        icono()
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
