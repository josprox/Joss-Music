object JossRedClient {

    private const val BASE_URL_API = "https://jossred.josprox.com/api/"
    private const val BASE_STREAM_URL = "https://jossred.josprox.com/yt/v2/stream/"

    // Metodo para obtener la URL de streaming
    fun getStreamingUrl(mediaId: String): String {
        return "$BASE_STREAM_URL$mediaId"
    }
}