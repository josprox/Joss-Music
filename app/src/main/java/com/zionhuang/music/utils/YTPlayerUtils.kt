package com.zionhuang.music.utils

import android.net.ConnectivityManager
import androidx.media3.common.PlaybackException
import com.zionhuang.innertube.NewPipeUtils
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.YouTubeClient
import com.zionhuang.innertube.models.YouTubeClient.Companion.IOS
import com.zionhuang.innertube.models.YouTubeClient.Companion.TVHTML5_SIMPLY_EMBEDDED_PLAYER
import com.zionhuang.innertube.models.YouTubeClient.Companion.WEB_REMIX
import com.zionhuang.innertube.models.response.PlayerResponse
import com.zionhuang.music.constants.AudioQuality
import com.zionhuang.music.db.entities.FormatEntity
import okhttp3.OkHttpClient
import java.io.IOException

object YTPlayerUtils {

    class PlaybackException(
        val statusCode: Int,
        message: String,
        cause: Throwable? = null
    ) : Exception(message, cause)

    private val httpClient = OkHttpClient.Builder()
        .proxy(YouTube.proxy)
        .build()

    private val MAIN_CLIENT: YouTubeClient = WEB_REMIX

    private val STREAM_FALLBACK_CLIENTS: Array<YouTubeClient> = arrayOf(
        TVHTML5_SIMPLY_EMBEDDED_PLAYER,
        IOS,
    )

    data class PlaybackData(
        val audioConfig: PlayerResponse.PlayerConfig.AudioConfig?,
        val videoDetails: PlayerResponse.VideoDetails?,
        val format: PlayerResponse.StreamingData.Format,
        val streamUrl: String,
        val streamExpiresInSeconds: Int,
    )

    suspend fun playerResponseForPlayback(
        videoId: String,
        playlistId: String? = null,
        playedFormat: FormatEntity?,
        audioQuality: AudioQuality,
        connectivityManager: ConnectivityManager,
    ): Result<PlaybackData> = runCatching {
        val signatureTimestamp = getSignatureTimestampOrNull(videoId)

        val mainPlayerResponse = YouTube.player(videoId, playlistId, MAIN_CLIENT, signatureTimestamp).getOrThrow()

        // Manejar códigos de estado específicos del reproductor
        mainPlayerResponse.playabilityStatus?.let { status ->
            when (status.status) {
                "ERROR" -> throw PlaybackException(
                    statusCode = 403,
                    message = status.reason ?: "Error de reproducción"
                )
                "UNPLAYABLE" -> throw PlaybackException(
                    statusCode = 403,
                    message = status.reason ?: "Contenido no reproducible"
                )
                "LOGIN_REQUIRED" -> throw PlaybackException(
                    statusCode = 401,
                    message = "Se requiere inicio de sesión"
                )
                "AGE_CHECK_REQUIRED" -> throw PlaybackException(
                    statusCode = 403,
                    message = "Verificación de edad requerida"
                )

                else -> {}
            }
        }

        val audioConfig = mainPlayerResponse.playerConfig?.audioConfig
        val videoDetails = mainPlayerResponse.videoDetails

        var format: PlayerResponse.StreamingData.Format? = null
        var streamUrl: String? = null
        var streamExpiresInSeconds: Int? = null

        var streamPlayerResponse: PlayerResponse? = null

        for (clientIndex in (-1 until STREAM_FALLBACK_CLIENTS.size)) {
            format = null
            streamUrl = null
            streamExpiresInSeconds = null

            streamPlayerResponse = if (clientIndex == -1) {
                mainPlayerResponse
            } else {
                val client = STREAM_FALLBACK_CLIENTS[clientIndex]
                if (client.loginRequired && YouTube.cookie == null) {
                    continue
                }
                YouTube.player(videoId, playlistId, client, signatureTimestamp).getOrNull()
            }

            if (streamPlayerResponse?.playabilityStatus?.status != "OK") {
                val status = streamPlayerResponse?.playabilityStatus
                throw PlaybackException(
                    statusCode = when (status?.status) {
                        "LOGIN_REQUIRED" -> 401
                        "AGE_CHECK_REQUIRED" -> 403
                        else -> 400
                    },
                    message = status?.reason ?: "Error desconocido al reproducir"
                )
            }

            format = findFormat(streamPlayerResponse, playedFormat, audioQuality, connectivityManager)
                ?: continue

            streamUrl = findUrlOrNull(format, videoId) ?: continue
            streamExpiresInSeconds = streamPlayerResponse.streamingData?.expiresInSeconds ?: continue

            if (clientIndex == STREAM_FALLBACK_CLIENTS.size - 1 || validateStatus(streamUrl)) {
                break
            }
        }

        if (streamPlayerResponse == null) {
            throw PlaybackException(404, "No se obtuvo una respuesta válida del reproductor")
        }

        if (streamExpiresInSeconds == null || format == null || streamUrl == null) {
            throw PlaybackException(500, "No se pudo obtener información completa de la transmisión")
        }

        PlaybackData(
            audioConfig,
            videoDetails,
            format,
            streamUrl,
            streamExpiresInSeconds,
        )
    }.mapError { e ->
        when (e) {
            is PlaybackException -> e
            is IOException -> PlaybackException(
                statusCode = 503,
                message = "Error de conexión: ${e.message}",
                cause = e
            )
            else -> PlaybackException(
                statusCode = 500,
                message = "Error desconocido: ${e.message}",
                cause = e
            )
        }
    }

    suspend fun playerResponseForMetadata(
        videoId: String,
        playlistId: String? = null,
    ): Result<PlayerResponse> =
        YouTube.player(videoId, playlistId, client = MAIN_CLIENT)

    private fun findFormat(
        playerResponse: PlayerResponse,
        playedFormat: FormatEntity?,
        audioQuality: AudioQuality,
        connectivityManager: ConnectivityManager,
    ): PlayerResponse.StreamingData.Format? =
        if (playedFormat != null) {
            playerResponse.streamingData?.adaptiveFormats?.find { it.itag == playedFormat.itag }
        } else {
            playerResponse.streamingData?.adaptiveFormats
                ?.filter { it.isAudio }
                ?.maxByOrNull {
                    it.bitrate * when (audioQuality) {
                        AudioQuality.AUTO -> if (connectivityManager.isActiveNetworkMetered) -1 else 1
                        AudioQuality.HIGH -> 1
                        AudioQuality.LOW -> -1
                    } + (if (it.mimeType.startsWith("audio/webm")) 10240 else 0)
                }
        }

    private fun validateStatus(url: String): Boolean {
        return try {
            val request = okhttp3.Request.Builder()
                .head()
                .url(url)
                .build()
            val response = httpClient.newCall(request).execute()
            response.use {
                it.isSuccessful
            }
        } catch (e: Exception) {
            reportException(e)
            false
        }
    }

    private fun getSignatureTimestampOrNull(videoId: String): Int? {
        return NewPipeUtils.getSignatureTimestamp(videoId)
            .onFailure { reportException(it) }
            .getOrNull()
    }

    private fun findUrlOrNull(
        format: PlayerResponse.StreamingData.Format,
        videoId: String
    ): String? {
        return NewPipeUtils.getStreamUrl(format, videoId)
            .onFailure { reportException(it) }
            .getOrNull()
    }

    private inline fun <T> Result<T>.mapError(transform: (Throwable) -> Throwable): Result<T> {
        return when {
            isSuccess -> this
            else -> Result.failure(transform(exceptionOrNull()!!))
        }
    }
}