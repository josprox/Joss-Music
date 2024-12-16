package com.zionhuang.music

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.zionhuang.music.playback.MusicService
import timber.log.Timber

class MusicWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // Configura el botón de reproducir/pausar
            val playPauseIntent = Intent(context, MusicWidgetProvider::class.java).apply {
                action = "PLAY_PAUSE"
            }
            val playPausePendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                playPauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.playPauseButton, playPausePendingIntent)

            // Configura el botón de siguiente
            val nextIntent = Intent(context, MusicWidgetProvider::class.java).apply {
                action = "NEXT"
            }
            val nextPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.nextButton, nextPendingIntent)

            // Configura el botón de anterior
            val previousIntent = Intent(context, MusicWidgetProvider::class.java).apply {
                action = "PREVIOUS"
            }
            val previousPendingIntent = PendingIntent.getBroadcast(
                context,
                2,
                previousIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.previousButton, previousPendingIntent)

            // Aquí puedes agregar la lógica para actualizar el estado
            views.setTextViewText(R.id.songTitle, "Reproduciendo: Canción ejemplo")
            views.setTextViewText(R.id.txt_artist, "Artista: Artista ejemplo")

            // Actualiza el widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val musicServiceIntent = Intent(context, MusicService::class.java) // Intent para controlar el servicio

        when (intent.action) {
            "PLAY_PAUSE" -> {
                Timber.tag("MusicWidgetProvider").d("Play/Pause clicked")
                // Enviar una solicitud al MusicService para pausar o reproducir la música
                musicServiceIntent.action = MusicService.ACTION_TOGGLE_PLAY_PAUSE
                context.startService(musicServiceIntent)
            }
            "NEXT" -> {
                Timber.tag("MusicWidgetProvider").d("Next clicked")
                // Enviar una solicitud al MusicService para pasar a la siguiente canción
                musicServiceIntent.action = MusicService.ACTION_NEXT
                context.startService(musicServiceIntent)
            }
            "PREVIOUS" -> {
                Timber.tag("MusicWidgetProvider").d("Previous clicked")
                // Enviar una solicitud al MusicService para retroceder a la canción anterior
                musicServiceIntent.action = MusicService.ACTION_PREVIOUS
                context.startService(musicServiceIntent)
            }
        }
    }
}
