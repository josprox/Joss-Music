package com.zionhuang.music

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.zionhuang.music.R

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
            val playPausePendingIntent = PendingIntent.getBroadcast(context, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.playPauseButton, playPausePendingIntent)

            // Configura el botón de siguiente
            val nextIntent = Intent(context, MusicWidgetProvider::class.java).apply {
                action = "NEXT"
            }
            val nextPendingIntent = PendingIntent.getBroadcast(context, 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.nextButton, nextPendingIntent)

            // Configura el botón de anterior
            val previousIntent = Intent(context, MusicWidgetProvider::class.java).apply {
                action = "PREVIOUS"
            }
            val previousPendingIntent = PendingIntent.getBroadcast(context, 2, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.previousButton, previousPendingIntent)

            // Configura los textos y la portada
            views.setTextViewText(R.id.songTitle, "Nombre de la canción")
            views.setTextViewText(R.id.txt_artist, "Artista")

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            "PLAY_PAUSE" -> {
                // Lógica para cambiar el estado de reproducción
            }
            "NEXT" -> {
                // Lógica para la siguiente canción
            }
            "PREVIOUS" -> {
                // Lógica para la canción anterior
            }
        }
    }
}