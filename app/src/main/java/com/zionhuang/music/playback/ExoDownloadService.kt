package com.zionhuang.music.playback

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Scheduler
import com.zionhuang.music.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// Esta clase representa un servicio de descarga para contenido multimedia que muestra
// notificaciones para indicar el progreso o el estado de la descarga.
@AndroidEntryPoint
class ExoDownloadService : DownloadService(
    NOTIFICATION_ID,      // ID de la notificación utilizada para mostrar el progreso.
    1000L,                // Intervalo en milisegundos para actualizar el progreso.
    CHANNEL_ID,           // ID del canal de notificación.
    R.string.download,    // ID del recurso de string para el texto de la notificación.
    0                     // Valor de prioridad de la notificación.
) {
    // Utiliza inyección de dependencias para obtener una instancia de DownloadUtil.
    @Inject
    lateinit var downloadUtil: DownloadUtil

    // Maneja el comando de inicio del servicio, revisando si el comando contiene una acción específica.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Si la acción del intent es eliminar todas las descargas pendientes...
        if (intent?.action == REMOVE_ALL_PENDING_DOWNLOADS) {
            // Recorre todas las descargas actuales y las elimina.
            downloadManager.currentDownloads.forEach { download ->
                downloadManager.removeDownload(download.request.id)
            }
        }
        // Llama a la implementación del padre para manejar otros aspectos del servicio.
        return super.onStartCommand(intent, flags, startId)
    }

    // Obtiene el administrador de descargas desde DownloadUtil.
    override fun getDownloadManager() = downloadUtil.downloadManager

    // Obtiene un programador de tareas (Scheduler) para manejar las descargas en segundo plano.
    override fun getScheduler(): Scheduler = PlatformScheduler(this, JOB_ID)

    // Configura y devuelve una notificación de progreso que se mostrará en primer plano.
    override fun getForegroundNotification(downloads: MutableList<Download>, notMetRequirements: Int): Notification =
        Notification.Builder.recoverBuilder(
            this,
            // Usa DownloadNotificationHelper para crear una notificación de progreso.
            downloadUtil.downloadNotificationHelper.buildProgressNotification(
                this,
                R.drawable.joss_music_logo,        // Icono para la notificación de descarga.
                null,                       // PendingIntent nulo en este caso.
                // Muestra el nombre del archivo si hay una descarga, o el número de descargas.
                if (downloads.size == 1) Util.fromUtf8Bytes(downloads[0].request.data)
                else resources.getQuantityString(R.plurals.n_song, downloads.size, downloads.size),
                downloads,                  // Lista de descargas en progreso.
                notMetRequirements          // Requisitos no cumplidos.
            )
        )
            // Añade una acción para cancelar todas las descargas pendientes.
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.close),  // Icono para la acción.
                    getString(android.R.string.cancel),               // Texto para cancelar.
                    PendingIntent.getService(                         // Configura el intent de cancelación.
                        this,
                        0,
                        Intent(this, ExoDownloadService::class.java).setAction(REMOVE_ALL_PENDING_DOWNLOADS),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                ).build()
            ).build()

    /**
     * Helper que se encargará de manejar notificaciones para descargas fallidas.
     * Este helper vivirá más allá de la instancia de ExoDownloadService.
     */
    class TerminalStateNotificationHelper(
        private val context: Context,
        private val notificationHelper: DownloadNotificationHelper,
        private var nextNotificationId: Int,
    ) : DownloadManager.Listener {
        // Callback cuando cambia el estado de la descarga.
        override fun onDownloadChanged(
            downloadManager: DownloadManager,
            download: Download,
            finalException: Exception?,
        ) {
            // Si la descarga ha fallado, crea y muestra una notificación de error.
            if (download.state == Download.STATE_FAILED) {
                val notification = notificationHelper.buildDownloadFailedNotification(
                    context,
                    R.drawable.error,                      // Icono de error.
                    null,                                  // PendingIntent nulo en este caso.
                    Util.fromUtf8Bytes(download.request.data)  // Nombre de la descarga fallida.
                )
                // Muestra la notificación de error.
                NotificationUtil.setNotification(context, nextNotificationId++, notification)
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "download"                   // ID del canal de notificación.
        const val NOTIFICATION_ID = 1                       // ID de la notificación de progreso.
        const val JOB_ID = 1                                // ID del trabajo programado.
        const val REMOVE_ALL_PENDING_DOWNLOADS = "REMOVE_ALL_PENDING_DOWNLOADS"  // Acción para cancelar descargas.
    }
}
