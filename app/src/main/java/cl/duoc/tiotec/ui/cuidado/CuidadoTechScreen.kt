package cl.duoc.tiotec.ui.cuidado

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.duoc.tiotec.model.AppUsoInfo
import cl.duoc.tiotec.model.CamaraHardwareInfo
import cl.duoc.tiotec.model.ConsumoPropiaApp
import cl.duoc.tiotec.model.DiagnosticoHardware
import cl.duoc.tiotec.model.EstadoGlobalHardware
import cl.duoc.tiotec.model.FlujoEnergeticoDispositivo
import cl.duoc.tiotec.model.MetricasBienestarDigital
import cl.duoc.tiotec.model.NivelRiesgo
import cl.duoc.tiotec.model.PantallaHardwareInfo
import cl.duoc.tiotec.model.SensorHardwareInfo
import cl.duoc.tiotec.model.TelemetriaSistema
import cl.duoc.tiotec.nativo.BienestarDigitalHelper
import cl.duoc.tiotec.ui.components.colorFondo
import cl.duoc.tiotec.ui.components.colorTexto
import cl.duoc.tiotec.viewmodel.TioTecUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

private data class SerieGrafica(
    val etiqueta: String,
    val valores: List<Float>,
    val color: Color
)

/**
 * Pantalla principal del módulo de Cuidado Tecnológico (Unidad 3).
 * Integra monitoreo en vivo de energía, estrés térmico, rendimiento, inventario de hardware y bienestar digital.
 */
