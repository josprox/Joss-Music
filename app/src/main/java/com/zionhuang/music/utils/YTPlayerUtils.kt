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

object YTPlayerUtils {

    private val httpClient = OkHttpClient.Builder()
        .proxy(YouTube.proxy)
        .build()

    /**
     * El cliente principal se utiliza para los metadatos y las transmisiones iniciales.
     * No se deben usar otros clientes para esto, ya que puede dar lugar a metadatos inconsistentes.
     * Por ejemplo, otros clientes pueden tener objetivos de normalización diferentes (loudnessDb).
     *
     * [com.zionhuang.innertube.models.YouTubeClient.WEB_REMIX] debería ser el preferido aquí porque actualmente es el único cliente que proporciona:
     * - los metadatos correctos (como loudnessDb)
     * - formatos premium
     */
    private val MAIN_CLIENT: YouTubeClient = WEB_REMIX

    /**
     * Clientes usados para transmisiones de respaldo en caso de que las del cliente principal no funcionen.
     */
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

    /**
     * Respuesta de reproductor personalizada destinada para la reproducción.
     * Los metadatos como audioConfig y videoDetails provienen de [MAIN_CLIENT].
     * El formato y la transmisión pueden ser de [MAIN_CLIENT] o de [STREAM_FALLBACK_CLIENTS].
     */
    suspend fun playerResponseForPlayback(
        videoId: String,
        playlistId: String? = null,
        playedFormat: FormatEntity?,
        audioQuality: AudioQuality,
        connectivityManager: ConnectivityManager,
    ): Result<PlaybackData> = runCatching {
        val signatureTimestamp = getSignatureTimestampOrNull(videoId)

        val mainPlayerResponse =
            YouTube.player(videoId, playlistId, MAIN_CLIENT, signatureTimestamp).getOrThrow()

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

            if (streamPlayerResponse?.playabilityStatus?.status != "OK") continue

            format = findFormat(streamPlayerResponse, playedFormat, audioQuality, connectivityManager)
                ?: continue

            streamUrl = findUrlOrNull(format, videoId) ?: continue
            streamExpiresInSeconds = streamPlayerResponse.streamingData?.expiresInSeconds ?: continue

            if (clientIndex == STREAM_FALLBACK_CLIENTS.size - 1 || validateStatus(streamUrl)) {
                break
            }
        }

        if (streamPlayerResponse == null) {
            return Result.failure(Exception("No se obtuvo una respuesta válida del reproductor"))
        }

        // Si el estado no es OK, pero hay metadatos, devolverlos con error manejable
        if (streamPlayerResponse.playabilityStatus.status != "OK") {
            return Result.failure(
                PlaybackException(
                    streamPlayerResponse.playabilityStatus.reason ?: "Motivo desconocido",
                    null,
                    PlaybackException.ERROR_CODE_REMOTE_ERROR
                )
            )
        }

        // Validaciones finales de datos críticos
        if (streamExpiresInSeconds == null || format == null || streamUrl == null) {
            return Result.failure(Exception("No se pudo obtener información completa de la transmisión"))
        }

        return Result.success(
            PlaybackData(
                audioConfig,
                videoDetails,
                format,
                streamUrl,
                streamExpiresInSeconds,
            )
        )
    }


    /**
     * Respuesta simple del reproductor destinada solo para metadatos.
     * Las URLs de transmisión de esta respuesta podrían no funcionar, por lo que no deben usarse.
     */
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
                    } + (if (it.mimeType.startsWith("audio/webm")) 10240 else 0) // preferir flujo opus
                }
        }

    /**
     * Verifica si la URL de transmisión devuelve un estado exitoso.
     * Si esto devuelve true, la URL probablemente funcionará.
     * Si esto devuelve false, la URL podría causar un error durante la reproducción.
     */
    private fun validateStatus(url: String): Boolean {
        try {
            val requestBuilder = okhttp3.Request.Builder()
                .head()
                .url(url)
            val response = httpClient.newCall(requestBuilder.build()).execute()
            return response.isSuccessful
        } catch (e: Exception) {
            reportException(e)
        }
        return false
    }

    /**
     * Envoltura alrededor de la función [NewPipeUtils.getSignatureTimestamp] que informa excepciones.
     */
    private fun getSignatureTimestampOrNull(
        videoId: String
    ): Int? {
        return NewPipeUtils.getSignatureTimestamp(videoId)
            .onFailure {
                reportException(it)
            }
            .getOrNull()
    }

    /**
     * Envoltura alrededor de la función [NewPipeUtils.getStreamUrl] que informa excepciones.
     */
    private fun findUrlOrNull(
        format: PlayerResponse.StreamingData.Format,
        videoId: String
    ): String? {
        return NewPipeUtils.getStreamUrl(format, videoId)
            .onFailure {
                reportException(it)
            }
            .getOrNull()
    }
}