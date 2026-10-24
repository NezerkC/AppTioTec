package cl.duoc.tiotec.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import cl.duoc.tiotec.nativo.BotonTomarFoto

/**
 * Componente UI para integración de cámara declarativa en Compose (Guía 13).
 */
@Composable
fun CamaraLauncherBoton(
    onFotoTomada: (String) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { }
    BotonTomarFoto(onFotoTomada = onFotoTomada)
}