@Composable
fun CuidadoTechScreen(
    state: TioTecUiState,
    onEscanear: (Context) -> Unit,
    onGenerarAccionesChecklist: () -> Unit,
    modifier: Modifier = Modifier,
    onRefrescarEnVivo: (Context) -> Unit = onEscanear,
    onAlternarEnVivo: () -> Unit = {}
) {
    val context = LocalContext.current
    var pestanaSeleccionada by remember { mutableIntStateOf(0) }
    val pestanas = remember {
        listOf(
            "Energía y Térmico",
            "Rendimiento",
            "Hardware y Sensores",
            "Mi App (Self-Profiling)",
            "Bienestar"
        )
    }

    LaunchedEffect(Unit) {
        onEscanear(context)
    }

    LaunchedEffect(state.monitoreoEnVivoActivo) {
        while (isActive && state.monitoreoEnVivoActivo) {
            onRefrescarEnVivo(context)
            delay(1500L)
        }
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
                bienestar = state.metricasBienestar,
                telemetria = state.telemetriaSistema
            )
        }

        item {
            BarraMonitorEnVivo(
                activo = state.monitoreoEnVivoActivo,
                totalMuestras = state.historialPotenciaWatts.size,
                onAlternar = onAlternarEnVivo
            )
        }

        item {
            ScrollableTabRow(
                selectedTabIndex = pestanaSeleccionada,
                edgePadding = 8.dp,
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                pestanas.forEachIndexed { indice, titulo ->
                    Tab(
                        selected = pestanaSeleccionada == indice,
                        onClick = { pestanaSeleccionada = indice },
                        text = {
                            Text(
                                text = titulo,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        }

        when (pestanaSeleccionada) {
            0 -> {
                item {
                    BannerEstresTermico(estadoGlobal = state.telemetriaSistema.estadoGlobal)
                }
                item {
                    TarjetaFlujoEnergetico(
                        flujo = state.telemetriaSistema.flujoEnergetico,
                        historialWatts = state.historialPotenciaWatts
                    )
                }
                item {
                    TarjetaBateria(hardware = state.diagnosticoHardware)
                }
            }
            1 -> {
                item {
                    TarjetaRendimientoRam(
                        estadoGlobal = state.telemetriaSistema.estadoGlobal,
                        historialRam = state.historialRamPorcentaje
                    )
                }
                item {
                    TarjetaRendimientoAlmacenamiento(
                        estadoGlobal = state.telemetriaSistema.estadoGlobal,
                        historialAlmacenamiento = state.historialAlmacenamientoPorcentaje
                    )
                }
                item {
                    TarjetaRendimientoRedGlobal(
                        estadoGlobal = state.telemetriaSistema.estadoGlobal,
                        historialBajada = state.historialRedBajadaKbps,
                        historialSubida = state.historialRedSubidaKbps
                    )
                }
                item {
                    TarjetaFichaSoC(estadoGlobal = state.telemetriaSistema.estadoGlobal)
                }
            }
            2 -> {
                val inventario = state.telemetriaSistema.inventarioHardware
                item {
                    TarjetaPantallasHardware(pantallas = inventario.pantallas)
                }
                item {
                    TarjetaCamarasHardware(camaras = inventario.camaras)
                }
                item {
                    TarjetaResumenSensores(
                        totalSensores = inventario.sensores.size,
                        consumoTotalMa = inventario.consumoTotalSensoresMa
                    )
                }
                if (inventario.sensores.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Text(
                                text = "No se reportaron sensores físicos disponibles.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(inventario.sensores) { sensor ->
                        FilaSensorHardware(sensor = sensor)
                    }
                }
            }
            3 -> {
                item {
                    TarjetaConsumoPropiaApp(
                        consumo = state.telemetriaSistema.consumoApp,
                        historialCpu = state.historialCpuAppPorcentaje
                    )
                }
                item {
                    TarjetaNotaSandboxAndroid(nota = state.telemetriaSistema.notaSandboxAndroid)
                }
            }
            else -> {
                item {
                    TarjetaBienestarDigital(
                        metricas = state.metricasBienestar,
                        context = context
                    )
                }
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
    bienestar: MetricasBienestarDigital,
    telemetria: TelemetriaSistema
) {
    val nivelGlobal = when {
        hardware.nivelRiesgo == NivelRiesgo.ALTO ||
            bienestar.nivelRiesgo == NivelRiesgo.ALTO ||
            telemetria.nivelRiesgo == NivelRiesgo.ALTO -> NivelRiesgo.ALTO
        hardware.nivelRiesgo == NivelRiesgo.MEDIO ||
            bienestar.nivelRiesgo == NivelRiesgo.MEDIO ||
            telemetria.nivelRiesgo == NivelRiesgo.MEDIO -> NivelRiesgo.MEDIO
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
                        text = "Telemetría en Vivo y Diagnóstico Preventivo",
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
private fun BarraMonitorEnVivo(
    activo: Boolean,
    totalMuestras: Int,
    onAlternar: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (activo) Color(0xFF2E7D32)
                            else MaterialTheme.colorScheme.outline
                        )
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Monitor en vivo (1.5s)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (activo) "Muestreo activo · $totalMuestras/24 puntos" else "En pausa · $totalMuestras/24 puntos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            FilterChip(
                selected = activo,
                onClick = onAlternar,
                label = {
                    Text(
                        text = if (activo) "Pausar" else "Reanudar",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
private fun BannerEstresTermico(estadoGlobal: EstadoGlobalHardware) {
    val hayAlerta = estadoGlobal.alertaThrottling || estadoGlobal.estadoTermicoCodigo >= 1
    val esCritico = estadoGlobal.alertaThrottling || estadoGlobal.estadoTermicoCodigo >= 2

    val fondo = when {
        esCritico -> MaterialTheme.colorScheme.errorContainer
        hayAlerta -> Color(0xFFFFF3E0)
        else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
    }
    val textoColor = when {
        esCritico -> MaterialTheme.colorScheme.onErrorContainer
        hayAlerta -> Color(0xFFE65100)
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = fondo)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (hayAlerta) Icons.Default.Warning else Icons.Default.Info,
                        contentDescription = null,
                        tint = textoColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Estado Térmico (PowerManager)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = textoColor
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = textoColor.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = "Código ${estadoGlobal.estadoTermicoCodigo}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = textoColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = estadoGlobal.estadoTermicoEtiqueta,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = textoColor
            )

            val headroomTexto = if (estadoGlobal.margenTermico < 0f) {
                "No disponible (< API 30)"
            } else {
                String.format(Locale.US, "%.0f%% (%.2f)", estadoGlobal.margenTermico * 100f, estadoGlobal.margenTermico)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Thermal Headroom: $headroomTexto",
                style = MaterialTheme.typography.bodySmall,
                color = textoColor.copy(alpha = 0.9f)
            )

            if (hayAlerta) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (esCritico) {
                        "Thermal throttling detectado: el sistema puede limitar CPU/GPU. Pausa tareas pesadas y retira la funda."
                    } else {
                        "Estrés térmico leve: vigila la temperatura si estás jugando o cargando."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = textoColor
                )
            }
        }
    }
}

@Composable
private fun TarjetaFlujoEnergetico(
    flujo: FlujoEnergeticoDispositivo,
    historialWatts: List<Float>
) {
    val colorPotencia = if (flujo.descargaAltaAlerta) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

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
                    text = "Flujo eléctrico en vivo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${flujo.potenciaWatts} W",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorPotencia
                )
            }

            Spacer(Modifier.height(10.dp))
            GraficaTelemetriaCanvas(
                series = listOf(
                    SerieGrafica(
                        etiqueta = "Potencia (W)",
                        valores = historialWatts,
                        color = colorPotencia
                    )
                ),
                unidad = "W",
                maxFijo = null
            )

            Spacer(Modifier.height(14.dp))
            FilaDato("Dirección de flujo:", flujo.direccionFlujo)
            FilaDato(
                "Corriente instantánea:",
                "${flujo.corrienteInstantaneaMa} mA (${flujo.corrienteInstantaneaUa} µA)",
                esAlerta = flujo.descargaAltaAlerta
            )
            FilaDato(
                "Corriente promedio:",
                "${flujo.corrientePromedioMa} mA (${flujo.corrientePromedioUa} µA)"
            )
            FilaDato("Potencia eléctrica:", "${flujo.potenciaWatts} W")
            FilaDato("Voltaje / Temp:", "${flujo.voltajeMv} mV · ${flujo.temperaturaCelsius} °C")
            FilaDato("Capacidad remanente:", "${flujo.capacidadRemanenteMah} mAh")

            if (flujo.descargaAltaAlerta) {
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
                        text = "Consumo de descarga elevado. Reduce el brillo o cierra apps exigentes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
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
                    text = "Batería (BatteryManager)",
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
private fun TarjetaRendimientoRam(
    estadoGlobal: EstadoGlobalHardware,
    historialRam: List<Float>
) {
    val colorRam = if (estadoGlobal.ramAlerta) MaterialTheme.colorScheme.error else Color(0xFF1565C0)
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
                    text = "Memoria RAM",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${estadoGlobal.porcentajeRamUsado}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorRam
                )
            }

            Spacer(Modifier.height(10.dp))
            GraficaTelemetriaCanvas(
                series = listOf(
                    SerieGrafica(
                        etiqueta = "Uso RAM (%)",
                        valores = historialRam,
                        color = colorRam
                    )
                ),
                unidad = "%",
                maxFijo = 100f
            )

            Spacer(Modifier.height(12.dp))
            FilaDato(
                "Ocupada / Total:",
                "${estadoGlobal.ramUsadaMb} MB / ${estadoGlobal.ramTotalMb} MB",
                esAlerta = estadoGlobal.ramAlerta
            )
            FilaDato("RAM libre:", "${estadoGlobal.ramDisponibleMb} MB")
            FilaDato("Umbral memoria baja:", "${estadoGlobal.umbralMemoriaBajaMb} MB")
        }
    }
}

@Composable
private fun TarjetaRendimientoAlmacenamiento(
    estadoGlobal: EstadoGlobalHardware,
    historialAlmacenamiento: List<Float>
) {
    val colorDisco = if (estadoGlobal.porcentajeAlmacenamientoUsado >= 85) {
        MaterialTheme.colorScheme.error
    } else {
        Color(0xFF2E7D32)
    }
    val usadoGb = ((estadoGlobal.almacenamientoTotalGb - estadoGlobal.almacenamientoLibreGb).coerceAtLeast(0.0) * 10.0)
        .roundToInt() / 10.0

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
                    text = "${estadoGlobal.porcentajeAlmacenamientoUsado}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorDisco
                )
            }

            Spacer(Modifier.height(10.dp))
            GraficaTelemetriaCanvas(
                series = listOf(
                    SerieGrafica(
                        etiqueta = "Uso Disco (%)",
                        valores = historialAlmacenamiento,
                        color = colorDisco
                    )
                ),
                unidad = "%",
                maxFijo = 100f
            )

            Spacer(Modifier.height(12.dp))
            FilaDato("Usado / Total:", "$usadoGb GB / ${estadoGlobal.almacenamientoTotalGb} GB")
            FilaDato("Espacio libre:", "${estadoGlobal.almacenamientoLibreGb} GB")
        }
    }
}

