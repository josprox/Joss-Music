package com.zionhuang.music.utils

import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.json.JSONObject
object Updater {
    private val client = HttpClient()
    val dotenv = dotenv {
        directory = "/assets"
        filename = "env" // instead of 'env', use 'env'
    }
    var lastCheckTime = -1L
        private set

    suspend fun getLatestVersionName(): Result<String> = runCatching {
        val response = client.get(dotenv["JOSS_RED"]).bodyAsText()
        val json = JSONObject(response)
        val versionName = json.getString("Version")
        lastCheckTime = System.currentTimeMillis()
        versionName
    }
}