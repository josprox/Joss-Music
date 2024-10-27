package com.zionhuang.music

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_ENDED

class MusicWidgetProvider : AppWidgetProvider() {

    private var player: Player? = null

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

            // Configura los textos
            views.setTextViewText(R.id.songTitle, "Nombre de la canción")
            views.setTextViewText(R.id.txt_artist, "Artista")

            // Actualiza el widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            "PLAY_PAUSE" -> {
                // Alternar entre reproducir y pausar
                Log.d("MusicWidgetProvider", "Play/Pause clicked")
            }
            "NEXT" -> {
                // Lógica para la siguiente canción
                Log.d("MusicWidgetProvider", "Next clicked")
            }
            "PREVIOUS" -> {
                // Lógica para la canción anterior
                Log.d("MusicWidgetProvider", "Previous clicked")
            }
        }
    }
}
