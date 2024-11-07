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

    suspend fun getLatestVersionName(): Result<String> = runCatching {
        val response = client.get(dotenv["UPDATER_URL"]).bodyAsText()
        val json = JSONObject(response)
        val versionName = json.getString("Version")
        lastCheckTime = System.currentTimeMillis()
        versionName
    }
}