@Composable
private fun TarjetaRendimientoRedGlobal(
    estadoGlobal: EstadoGlobalHardware,
    historialBajada: List<Float>,
    historialSubida: List<Float>
) {
    val colorRx = Color(0xFF0277BD)
    val colorTx = Color(0xFFEF6C00)

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
                    text = "Tráfico de Red Global (TrafficStats)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(10.dp))
            GraficaTelemetriaCanvas(
                series = listOf(
                    SerieGrafica("Descarga (↓)", historialBajada, colorRx),
                    SerieGrafica("Subida (↑)", historialSubida, colorTx)
                ),
                unidad = "KB/s",
                maxFijo = null
            )

            Spacer(Modifier.height(12.dp))
            FilaDato("Velocidad descarga (↓):", estadoGlobal.redBajadaFormateada)
            FilaDato("Velocidad subida (↑):", estadoGlobal.redSubidaFormateada)
            FilaDato("Acumulado RX / TX:", "${estadoGlobal.redTotalRxMb} MB / ${estadoGlobal.redTotalTxMb} MB")
        }
    }
}

@Composable
private fun TarjetaFichaSoC(estadoGlobal: EstadoGlobalHardware) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Ficha Técnica de CPU y SoC",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            FilaDato("Modelo de SoC:", estadoGlobal.modeloSoC)
            FilaDato("Arquitectura ABI:", estadoGlobal.arquitecturaCpu)
            FilaDato("Núcleos activos:", "${estadoGlobal.nucleosCpu} núcleos")
            FilaDato("Dispositivo:", estadoGlobal.modeloDispositivo)
            FilaDato("Sistema Operativo:", "Android ${estadoGlobal.versionAndroid}")
            FilaDato(
                "Ahorro de energía:",
                if (estadoGlobal.modoAhorroEnergia) "Activado" else "Desactivado"
            )
        }
    }
}

