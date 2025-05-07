package com.zionhuang.music.utils

import com.zionhuang.music.BuildConfig
import org.dotenv.vault.dotenvVault

object SecureKeys {
    fun getJossRedKey(): String {
        val dotenv = dotenvVault(BuildConfig.DOTENV_KEY) {
            directory = "/assets"
            filename = "env.vault"
        }
        return dotenv["STREAMING_HEAD_JOSSRED"] ?: ""
    }
}