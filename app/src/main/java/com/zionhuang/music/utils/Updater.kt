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
        val response = client.get("https://jossred.josprox.com/?app_name=com.josprox.jossmusic").bodyAsText()
        val json = JSONObject(response)
        val versionName = json.getString("Version")
        lastCheckTime = System.currentTimeMillis()
        versionName
    }
}