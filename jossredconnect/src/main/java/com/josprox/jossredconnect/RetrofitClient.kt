import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://jossred.josprox.com/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}

data class SongSearchResponse(
    val success: Boolean,
    val results: List<Song>
)

data class PlaylistResponse(
    val success: Boolean,
    val playlists: List<Playlist>
)

data class PlaylistSongsResponse(
    val success: Boolean,
    val songs: List<Song>
)

data class CreatePlaylistResponse(
    val success: Boolean,
    val playlist: Playlist
)

data class RemovePlaylistResponse(
    val success: Boolean
)

data class ManagePlaylistItemResponse(
    val success: Boolean
)

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val url: String
)

data class Playlist(
    val id: String,
    val title: String,
    val description: String
)
