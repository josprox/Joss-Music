package com.zionhuang.music.utils

import android.net.ConnectivityManager
import androidx.media3.common.PlaybackException
import com.zionhuang.music.constants.AudioQuality
import com.zionhuang.innertube.NewPipeUtils
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.YouTubeClient
import com.zionhuang.innertube.models.YouTubeClient.Companion.IOS
import com.zionhuang.innertube.models.YouTubeClient.Companion.TVHTML5_SIMPLY_EMBEDDED_PLAYER
import com.zionhuang.innertube.models.YouTubeClient.Companion.WEB_REMIX
import com.zionhuang.innertube.models.response.PlayerResponse
import com.zionhuang.innertube.models.YouTubeClient.Companion.ANDROID_VR_NO_AUTH
import com.zionhuang.innertube.models.YouTubeClient.Companion.MOBILE
import com.zionhuang.innertube.models.YouTubeClient.Companion.WEB
import com.zionhuang.innertube.models.YouTubeClient.Companion.WEB_CREATOR
import okhttp3.OkHttpClient
import timber.log.Timber

object YTPlayerUtils {

    class PlaybackException(
        val statusCode: Int,
        message: String,
        cause: Throwable? = null
    ) : Exception(message, cause)

    private const val logTag = "YTPlayerUtils"

    private val httpClient = OkHttpClient.Builder()
        .proxy(YouTube.proxy)
        .build()


    /**
     * El cliente principal se usa para los metadatos y los streams iniciales.
     * No se deben usar otros clientes para esto porque puede resultar en metadatos inconsistentes.
     * Por ejemplo, otros clientes pueden tener diferentes objetivos de normalización (loudnessDb).
     *
     * [WEB_REMIX] debe preferirse aquí porque actualmente es el único cliente que proporciona:
     * - los metadatos correctos (como loudnessDb)
     * - formatos premium
     */
    private val MAIN_CLIENT: YouTubeClient = WEB_REMIX
    /**
     * Clientes usados como respaldo para los streams en caso de que los del cliente principal no funcionen.
     */
    private val STREAM_FALLBACK_CLIENTS: Array<YouTubeClient> = arrayOf(
        ANDROID_VR_NO_AUTH,
        MOBILE,
        TVHTML5_SIMPLY_EMBEDDED_PLAYER,
        IOS,
        WEB,
        WEB_CREATOR
    )
    data class PlaybackData(
        val audioConfig: PlayerResponse.PlayerConfig.AudioConfig?,
        val videoDetails: PlayerResponse.VideoDetails?,
        val format: PlayerResponse.StreamingData.Format,
        val streamUrl: String,
        val streamExpiresInSeconds: Int,
    )
    /**
     * Respuesta personalizada del reproductor destinada a usarse para la reproducción.
     * Los metadatos como audioConfig y videoDetails provienen de [MAIN_CLIENT].
     * El formato y el stream pueden provenir de [MAIN_CLIENT] o de [STREAM_FALLBACK_CLIENTS].
     */

