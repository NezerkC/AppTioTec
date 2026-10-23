package cl.duoc.tiotec.nativo

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.duoc.tiotec.MainActivity

/**
 * Recurso nativo 2: NOTIFICACIONES (#9).
 *
 *  - Android 8+ exige un "canal" de notificaciones (se crea una vez).
 *  - Android 13+ exige pedir el permiso POST_NOTIFICATIONS en tiempo de ejecución.
 *  - Al tocar la notificación se abre la app (PendingIntent a MainActivity).
 */
object NotificacionesHelper {

    private const val CANAL_ID = "recordatorios_preventivos"
    private const val NOTIFICACION_ID = 1001

    fun tienePermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                "Recordatorios preventivos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Avisos para revisar acciones urgentes del checklist" }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }

    fun mostrarRecordatorio(context: Context, accionesUrgentes: Int) {
        if (!tienePermiso(context)) return
        crearCanal(context)

        val abrirApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )

        val texto = if (accionesUrgentes > 0)
            "Tienes $accionesUrgentes acción(es) urgente(s) pendiente(s) en tu checklist."
        else
            "Recuerda revisar tu checklist y volver a evaluarte este mes."

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("TíoTec · Recordatorio preventivo")
            .setContentText(texto)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICACION_ID, notificacion)
    }
}

/** Botón que pide el permiso (si hace falta) y lanza el recordatorio. */
@Composable
fun BotonRecordatorio(
    accionesUrgentes: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var permisoDenegado by remember { mutableStateOf(false) }

    val lanzadorPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoDenegado = !concedido
        if (concedido) NotificacionesHelper.mostrarRecordatorio(context, accionesUrgentes)
    }

    Column(modifier = modifier) {
        FilledTonalButton(
            onClick = {
                if (NotificacionesHelper.tienePermiso(context)) {
                    NotificacionesHelper.mostrarRecordatorio(context, accionesUrgentes)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    lanzadorPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text("Enviarme un recordatorio")
        }
        if (permisoDenegado) {
            Text(
                text = "Sin permiso de notificaciones no podemos recordarte. Puedes activarlo en Ajustes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
