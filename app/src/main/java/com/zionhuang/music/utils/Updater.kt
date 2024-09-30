package com.zionhuang.music.utils

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.json.JSONObject

object Updater {
    private val client = HttpClient()
    var lastCheckTime = -1L
        private set

    suspend fun getLatestVersionName(): Result<String> = runCatching {
        val response = client.get("https://api.github.com/repos/josprox/Joss-Music/releases/latest").bodyAsText()
        val json = JSONObject(response)
        // Obtener el nombre del tag y eliminar el prefijo "V"
        val versionName = json.getString("tag_name").removePrefix("V")
        lastCheckTime = System.currentTimeMillis()
        versionName
    }
}

