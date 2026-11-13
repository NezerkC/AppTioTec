package cl.duoc.tiotec.ui.cuidado

import android.content.Context
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
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.duoc.tiotec.model.AlmacenamientoInfo
import cl.duoc.tiotec.model.AppUsoInfo
import cl.duoc.tiotec.model.BateriaInfo
import cl.duoc.tiotec.model.DiagnosticoHardwareRegistro
import cl.duoc.tiotec.model.DiagnosticoSistema
import cl.duoc.tiotec.model.InventarioSensores
import cl.duoc.tiotec.model.MetricasBienestarDigital
import cl.duoc.tiotec.model.NivelRiesgo
import cl.duoc.tiotec.model.RamInfo
import cl.duoc.tiotec.model.SensorHardwareInfo
import cl.duoc.tiotec.ui.components.colorFondo
import cl.duoc.tiotec.ui.components.colorTexto
import cl.duoc.tiotec.viewmodel.TioTecUiState
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Pantalla principal de Cuidado Tecnológico (Unidad 3 / Entrega Final).
 * Enfocada en hardware estable (Batería, RAM, Almacenamiento, Sensores),
 * persistencia local en Room (SQLite) y sincronización con Spring Boot vía Retrofit.
 */
@Composable
fun CuidadoTechScreen(
    state: TioTecUiState,
    onEscanear: (Context) -> Unit,
    onGenerarAccionesChecklist: () -> Unit,
    modifier: Modifier = Modifier,
    onRefrescarEnVivo: (Context) -> Unit = onEscanear,
    onAlternarEnVivo: () -> Unit = {},
    onGuardarEnRoom: () -> Unit = {},
    onSincronizarConBackend: (String?) -> Unit = {},
    onConsultarRemoto: () -> Unit = {},
    onUrlServidorChange: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var pestanaSeleccionada by remember { mutableIntStateOf(0) }
    val pestanas = remember {
        listOf(
            "Hardware Estable",
            "Persistencia Room",
            "Spring Boot API",
            "Bienestar Digital"
        )
    }

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
            HeaderHardwareEstable(
                diagnostico = state.diagnosticoSistema,
                onEscanear = { onEscanear(context) },
                onGenerarAcciones = onGenerarAccionesChecklist
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
                // Tab 0: Hardware Estable (Batería, RAM, Almacenamiento, Sensores)
                val diag = state.diagnosticoSistema
                item { TarjetaBateriaEstable(bateria = diag.bateria) }
                item { TarjetaRamEstable(ram = diag.ram) }
                item { TarjetaAlmacenamientoEstable(almacenamiento = diag.almacenamiento) }
                item { TarjetaSensoresEstable(sensores = diag.sensores) }
            }
            1 -> {
                // Tab 1: Persistencia en Room (SQLite)
                item {
                    TarjetaAccionGuardarRoom(
                        diagnostico = state.diagnosticoSistema,
                        totalGuardados = state.historialDiagnosticos.size,
                        onGuardar = onGuardarEnRoom
                    )
                }
                item {
                    Text(
                        text = "Historial en Base de Datos Local Room (${state.historialDiagnosticos.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                if (state.historialDiagnosticos.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = "Aún no se han guardado diagnósticos en Room. Presiona 'Guardar Diagnóstico en Room' para persistir el estado actual.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(state.historialDiagnosticos) { registro ->
                        ItemHistorialDiagnostico(
                            registro = registro,
                            onSincronizar = { onSincronizarConBackend(registro.id) }
                        )
                    }
                }
            }
            2 -> {
                // Tab 2: Spring Boot + Retrofit
                item {
                    TarjetaSpringBootRetrofit(
                        urlServidor = state.urlServidorSpring,
                        onUrlChange = onUrlServidorChange,
                        sincronizando = state.sincronizandoApi,
                        onSubirActual = { onSincronizarConBackend(null) },
                        onConsultar = onConsultarRemoto
                    )
                }
            }
            3 -> {
                // Tab 3: Bienestar Digital
                val bienestar = state.metricasBienestar
                item { TarjetaResumenBienestar(bienestar = bienestar) }
                if (bienestar.appsMasUsadas.isNotEmpty()) {
                    item {
                        Text(
                            text = "Aplicaciones con mayor tiempo de pantalla",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(bienestar.appsMasUsadas) { app ->
                        ItemAppUso(app = app)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderHardwareEstable(
    diagnostico: DiagnosticoSistema,
    onEscanear: () -> Unit,
    onGenerarAcciones: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Diagnóstico Preventivo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Actualizado: ${diagnostico.fechaFormateada}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Surface(
                    color = diagnostico.nivelRiesgo.colorFondo,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = diagnostico.nivelRiesgo.etiqueta,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = diagnostico.nivelRiesgo.colorTexto
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEscanear,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Escanear")
                }

                OutlinedButton(
                    onClick = onGenerarAcciones,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Checklist")
                }
            }
        }
    }
}

@Composable
private fun TarjetaBateriaEstable(bateria: BateriaInfo) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Batería y Energía",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            LinearProgressIndicator(
                progress = { bateria.nivelPorcentaje / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (bateria.nivelPorcentaje <= 20) Color(0xFFD32F2F) else Color(0xFF388E3C)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Nivel: ${bateria.nivelPorcentaje}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (bateria.estaCargando) "Cargando (${bateria.fuenteEnergia})" else "En descarga",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (bateria.estaCargando) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Temperatura", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${bateria.temperaturaCelsius} °C",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (bateria.temperaturaAlerta) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface
                    )
                }
                Column {
                    Text(text = "Voltaje", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${bateria.voltajeMv} mV (${bateria.voltajeVoltios} V)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(text = "Fuente", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = bateria.fuenteEnergia,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaRamEstable(ram: RamInfo) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Memoria RAM",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            LinearProgressIndicator(
                progress = { ram.porcentajeUsado / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (ram.ramAlerta) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Uso: ${ram.porcentajeUsado}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${ram.usadaMb} MB usados / ${ram.totalMb} MB total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "RAM disponible para apps: ${ram.disponibleMb} MB",
                style = MaterialTheme.typography.bodySmall,
                color = if (ram.ramAlerta) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TarjetaAlmacenamientoEstable(almacenamiento: AlmacenamientoInfo) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Almacenamiento Interno",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            LinearProgressIndicator(
                progress = { almacenamiento.porcentajeUsado / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (almacenamiento.almacenamientoAlerta) Color(0xFFD32F2F) else MaterialTheme.colorScheme.secondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ocupado: ${almacenamiento.porcentajeUsado}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${almacenamiento.usadoGb} GB usados / ${almacenamiento.totalGb} GB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Espacio libre para el usuario: ${almacenamiento.libreGb} GB",
                style = MaterialTheme.typography.bodySmall,
                color = if (almacenamiento.almacenamientoAlerta) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TarjetaSensoresEstable(sensores: InventarioSensores) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sensores Físicos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${sensores.cantidadSensores} sensores · ${sensores.consumoTotalSensoresMa} mA",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Text(
                text = "Listado de componentes de hardware y consumo teórico declarado por el fabricante en miliamperios (mA):",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            sensores.sensores.take(8).forEach { sensor ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sensor.nombre,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${sensor.tipo} · ${sensor.fabricante}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "${String.format(Locale.US, "%.2f", sensor.consumoMa)} mA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (sensores.sensores.size > 8) {
                Text(
                    text = "... y ${sensores.sensores.size - 8} sensores adicionales disponibles en el dispositivo.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TarjetaAccionGuardarRoom(
    diagnostico: DiagnosticoSistema,
    totalGuardados: Int,
    onGuardar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Persistencia Local ACID (Room SQLite)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "Permite registrar una instantánea de batería (${diagnostico.bateria.nivelPorcentaje}%), RAM (${diagnostico.ram.porcentajeUsado}%), almacenamiento (${diagnostico.almacenamiento.porcentajeUsado}%) y sensores en la base de datos tiotec_database.db.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Button(
                onClick = onGuardar,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Diagnóstico en Room (SQLite)")
            }
        }
    }
}

@Composable
private fun ItemHistorialDiagnostico(
    registro: DiagnosticoHardwareRegistro,
    onSincronizar: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = registro.fechaRegistro,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${registro.id}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = if (registro.sincronizadoRemoto) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (registro.sincronizadoRemoto) Icons.Default.Check else Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (registro.sincronizadoRemoto) Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (registro.sincronizadoRemoto) "En Spring Boot" else "Pendiente Local",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (registro.sincronizadoRemoto) Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                    }
                }
            }

            Text(
                text = "Batería: ${registro.nivelBateria}% (${registro.temperaturaBateria} °C) · RAM: ${registro.ramTotalMb - registro.ramDisponibleMb} MB / ${registro.ramTotalMb} MB · Sensores: ${registro.totalSensores}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!registro.sincronizadoRemoto) {
                OutlinedButton(
                    onClick = onSincronizar,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Subir a Spring Boot", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun TarjetaSpringBootRetrofit(
    urlServidor: String,
    onUrlChange: (String) -> Unit,
    sincronizando: Boolean,
    onSubirActual: () -> Unit,
    onConsultar: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Integración REST Spring Boot + Retrofit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Conecta la app Android con los endpoints /api/v1/diagnosticos de Spring Boot usando Retrofit y OkHttp.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = urlServidor,
                onValueChange = onUrlChange,
                label = { Text("URL Base Servidor Spring Boot") },
                placeholder = { Text("http://10.0.2.2:8080/") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Nota: En emulador Android oficial, 10.0.2.2 representa el localhost de tu PC.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            if (sincronizando) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Comunicando con Spring Boot...", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSubirActual,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Subir Actual")
                    }

                    OutlinedButton(
                        onClick = onConsultar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Consultar")
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaResumenBienestar(bienestar: MetricasBienestarDigital) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Bienestar Digital",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tiempo total de pantalla hoy: ${bienestar.horasTotales} horas (${bienestar.minutosRestantes} minutos)",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Estado: ${bienestar.recomendacion}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ItemAppUso(app: AppUsoInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = app.nombreApp, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(text = app.paquete, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = app.tiempoFormateado, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}