    suspend fun playerResponseForPlayback(
        videoId: String,
        playlistId: String? = null,
        audioQuality: AudioQuality,
        connectivityManager: ConnectivityManager,
    ): Result<PlaybackData> = runCatching {
        Timber.tag(logTag).d("Obteniendo respuesta del reproductor para videoId: $videoId, playlistId: $playlistId")
        /**
         * Esto es necesario para que algunos clientes obtengan streams funcionales; sin embargo,
         * no debe forzarse para el [MAIN_CLIENT] porque se necesita la respuesta de este cliente
         * incluso si los streams no funcionan desde él.
         * Por eso se permite que sea null.
         */
        val signatureTimestamp = getSignatureTimestampOrNull(videoId)
        Timber.tag(logTag).d("Marca de tiempo de firma: $signatureTimestamp")

        val isLoggedIn = YouTube.cookie != null
        val sessionId =
            if (isLoggedIn) {
                // las sesiones con inicio de sesión usan dataSyncId como identificador
                YouTube.dataSyncId
            } else {
                // las sesiones sin iniciar sesión usan visitorData como identificador
                YouTube.visitorData
            }
        Timber.tag(logTag).d("Estado de autenticación de la sesión: ${if (isLoggedIn) "Con sesión iniciada" else "Sin sesión iniciada"}")

        Timber.tag(logTag).d("Intentando obtener respuesta del reproductor usando MAIN_CLIENT: ${MAIN_CLIENT.clientName}")
        val mainPlayerResponse =
            YouTube.player(videoId, playlistId, MAIN_CLIENT, signatureTimestamp).getOrThrow()
        val audioConfig = mainPlayerResponse.playerConfig?.audioConfig
        val videoDetails = mainPlayerResponse.videoDetails
        var format: PlayerResponse.StreamingData.Format? = null
        var streamUrl: String? = null
        var streamExpiresInSeconds: Int? = null
        var streamPlayerResponse: PlayerResponse? = null

        for (clientIndex in (-1 until STREAM_FALLBACK_CLIENTS.size)) {
            // reiniciar para cada cliente
            format = null
            streamUrl = null
            streamExpiresInSeconds = null

            // decidir qué cliente usar para los streams y cargar su respuesta del reproductor
            val client: YouTubeClient
            if (clientIndex == -1) {
                // primero intentar con streams del cliente principal
                client = MAIN_CLIENT
                streamPlayerResponse = mainPlayerResponse
                Timber.tag(logTag).d("Probando stream de MAIN_CLIENT: ${client.clientName}")
            } else {
                // después del cliente principal usar clientes de respaldo
                client = STREAM_FALLBACK_CLIENTS[clientIndex]
                Timber.tag(logTag).d("Probando cliente de respaldo ${clientIndex + 1}/${STREAM_FALLBACK_CLIENTS.size}: ${client.clientName}")

                if (client.loginRequired && !isLoggedIn && YouTube.cookie == null) {
                    // saltar cliente si requiere inicio de sesión pero el usuario no ha iniciado sesión
                    Timber.tag(logTag).d("Omitiendo cliente ${client.clientName} - requiere inicio de sesión pero el usuario no ha iniciado sesión")
                    continue
                }

                Timber.tag(logTag).d("Obteniendo respuesta del reproductor para cliente de respaldo: ${client.clientName}")
                streamPlayerResponse =
                    YouTube.player(videoId, playlistId, client, signatureTimestamp).getOrNull()
            }

            // procesar respuesta del cliente actual
            if (streamPlayerResponse?.playabilityStatus?.status == "OK") {
                Timber.tag(logTag).d("Estado de respuesta del reproductor OK para cliente: ${if (clientIndex == -1) MAIN_CLIENT.clientName else STREAM_FALLBACK_CLIENTS[clientIndex].clientName}")

                format =
                    findFormat(
                        streamPlayerResponse,
                        audioQuality,
                        connectivityManager,
                    )

                if (format == null) {
                    Timber.tag(logTag).d("No se encontró formato adecuado para cliente: ${if (clientIndex == -1) MAIN_CLIENT.clientName else STREAM_FALLBACK_CLIENTS[clientIndex].clientName}")
                    continue
                }

                Timber.tag(logTag).d("Formato encontrado: ${format.mimeType}, bitrate: ${format.bitrate}")

                streamUrl = findUrlOrNull(format, videoId)
                if (streamUrl == null) {
                    Timber.tag(logTag).d("No se encontró URL del stream para el formato")
                    continue
                }

                streamExpiresInSeconds = streamPlayerResponse.streamingData?.expiresInSeconds
                if (streamExpiresInSeconds == null) {
                    Timber.tag(logTag).d("No se encontró tiempo de expiración del stream")
                    continue
                }

                Timber.tag(logTag).d("El stream expira en: $streamExpiresInSeconds segundos")

                if (clientIndex == STREAM_FALLBACK_CLIENTS.size - 1) {
                    /** omitir [validateStatus] para el último cliente */
                    Timber.tag(logTag).d("Usando último cliente de respaldo sin validación: ${STREAM_FALLBACK_CLIENTS[clientIndex].clientName}")
                    break
                }

                if (validateStatus(streamUrl)) {
                    // se encontró un stream funcional
                    Timber.tag(logTag).d("Stream validado exitosamente con cliente: ${if (clientIndex == -1) MAIN_CLIENT.clientName else STREAM_FALLBACK_CLIENTS[clientIndex].clientName}")
                    break
                } else {
                    Timber.tag(logTag).d("Validación del stream fallida para cliente: ${if (clientIndex == -1) MAIN_CLIENT.clientName else STREAM_FALLBACK_CLIENTS[clientIndex].clientName}")
                }
            } else {
                Timber.tag(logTag).d("Estado de respuesta del reproductor no OK: ${streamPlayerResponse?.playabilityStatus?.status}, razón: ${streamPlayerResponse?.playabilityStatus?.reason}")
            }
        }

        if (streamPlayerResponse == null) {
            Timber.tag(logTag).e("Mala respuesta del reproductor de stream - todos los clientes fallaron")
            throw Exception("Mala respuesta del reproductor de stream")
        }

        if (streamPlayerResponse.playabilityStatus.status != "OK") {
            val errorReason = streamPlayerResponse.playabilityStatus.reason
            Timber.tag(logTag).e("Estado de capacidad de reproducción no OK: $errorReason")
            throw PlaybackException(
                statusCode = 403, // o el código de estado apropiado
                message = errorReason ?: "Error de reproducción"
            )
        }

        if (streamExpiresInSeconds == null) {
            Timber.tag(logTag).e("Falta tiempo de expiración del stream")
            throw Exception("Falta tiempo de expiración del stream")
        }

        if (format == null) {
            Timber.tag(logTag).e("No se pudo encontrar el formato")
            throw Exception("No se pudo encontrar el formato")
        }

        if (streamUrl == null) {
            Timber.tag(logTag).e("No se pudo encontrar la URL del stream")
            throw Exception("No se pudo encontrar la URL del stream")
        }

        Timber.tag(logTag).d("Datos de reproducción obtenidos exitosamente con formato: ${format.mimeType}, bitrate: ${format.bitrate}")
        PlaybackData(
            audioConfig,
            videoDetails,
            format,
            streamUrl,
            streamExpiresInSeconds,
        )
    }
    /**
     * Respuesta simple del reproductor destinada a usarse solo para metadatos.
     * Las URLs de stream de esta respuesta podrían no funcionar, así que no las uses.
     */
    suspend fun playerResponseForMetadata(
        videoId: String,
        playlistId: String? = null,
    ): Result<PlayerResponse> {
        Timber.tag(logTag).d("Obteniendo respuesta del reproductor solo para metadatos para videoId: $videoId usando MAIN_CLIENT: ${MAIN_CLIENT.clientName}")
        return YouTube.player(videoId, playlistId, client = MAIN_CLIENT)
            .onSuccess { Timber.tag(logTag).d("Metadatos obtenidos exitosamente") }
            .onFailure { Timber.tag(logTag).e(it, "Error al obtener metadatos") }
    }

