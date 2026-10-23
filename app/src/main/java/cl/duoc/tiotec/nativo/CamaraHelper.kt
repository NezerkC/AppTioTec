package cl.duoc.tiotec.nativo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

/**
 * Recurso nativo 1: CÁMARA (#9).
 *
 * Flujo seguro:
 *  1. Al tocar el botón se revisa si ya hay permiso de cámara.
 *  2. Si no lo hay, se pide en tiempo de ejecución.
 *  3. Con permiso, se abre la cámara del teléfono y se recibe una foto pequeña (Bitmap).
 *  4. La foto se guarda en la carpeta privada de la app y se entrega su ruta.
 */
object CamaraHelper {

    /** Guarda la foto como JPG en el almacenamiento interno de la app y devuelve la ruta. */
    fun guardarFoto(context: Context, bitmap: Bitmap): String {
        val carpeta = File(context.filesDir, "fotos_dispositivos").apply { mkdirs() }
        val archivo = File(carpeta, "disp_${System.currentTimeMillis()}.jpg")
        FileOutputStream(archivo).use { salida ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, salida)
        }
        return archivo.absolutePath
    }

    fun tienePermiso(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
}

/**
 * Botón "Tomar foto" que maneja el permiso y la cámara.
 * Cuando hay foto, entrega la ruta del archivo con [onFotoTomada].
 */
@Composable
fun BotonTomarFoto(
    onFotoTomada: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var permisoDenegado by remember { mutableStateOf(false) }

    // Abre la app de cámara y devuelve una miniatura (Bitmap) o null si se canceló
    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            onFotoTomada(CamaraHelper.guardarFoto(context, bitmap))
        }
    }

    // Pide el permiso de cámara; si lo aceptan, abre la cámara
    val lanzadorPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoDenegado = !concedido
        if (concedido) lanzadorCamara.launch(null)
    }

    Column(modifier = modifier) {
        OutlinedButton(onClick = {
            if (CamaraHelper.tienePermiso(context)) {
                lanzadorCamara.launch(null)
            } else {
                lanzadorPermiso.launch(Manifest.permission.CAMERA)
            }
        }) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text("Foto del dispositivo (opcional)")
        }
        if (permisoDenegado) {
            Text(
                text = "Sin permiso de cámara no se puede tomar la foto. Puedes activarlo en Ajustes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/** Muestra la foto guardada en [ruta] como miniatura cuadrada. */
@Composable
fun FotoMiniatura(ruta: String, tamano: Dp = 56.dp, modifier: Modifier = Modifier) {
    // Se decodifica una sola vez por ruta (remember) para no leer el archivo en cada redibujo
    val imagen = remember(ruta) { BitmapFactory.decodeFile(ruta)?.asImageBitmap() }
    if (imagen != null) {
        Image(
            bitmap = imagen,
            contentDescription = "Foto del dispositivo",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(tamano)
                .clip(RoundedCornerShape(8.dp))
        )
    }
}

/** Fila con la miniatura recién tomada y un texto, usada dentro del formulario. */
@Composable
fun VistaPreviaFoto(ruta: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FotoMiniatura(ruta = ruta, tamano = 64.dp)
        Text(
            text = "Foto del dispositivo lista",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
