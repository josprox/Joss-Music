package com.zionhuang.music

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class MusicWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // Inicializa con texto predeterminado
            views.setTextViewText(R.id.txt_station_title, "Sin reproducción")
            views.setTextViewText(R.id.txt_station_subtitle, "---")

            // Actualiza el widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            "UPDATE_WIDGET" -> {
                // Obtén los datos de la canción del intent
                val songTitle = intent.getStringExtra("SONG_TITLE") ?: "Sin título"
                val artistName = intent.getStringExtra("ARTIST_NAME") ?: "Artista desconocido"

                // Actualiza el widget con los nuevos datos
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, MusicWidgetProvider::class.java)
                )

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_layout)
                    views.setTextViewText(R.id.txt_station_title, songTitle)
                    views.setTextViewText(R.id.txt_station_subtitle, artistName)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}
