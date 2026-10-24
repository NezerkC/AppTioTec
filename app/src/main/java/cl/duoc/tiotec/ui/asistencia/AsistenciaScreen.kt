package cl.duoc.tiotec.ui.asistencia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.tiotec.model.CategoriaSeguridad
import cl.duoc.tiotec.model.EstadoAsistencia
import cl.duoc.tiotec.model.SolicitudAsistencia
import cl.duoc.tiotec.ui.components.CampoTextoValidado
import cl.duoc.tiotec.viewmodel.TioTecUiState

@Composable
fun AsistenciaScreen(
    state: TioTecUiState,
    onCategoriaChange: (CategoriaSeguridad) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onEnviarSolicitud: () -> Unit,
    onToggleEstado: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Solicitud de Asistencia Simulada",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Registra una consulta preventiva para recibir orientación técnica guiada (sin datos sensibles).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Formulario
        item {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Nueva Solicitud",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "1. Selecciona la categoría:",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoriaSeguridad.entries.take(2).forEach { cat ->
                            FilterChip(
                                selected = state.categoriaAsistenciaSeleccionada == cat,
                                onClick = { onCategoriaChange(cat) },
                                label = { Text(cat.titulo, maxLines = 1) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoriaSeguridad.entries.drop(2).forEach { cat ->
                            FilterChip(
                                selected = state.categoriaAsistenciaSeleccionada == cat,
                                onClick = { onCategoriaChange(cat) },
                                label = { Text(cat.titulo, maxLines = 1) }
                            )
                        }
                    }

                    CampoTextoValidado(
                        valor = state.descripcionAsistenciaInput,
                        onValorChange = onDescripcionChange,
                        etiqueta = "Descripción (mín. 10 caracteres)",
                        icono = Icons.Default.Info,
                        error = state.errorDescripcionAsistencia
                    )

                    Button(
                        onClick = onEnviarSolicitud,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.descripcionAsistenciaInput.trim().length >= 10,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Enviar Solicitud")
                    }
                }
            }
        }

        item {
            Text(
                text = "Solicitudes Registradas (${state.solicitudesAsistencia.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (state.solicitudesAsistencia.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No hay solicitudes registradas.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(state.solicitudesAsistencia, key = { it.id }) { solicitud ->
                SolicitudAsistenciaCard(
                    solicitud = solicitud,
                    onToggle = { onToggleEstado(solicitud.id) }
                )
            }
        }
    }
}

@Composable
private fun SolicitudAsistenciaCard(
    solicitud: SolicitudAsistencia,
    onToggle: () -> Unit
) {
    val esAtendida = solicitud.estado == EstadoAsistencia.ATENDIDA
    val chipColor = if (esAtendida) Color(0xFF2E7D32) else Color(0xFFE65100)

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = solicitud.categoria.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                SuggestionChip(
                    onClick = onToggle,
                    label = { Text(solicitud.estado.etiqueta, fontWeight = FontWeight.Bold) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        labelColor = chipColor
                    ),
                    icon = {
                        Icon(
                            imageVector = if (esAtendida) Icons.Default.Check else Icons.Default.Refresh,
                            contentDescription = null,
                            tint = chipColor
                        )
                    }
                )
            }

            Text(
                text = solicitud.descripcion,
                style = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fecha: ${solicitud.fecha}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = onToggle,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (esAtendida) "Marcar Pendiente" else "Marcar Atendida",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
