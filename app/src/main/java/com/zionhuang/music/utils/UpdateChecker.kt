package com.zionhuang.music.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.Manifest
import com.zionhuang.music.R
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import androidx.core.net.toUri
import com.zionhuang.music.BuildConfig
import org.dotenv.vault.dotenvVault

class UpdateChecker(private val context: Context) {

    private val client = OkHttpClient()
    private val channelId = "update_channel"
    private val notificationId = 1

    // URL de tu API
    val dotenv = dotenvVault(BuildConfig.DOTENV_KEY) {
        directory = "/assets"
        filename = "env.vault" // instead of '.env', use 'env'
    }
    private val versionUrl = dotenv["UPDATER_URL"]

    init {
        createNotificationChannel()
    }

    fun checkForUpdates() {
        val request = Request.Builder()
            .url(versionUrl)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    json?.let {
                        val versionInfo = parseVersionInfo(it)
                        val currentVersion = try {
                            context.packageManager.getPackageInfo(
                                context.packageName,
                                0
                            ).versionName
                        } catch (e: PackageManager.NameNotFoundException) {
                            e.printStackTrace()
                            return
                        }

                        if (versionInfo.Version > currentVersion.toString()) {
                            showUpdateNotification(versionInfo)
                        }
                    }
                }
            }
        })
    }

    private fun parseVersionInfo(json: String): VersionInfo {
        val jsonObject = JSONObject(json)
        return VersionInfo(
            Autor = jsonObject.getString("Autor"),
            Aplicacion = jsonObject.getString("Aplicacion"),
            Version = jsonObject.getString("Version"),
            Titulo = jsonObject.getString("Titulo"),
            Descripcion = jsonObject.getString("Descripcion"),
            Descarga = jsonObject.getString("Descarga"),
            Consultado = jsonObject.getString("Consultado")
        )
    }

    private fun showUpdateNotification(versionInfo: VersionInfo) {
        // Verificar permiso SOLO para Android 13+ (Tiramisu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return // No mostrar si no hay permiso (Android 13+)
            }
        }

        // Intent para abrir el navegador
        val intent = Intent(Intent.ACTION_VIEW, versionInfo.Descarga.toUri())
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construir notificación (compatible con todas las versiones)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.joss_music_logo)
            .setContentTitle("Actualización disponible")
            .setContentText("Pulsa para descargar v${versionInfo.Version}")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Mostrar notificación
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    private fun createNotificationChannel() {
        // Solo necesario para Android 8.0+ (Oreo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Actualizaciones"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = "Notificaciones de nuevas versiones"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    data class VersionInfo(
        val Autor: String,
        val Aplicacion: String,
        val Version: String,
        val Titulo: String,
        val Descripcion: String,
        val Descarga: String,
        val Consultado: String
    )
}