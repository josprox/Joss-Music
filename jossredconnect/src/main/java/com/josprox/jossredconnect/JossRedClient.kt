import androidx.media3.datasource.DataSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import android.net.Uri

object JossRedClient {
    private const val BASE_STREAM_URL = "https://jossred.josprox.com/yt/v2/stream/"
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // Excepción personalizada para errores de JossRed
    class JossRedException(
        val statusCode: Int,
        message: String,
        cause: Throwable? = null
    ) : Exception(message, cause)

    // Metodo para obtener la URL de streaming con manejo de errores
    fun getStreamingUrl(mediaId: String, secretKey: String): String {
        val requestUrl = "$BASE_STREAM_URL$mediaId"

        val request = Request.Builder()
            .url(requestUrl)
            .addHeader("X-JossRed-Auth", secretKey)
            .get()
            .build()

        val response = try {
            httpClient.newCall(request).execute()
        } catch (e: IOException) {
            throw JossRedException(
                statusCode = -1,
                message = "Error de conexión: ${e.message}",
                cause = e
            )
        }

        val responseBody = response.body?.string()
        val code = response.code

        if (!response.isSuccessful || responseBody?.contains("Unable to fetch audio URL") == true) {
            response.close()

            val message = if (responseBody?.contains("Unable to fetch audio URL") == true) {
                "Error del servidor: No se pudo obtener el audio desde JossRed"
            } else {
                when (code) {
                    403 -> "Acceso denegado (403) para el recurso"
                    404 -> "Recurso no encontrado (404)"
                    in 400..499 -> "Error del cliente ($code)"
                    in 500..599 -> "Error del servidor ($code)"
                    else -> "Error desconocido ($code)"
                }
            }

            throw JossRedException(
                statusCode = code,
                message = message
            )
        }

        return requestUrl
    }

    fun resolveDataSpec(original: DataSpec, mediaId: String, secretKey: String): DataSpec {
        val streamUrl = getStreamingUrl(mediaId, secretKey)

        return original.buildUpon()
            .setUri(Uri.parse(streamUrl))
            .setHttpRequestHeaders(mapOf("X-JossRed-Auth" to secretKey))
            .build()
    }


}