    private fun findFormat(
        playerResponse: PlayerResponse,
        audioQuality: AudioQuality,
        connectivityManager: ConnectivityManager,
    ): PlayerResponse.StreamingData.Format? {
        Timber.tag(logTag).d("Buscando formato con calidad de audio: $audioQuality, red medida: ${connectivityManager.isActiveNetworkMetered}")

        val format = playerResponse.streamingData?.adaptiveFormats
            ?.filter { it.isAudio }
            ?.maxByOrNull {
                it.bitrate * when (audioQuality) {
                    AudioQuality.AUTO -> if (connectivityManager.isActiveNetworkMetered) -1 else 1
                    AudioQuality.HIGH -> 1
                    AudioQuality.LOW -> -1
                } + (if (it.mimeType.startsWith("audio/webm")) 10240 else 0) // preferir stream opus
            }

        if (format != null) {
            Timber.tag(logTag).d("Formato seleccionado: ${format.mimeType}, bitrate: ${format.bitrate}")
        } else {
            Timber.tag(logTag).d("No se encontró formato de audio adecuado")
        }

        return format
    }
    /**
     * Verifica si la URL del stream devuelve un estado exitoso.
     * Si esto devuelve true, es probable que la URL funcione.
     * Si esto devuelve false, la URL podría causar un error durante la reproducción.
     */
    private fun validateStatus(url: String): Boolean {
        Timber.tag(logTag).d("Validando estado de URL del stream")
        try {
            val requestBuilder = okhttp3.Request.Builder()
                .head()
                .url(url)
            val response = httpClient.newCall(requestBuilder.build()).execute()
            val isSuccessful = response.isSuccessful
            Timber.tag(logTag).d("Resultado de validación de URL del stream: ${if (isSuccessful) "Éxito" else "Falló"} (${response.code})")
            return isSuccessful
        } catch (e: Exception) {
            Timber.tag(logTag).e(e, "Validación de URL del stream falló con excepción")
            reportException(e)
        }
        return false
    }
    /**
     * Envoltura alrededor de la función [NewPipeUtils.getSignatureTimestamp] que reporta excepciones
     */
    private fun getSignatureTimestampOrNull(
        videoId: String
    ): Int? {
        Timber.tag(logTag).d("Obteniendo marca de tiempo de firma para videoId: $videoId")
        return NewPipeUtils.getSignatureTimestamp(videoId)
            .onSuccess { Timber.tag(logTag).d("Marca de tiempo de firma obtenida: $it") }
            .onFailure {
                Timber.tag(logTag).e(it, "Error al obtener marca de tiempo de firma")
                reportException(it)
            }
            .getOrNull()
    }
    /**
     * Envoltura alrededor de la función [NewPipeUtils.getStreamUrl] que reporta excepciones
     */
    private fun findUrlOrNull(
        format: PlayerResponse.StreamingData.Format,
        videoId: String
    ): String? {
        Timber.tag(logTag).d("Buscando URL del stream para formato: ${format.mimeType}, videoId: $videoId")
        return NewPipeUtils.getStreamUrl(format, videoId)
            .onSuccess { Timber.tag(logTag).d("URL del stream obtenida exitosamente") }
            .onFailure {
                Timber.tag(logTag).e(it, "Error al obtener URL del stream")
                reportException(it)
            }
            .getOrNull()
    }
}