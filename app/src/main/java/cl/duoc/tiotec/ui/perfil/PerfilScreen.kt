package cl.duoc.tiotec.ui.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotec.model.Dispositivo
import cl.duoc.tiotec.model.EstadoSoporte
import cl.duoc.tiotec.model.SoporteAndroid
import cl.duoc.tiotec.model.NivelExperiencia
import cl.duoc.tiotec.model.TipoDispositivo
import cl.duoc.tiotec.nativo.BotonTomarFoto
import cl.duoc.tiotec.nativo.FotoMiniatura
import cl.duoc.tiotec.nativo.VistaPreviaFoto
import cl.duoc.tiotec.theme.AppTioTecTheme
import cl.duoc.tiotec.ui.components.CampoTextoValidado
import cl.duoc.tiotec.ui.components.SelectorOpciones
import cl.duoc.tiotec.viewmodel.TioTecUiState

/**
 * Pantalla de Perfil ficticio (RF-01) e Inventario de dispositivos (RF-02).
 *
 * Solo recibe el estado y funciones: no conoce el ViewModel ni valida nada.
 * Cada vez que el usuario escribe o presiona un botón, avisa con una función
 * y vuelve a dibujarse con el nuevo estado.
 */
@Composable
fun PerfilScreen(
    state: TioTecUiState,
    onNombrePerfilChange: (String) -> Unit,
    onNivelExperienciaChange: (NivelExperiencia) -> Unit,
    onGuardarPerfil: () -> Unit,
    onNombreDispositivoChange: (String) -> Unit,
    onTipoDispositivoChange: (TipoDispositivo) -> Unit,
    onSistemaOperativoChange: (String) -> Unit,
    onFotoDispositivoChange: (String?) -> Unit,
    onRegistrarDispositivo: () -> Unit,
    modifier: Modifier = Modifier,
    onEliminarDispositivo: (String) -> Unit = {},
    onEditarDispositivo: (String) -> Unit = {},
    onCancelarEdicion: () -> Unit = {},
    onSincronizarServidor: () -> Unit = {},
    onConsultarSoporteAndroid: () -> Unit = {}
) {
    // La API externa se consulta una vez al abrir la pantalla (si aún no hay datos).
    LaunchedEffect(Unit) {
        if (state.versionesAndroid.isEmpty()) onConsultarSoporteAndroid()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TarjetaPerfil(
                state = state,
                onNombrePerfilChange = onNombrePerfilChange,
                onNivelExperienciaChange = onNivelExperienciaChange,
                onGuardarPerfil = onGuardarPerfil
            )
        }
        item {
            TarjetaRegistroDispositivo(
                state = state,
                onNombreDispositivoChange = onNombreDispositivoChange,
                onTipoDispositivoChange = onTipoDispositivoChange,
                onSistemaOperativoChange = onSistemaOperativoChange,
                onFotoDispositivoChange = onFotoDispositivoChange,
                onRegistrarDispositivo = onRegistrarDispositivo,
                onCancelarEdicion = onCancelarEdicion
            )
        }
        item {
            TarjetaServidor(state = state, onSincronizar = onSincronizarServidor)
        }
        item {
            TarjetaSoporteAndroid(state = state, onReintentar = onConsultarSoporteAndroid)
        }
        item {
            Text(
                text = "Mis dispositivos (${state.dispositivos.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (state.dispositivos.isEmpty()) {
            item {
                Text(
                    text = "Aún no registras dispositivos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(state.dispositivos, key = { it.id }) { dispositivo ->
            ItemDispositivo(
                dispositivo = dispositivo,
                soporte = if (state.versionesAndroid.isEmpty()) null
                else SoporteAndroid.evaluar(dispositivo.sistemaOperativo, state.versionesAndroid),
                enEdicion = state.dispositivoEnEdicionId == dispositivo.id,
                onEditar = { onEditarDispositivo(dispositivo.id) },
                onEliminar = { onEliminarDispositivo(dispositivo.id) }
            )
        }
    }
}

@Composable
private fun TarjetaPerfil(
    state: TioTecUiState,
    onNombrePerfilChange: (String) -> Unit,
    onNivelExperienciaChange: (NivelExperiencia) -> Unit,
    onGuardarPerfil: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Mi perfil ficticio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Actual: ${state.usuario.nombre} · ${state.usuario.nivelExperiencia.etiqueta}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CampoTextoValidado(
                valor = state.nombrePerfilInput,
                onValorChange = onNombrePerfilChange,
                etiqueta = "Nombre o alias",
                icono = Icons.Default.Person,
                error = state.errorNombrePerfil
            )

            SelectorOpciones(
                titulo = "Nivel de experiencia digital",
                opciones = NivelExperiencia.entries,
                seleccionada = state.nivelExperienciaSeleccionado,
                etiquetaDe = { it.etiqueta },
                onSeleccionar = onNivelExperienciaChange
            )

            Button(
                onClick = onGuardarPerfil,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text("Guardar perfil")
            }
        }
    }
}

