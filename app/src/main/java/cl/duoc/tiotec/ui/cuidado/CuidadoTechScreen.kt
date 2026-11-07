package cl.duoc.tiotec.ui.cuidado

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.tiotec.model.AppUsoInfo
import cl.duoc.tiotec.model.DiagnosticoHardware
import cl.duoc.tiotec.model.MetricasBienestarDigital
import cl.duoc.tiotec.model.NivelRiesgo
import cl.duoc.tiotec.nativo.BienestarDigitalHelper
import cl.duoc.tiotec.ui.components.colorFondo
import cl.duoc.tiotec.ui.components.colorTexto
import cl.duoc.tiotec.viewmodel.TioTecUiState

/**
 * Pantalla principal del módulo de Cuidado Tecnológico (Unidad 3).
 * Integra monitoreo de hardware nativo (batería, almacenamiento) y bienestar digital (tiempo en pantalla).
 */
@Composable
fun CuidadoTechScreen(
    state: TioTecUiState,
    onEscanear: (Context) -> Unit,
    onGenerarAccionesChecklist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pestanaSeleccionada by remember { mutableIntStateOf(0) }

    // Al entrar a la pantalla, actualiza las métricas automáticamente
    LaunchedEffect(Unit) {
        onEscanear(context)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderCuidadoTech(
                hardware = state.diagnosticoHardware,
                bienestar = state.metricasBienestar
            )
        }

        item {
            TabRow(
                selectedTabIndex = pestanaSeleccionada,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                Tab(
                    selected = pestanaSeleccionada == 0,
                    onClick = { pestanaSeleccionada = 0 },
                    text = { Text("Hardware y Batería", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = pestanaSeleccionada == 1,
                    onClick = { pestanaSeleccionada = 1 },
                    text = { Text("Bienestar Digital", fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        if (pestanaSeleccionada == 0) {
            item {
                TarjetaBateria(hardware = state.diagnosticoHardware)
            }
            item {
                TarjetaAlmacenamiento(hardware = state.diagnosticoHardware)
            }
        } else {
            item {
                TarjetaBienestarDigital(
                    metricas = state.metricasBienestar,
                    context = context
                )
            }
        }

        item {
            TarjetaAccionesCuidado(
                onEscanear = { onEscanear(context) },
                onGenerarAcciones = onGenerarAccionesChecklist
            )
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeaderCuidadoTech(
    hardware: DiagnosticoHardware,
    bienestar: MetricasBienestarDigital
) {
    val nivelGlobal = when {
        hardware.nivelRiesgo == NivelRiesgo.ALTO || bienestar.nivelRiesgo == NivelRiesgo.ALTO -> NivelRiesgo.ALTO
        hardware.nivelRiesgo == NivelRiesgo.MEDIO || bienestar.nivelRiesgo == NivelRiesgo.MEDIO -> NivelRiesgo.MEDIO
        else -> NivelRiesgo.BAJO
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = nivelGlobal.colorFondo
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = nivelGlobal.colorTexto,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Centro de Cuidado Tecnológico",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = nivelGlobal.colorTexto
                    )
                    Text(
                        text = "Diagnóstico Preventivo de Hardware y Uso",
                        style = MaterialTheme.typography.bodySmall,
                        color = nivelGlobal.colorTexto.copy(alpha = 0.85f)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = hardware.recomendacion,
                style = MaterialTheme.typography.bodyMedium,
                color = nivelGlobal.colorTexto
            )
        }
    }
}

@Composable
private fun TarjetaBateria(hardware: DiagnosticoHardware) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Batería y Energía (BatteryManager)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${hardware.nivelBateria}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (hardware.nivelBateria <= 20) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(8.dp))
            val progresoBateria by animateFloatAsState(
                targetValue = (hardware.nivelBateria / 100f).coerceIn(0f, 1f),
                label = "progresoBateria"
            )
            LinearProgressIndicator(
                progress = { progresoBateria },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (hardware.nivelBateria <= 20) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(14.dp))
            FilaDato("Estado:", if (hardware.estaCargando) "Cargando (${hardware.fuenteEnergia})" else "En uso con batería")
            FilaDato("Temperatura:", "${hardware.temperaturaBateria} °C", esAlerta = hardware.temperaturaAlerta)
            FilaDato("Salud de la Batería:", hardware.saludBateria)
            FilaDato("Voltaje:", "${hardware.voltajeBateria} mV")

            if (hardware.temperaturaAlerta) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Temperatura elevada. Evita usar el teléfono mientras carga.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaAlmacenamiento(hardware: DiagnosticoHardware) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Almacenamiento (StatFs)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${hardware.porcentajeAlmacenamientoUsado}% Usado",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (hardware.almacenamientoAlerta) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(Modifier.height(8.dp))
            val progresoEspacio by animateFloatAsState(
                targetValue = (hardware.porcentajeAlmacenamientoUsado / 100f).coerceIn(0f, 1f),
                label = "progresoEspacio"
            )
            LinearProgressIndicator(
                progress = { progresoEspacio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (hardware.almacenamientoAlerta) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
            )

            Spacer(Modifier.height(14.dp))
            FilaDato("Espacio ocupado:", "${hardware.almacenamientoUsadoGb} GB de ${hardware.almacenamientoTotalGb} GB")
            FilaDato("Espacio libre disponible:", "${hardware.almacenamientoLibreGb} GB")
            FilaDato("Diagnóstico:", if (hardware.almacenamientoAlerta) "Espacio crítico (liberar memoria)" else "Capacidad adecuada")
        }
    }
}

@Composable
private fun TarjetaBienestarDigital(
    metricas: MetricasBienestarDigital,
    context: Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Bienestar Digital (UsageStatsManager)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            if (!metricas.permisoConcedido) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Para monitorear el tiempo en pantalla y detectar sobreuso, Android requiere el permiso especial de 'Acceso a datos de uso' (AppOpsManager).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FilledTonalButton(
                        onClick = {
                            context.startActivity(BienestarDigitalHelper.crearIntentAjustesUso())
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Conceder Acceso a Estadísticas")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tiempo total en pantalla hoy:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = metricas.tiempoTotalFormateado,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (metricas.alertaUsoExcesivo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = metricas.recomendacion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Aplicaciones con mayor tiempo de uso:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))

                metricas.appsMasUsadas.forEach { app ->
                    FilaAppUso(app = app, tiempoTotal = metricas.tiempoTotalPantallaMinutos)
                }
            }
        }
    }
}

@Composable
private fun FilaAppUso(app: AppUsoInfo, tiempoTotal: Long) {
    val porcentaje = if (tiempoTotal > 0) (app.tiempoMinutos.toFloat() / tiempoTotal).coerceIn(0f, 1f) else 0f
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = app.nombreApp,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = app.tiempoFormateado,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { porcentaje },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
        )
    }
}

@Composable
private fun TarjetaAccionesCuidado(
    onEscanear: () -> Unit,
    onGenerarAcciones: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Acciones Preventivas Recomendadas",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onEscanear,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Escanear Sensores de Hardware y Uso")
            }
            OutlinedButton(
                onClick = onGenerarAcciones,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Agregar Tareas de Cuidado al Checklist")
            }
        }
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String, esAlerta: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = if (esAlerta) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}
