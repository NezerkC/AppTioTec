package cl.duoc.tiotec.nativo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cl.duoc.tiotec.MainActivity
import cl.duoc.tiotec.model.DiagnosticoHardware
import cl.duoc.tiotec.model.MetricasBienestarDigital
import cl.duoc.tiotec.model.NivelRiesgo

/**
 * Orquestador de chequeos de mantenimiento y optimización técnica preventiva.
 * Envía notificaciones de alerta cuando se superan umbrales críticos de temperatura,
 * almacenamiento o tiempo en pantalla.
 */
object ChequeoMantenimientoHelper {

    private const val CANAL_MANTENIMIENTO_ID = "mantenimiento_preventivo"
    private const val NOTIF_MANTENIMIENTO_ID = 2002

    private fun crearCanalMantenimiento(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_MANTENIMIENTO_ID,
                "Mantenimiento y Cuidado Tech",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de temperatura, batería, almacenamiento y bienestar digital"
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }

    /**
     * Evalúa las condiciones actuales y emite una notificación de advertencia preventiva si existe riesgo.
     */
    fun evaluarYNotificar(
        context: Context,
        hardware: DiagnosticoHardware,
        bienestar: MetricasBienestarDigital
    ) {
        if (!NotificacionesHelper.tienePermiso(context)) return

        val mensajeAlerta: String? = when {
            hardware.temperaturaBateria >= 42.0f ->
                "⚠️ Temperatura alta: La batería alcanzó ${hardware.temperaturaBateria}°C. Te sugerimos desconectar el cargador y pausar juegos o tareas pesadas."
            hardware.porcentajeAlmacenamientoUsado >= 88 ->
                "💾 Almacenamiento casi lleno: Te queda solo ${hardware.almacenamientoLibreGb} GB libres (${hardware.porcentajeAlmacenamientoUsado}% ocupado). Limpia archivos temporales para evitar lentitud."
            bienestar.tiempoTotalPantallaMinutos >= 300 ->
                "🧘 Pausa recomendada: Llevas ${bienestar.horasTotales}h ${bienestar.minutosRestantes}m de uso de pantalla hoy. Tómate 10 minutos para descansar la vista."
            else -> null
        }

        if (mensajeAlerta != null) {
            crearCanalMantenimiento(context)
            val abrirApp = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_IMMUTABLE
            )

            val notificacion = NotificationCompat.Builder(context, CANAL_MANTENIMIENTO_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("TíoTec · Alerta de Cuidado Tech")
                .setContentText(mensajeAlerta)
                .setStyle(NotificationCompat.BigTextStyle().bigText(mensajeAlerta))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(abrirApp)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(NOTIF_MANTENIMIENTO_ID, notificacion)
        }
    }
}