@Composable
private fun TarjetaRegistroDispositivo(
    state: TioTecUiState,
    onNombreDispositivoChange: (String) -> Unit,
    onTipoDispositivoChange: (TipoDispositivo) -> Unit,
    onSistemaOperativoChange: (String) -> Unit,
    onFotoDispositivoChange: (String?) -> Unit,
    onRegistrarDispositivo: () -> Unit,
    onCancelarEdicion: () -> Unit = {}
) {
    val editando = state.dispositivoEnEdicionId != null
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (editando) "Editar dispositivo" else "Registrar dispositivo de prueba",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            CampoTextoValidado(
                valor = state.nombreDispositivoInput,
                onValorChange = onNombreDispositivoChange,
                etiqueta = "Nombre del dispositivo",
                icono = Icons.Default.Phone,
                error = state.errorNombreDispositivo
            )

            SelectorOpciones(
                titulo = "Tipo de dispositivo",
                opciones = TipoDispositivo.entries,
                seleccionada = state.tipoDispositivoSeleccionado,
                etiquetaDe = { it.etiqueta },
                onSeleccionar = onTipoDispositivoChange
            )

            CampoTextoValidado(
                valor = state.sistemaOperativoInput,
                onValorChange = onSistemaOperativoChange,
                etiqueta = "Sistema operativo (ej: Android 14)",
                icono = Icons.Default.Settings,
                error = state.errorSistemaOperativo
            )

            // Recurso nativo: foto opcional del dispositivo (#9)
            BotonTomarFoto(onFotoTomada = onFotoDispositivoChange)
            state.fotoDispositivoInput?.let { ruta -> VistaPreviaFoto(ruta) }

            Button(
                onClick = onRegistrarDispositivo,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    if (editando) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text(if (editando) "Guardar cambios" else "Registrar dispositivo")
            }
            if (editando) {
                OutlinedButton(onClick = onCancelarEdicion, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text("Cancelar edición")
                }
            }
        }
    }
}

/** Estado de la conexión con el microservicio Spring Boot. */
@Composable
private fun TarjetaServidor(state: TioTecUiState, onSincronizar: () -> Unit) {
    val color = when (state.servidorDispositivosOk) {
        true -> MaterialTheme.colorScheme.primary
        false -> MaterialTheme.colorScheme.error
        null -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Microservicio Spring Boot",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = state.urlServidorSpring + "api/v1/dispositivos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = when {
                    state.sincronizandoDispositivos -> "Sincronizando..."
                    state.estadoServidorDispositivos != null -> state.estadoServidorDispositivos
                    else -> "Aún no se conecta con el servidor"
                },
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
            OutlinedButton(
                onClick = onSincronizar,
                enabled = !state.sincronizandoDispositivos,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text("Sincronizar con el servidor")
            }
        }
    }
}

/** Resumen de la API externa endoflife.date. */
@Composable
private fun TarjetaSoporteAndroid(state: TioTecUiState, onReintentar: () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Soporte de Android (API externa endoflife.date)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            when {
                state.cargandoSoporteAndroid -> Text("Consultando...", style = MaterialTheme.typography.bodySmall)
                state.errorSoporteAndroid != null -> {
                    Text(
                        text = state.errorSoporteAndroid,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    OutlinedButton(onClick = onReintentar, modifier = Modifier.fillMaxWidth()) {
                        Text("Reintentar")
                    }
                }
                state.versionesAndroid.isNotEmpty() -> {
                    val conSoporte = state.versionesAndroid.filter { it.conSoporte }
                    Text(
                        text = "Versiones con soporte de seguridad: " +
                            conSoporte.joinToString { "Android ${it.version}" },
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Escribe el sistema como \"Android 14\" para ver el estado de cada dispositivo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> Text("Sin datos todavía", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ItemDispositivo(
    dispositivo: Dispositivo,
    soporte: EstadoSoporte? = null,
    enEdicion: Boolean = false,
    onEditar: () -> Unit = {},
    onEliminar: () -> Unit = {}
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (dispositivo.fotoUri != null) {
                FotoMiniatura(ruta = dispositivo.fotoUri)
            } else {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = dispositivo.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${dispositivo.tipo.etiqueta} · ${dispositivo.sistemaOperativo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (soporte != null) {
                    Text(
                        text = soporte.texto,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (soporte) {
                            is EstadoSoporte.ConSoporte -> MaterialTheme.colorScheme.primary
                            is EstadoSoporte.SinSoporte -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                if (enEdicion) {
                    Text(
                        text = "Editando...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            IconButton(onClick = onEditar) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar dispositivo",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar dispositivo",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PerfilScreenPreview() {
    AppTioTecTheme {
        PerfilScreen(
            state = TioTecUiState(nombrePerfilInput = "Al", errorNombrePerfil = "El nombre debe tener al menos 3 caracteres"),
            onNombrePerfilChange = {},
            onNivelExperienciaChange = {},
            onGuardarPerfil = {},
            onNombreDispositivoChange = {},
            onTipoDispositivoChange = {},
            onSistemaOperativoChange = {},
            onFotoDispositivoChange = {},
            onRegistrarDispositivo = {}
        )
    }
}
