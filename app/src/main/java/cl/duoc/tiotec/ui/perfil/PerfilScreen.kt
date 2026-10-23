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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.tiotec.model.Dispositivo
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
    modifier: Modifier = Modifier
) {
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
                onRegistrarDispositivo = onRegistrarDispositivo
            )
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
            ItemDispositivo(dispositivo)
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
    onRegistrarDispositivo: () -> Unit
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
                text = "Registrar dispositivo de prueba",
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
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text("Registrar dispositivo")
            }
        }
    }
}

@Composable
private fun ItemDispositivo(dispositivo: Dispositivo) {
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
            Column(modifier = Modifier.padding(start = 12.dp)) {
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