@Composable
private fun TarjetaPantallasHardware(pantallas: List<PantallaHardwareInfo>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Pantallas y Tasa de Refresco (${pantallas.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            if (pantallas.isEmpty()) {
                Text(
                    text = "Sin pantallas reportadas por DisplayManager.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                pantallas.forEachIndexed { idx, pantalla ->
                    if (idx > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "${pantalla.nombre} (ID ${pantalla.id})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    FilaDato("Resolución:", pantalla.resolucion)
                    FilaDato("Refresco actual:", "${pantalla.tasaRefrescoHz.roundToInt()} Hz")
                    if (pantalla.tasasSoportadasHz.isNotEmpty()) {
                        val tasasTexto = pantalla.tasasSoportadasHz
                            .joinToString(", ") { "${it.roundToInt()} Hz" }
                        FilaDato("Modos soportados:", tasasTexto)
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaCamarasHardware(camaras: List<CamaraHardwareInfo>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Cámaras Físicas (${camaras.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            if (camaras.isEmpty()) {
                Text(
                    text = "Sin cámaras reportadas por CameraManager.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                camaras.forEachIndexed { idx, cam ->
                    if (idx > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    FilaDato("Cámara #${cam.id} (${cam.ubicacion}):", cam.nivelHardware)
                    FilaDato("Flash disponible:", if (cam.tieneFlash) "Sí" else "No")
                }
            }
        }
    }
}

@Composable
private fun TarjetaResumenSensores(
    totalSensores: Int,
    consumoTotalMa: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sensores Integrados ($totalSensores)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$consumoTotalMa mA",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Consumo teórico combinado si todos los sensores operaran simultáneamente: $consumoTotalMa mA.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilaSensorHardware(sensor: SensorHardwareInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sensor.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${String.format(Locale.US, "%.2f", sensor.consumoMa)} mA",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(4.dp))
            FilaDato("Fabricante:", sensor.fabricante)
            FilaDato("Tipo:", sensor.tipo)
            FilaDato(
                "Rango / Resolución:",
                "${String.format(Locale.US, "%.2f", sensor.rangoMaximo)} · ${String.format(Locale.US, "%.4f", sensor.resolucion)}"
            )
        }
    }
}

@Composable
private fun TarjetaConsumoPropiaApp(
    consumo: ConsumoPropiaApp,
    historialCpu: List<Float>
) {
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
                    text = "Perfilador en Vivo (AppTioTec)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${String.format(Locale.US, "%.1f", consumo.porcentajeCpuApp)}% CPU",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(10.dp))
            GraficaTelemetriaCanvas(
                series = listOf(
                    SerieGrafica(
                        etiqueta = "CPU Propia (%)",
                        valores = historialCpu,
                        color = MaterialTheme.colorScheme.primary
                    )
                ),
                unidad = "%",
                maxFijo = 100f
            )

            Spacer(Modifier.height(12.dp))
            val progresoHeap by animateFloatAsState(
                targetValue = (consumo.porcentajeHeapUsado / 100f).coerceIn(0f, 1f),
                label = "progresoHeap"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Heap JVM",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${consumo.porcentajeHeapUsado}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progresoHeap },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(Modifier.height(12.dp))
            FilaDato("Proceso:", "PID ${consumo.pidProceso} · UID ${consumo.uidAplicacion}")
            FilaDato("Potencia estimada:", "${consumo.potenciaEstimadaMw} mW")
            FilaDato("Consumo en sesión:", "${consumo.consumoBateriaSesionMah} mAh")
            FilaDato(
                "Red propia (↓ / ↑):",
                "${String.format(Locale.US, "%.1f", consumo.redRxKbps)} / ${String.format(Locale.US, "%.1f", consumo.redTxKbps)} KB/s"
            )
            FilaDato("Red total sesión app:", "${consumo.redTotalAppKb} KB")
            FilaDato("Memoria PSS total:", "${consumo.memoriaPssTotalMb} MB")
            FilaDato(
                "Heap JVM usado / máx:",
                "${consumo.heapUsadoMb} MB / ${consumo.heapMaximoMb} MB"
            )
            FilaDato("Tiempo de CPU / Hilos:", "${consumo.tiempoCpuMs} ms · ${consumo.hilosActivos} hilos")
        }
    }
}

@Composable
private fun GraficaTelemetriaCanvas(
    series: List<SerieGrafica>,
    unidad: String,
    maxFijo: Float? = null,
    modifier: Modifier = Modifier
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    val bgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    val maxCalculado = remember(series, maxFijo) {
        if (maxFijo != null) {
            max(1f, maxFijo)
        } else {
            val pico = series.flatMap { it.valores }.maxOrNull() ?: 0f
            max(1f, pico * 1.15f)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                series.forEach { serie ->
                    val ultimo = serie.valores.lastOrNull()
                    val textoValor = if (ultimo != null) {
                        "${serie.etiqueta}: ${String.format(Locale.US, "%.1f", ultimo)} $unidad"
                    } else {
                        "${serie.etiqueta}: -- $unidad"
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(serie.color)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = textoValor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Text(
                text = "Máx ${String.format(Locale.US, "%.0f", maxCalculado)} $unidad",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor)
                .border(1.dp, gridColor, RoundedCornerShape(10.dp))
        ) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas

            // Grilla estilo Monitor de Rendimiento
            for (i in 1..3) {
                val y = h * (i / 4f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            for (i in 1..5) {
                val x = w * (i / 6f)
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val paddingV = 6.dp.toPx()
            val usableH = (h - paddingV * 2f).coerceAtLeast(1f)

            series.forEach { serie ->
                val valores = serie.valores
                if (valores.isEmpty()) return@forEach

                val puntos: List<Offset> = if (valores.size == 1) {
                    val norm = (valores[0] / maxCalculado).coerceIn(0f, 1f)
                    val y = paddingV + usableH * (1f - norm)
                    listOf(Offset(0f, y), Offset(w, y))
                } else {
                    val pasoX = w / (valores.size - 1).toFloat()
                    valores.mapIndexed { idx, v ->
                        val norm = (v / maxCalculado).coerceIn(0f, 1f)
                        val x = (idx * pasoX).coerceIn(0f, w)
                        val y = paddingV + usableH * (1f - norm)
                        Offset(x, y)
                    }
                }

                val linePath = Path().apply {
                    moveTo(puntos.first().x, puntos.first().y)
                    for (i in 1 until puntos.size) {
                        lineTo(puntos[i].x, puntos[i].y)
                    }
                }

                val fillPath = Path().apply {
                    addPath(linePath)
                    lineTo(puntos.last().x, h)
                    lineTo(puntos.first().x, h)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            serie.color.copy(alpha = 0.28f),
                            serie.color.copy(alpha = 0.02f)
                        )
                    )
                )

                drawPath(
                    path = linePath,
                    color = serie.color,
                    style = Stroke(
                        width = 2.2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                drawCircle(
                    color = serie.color,
                    radius = 3.5.dp.toPx(),
                    center = puntos.last()
                )
            }
        }
    }
}

@Composable
private fun TarjetaNotaSandboxAndroid(nota: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "Privacidad y Sandbox de Android",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = nota,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.45f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = valor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = if (esAlerta) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}
