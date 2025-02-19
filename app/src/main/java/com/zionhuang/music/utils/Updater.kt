package com.zionhuang.music.utils

import com.zionhuang.music.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.dotenv.vault.dotenvVault
import org.json.JSONObject
object Updater {
    private val client = HttpClient()
    val dotenv = dotenvVault(BuildConfig.DOTENV_KEY) {
        directory = "/assets"
        filename = "env.vault" // instead of '.env', use 'env'
    }
    var lastCheckTime = -1L
        private set

    // Modelo de datos para los detalles de la versión
    data class ReleaseDetails(
        val version: String,
        val title: String,
        val description: String,
        val author: String,
        val downloadUrl: String
    )

    /**
     * Obtiene los detalles de la última versión como un modelo `ReleaseDetails`.
     */
    suspend fun getLatestReleaseDetails(): Result<ReleaseDetails> = runCatching {
        val response = client.get(dotenv["UPDATER_URL"]).bodyAsText()
        val json = JSONObject(response)

        val version = json.getString("Version")
        val title = json.optString("Titulo", "No disponible")
        val description = json.optString("Descripcion", "No disponible")
        val author = json.optString("Autor", "No disponible")
        val downloadUrl = json.optString("Descarga", "No disponible")

        lastCheckTime = System.currentTimeMillis()

        ReleaseDetails(
            version = version,
            title = title,
            description = description,
            author = author,
            downloadUrl = downloadUrl
        )
    }

    // Obtener solo la versión.



    suspend fun getLatestVersionName(): Result<String> = runCatching {
        val response = client.get(dotenv["UPDATER_URL"]).bodyAsText()
        val json = JSONObject(response)
        val versionName = json.getString("Version")
        lastCheckTime = System.currentTimeMillis()
        versionName
    }
}