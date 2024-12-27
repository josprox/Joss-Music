import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Header

interface ApiService {

    // Búsqueda de canción
    @GET("yt/connect/search/{query}")
    suspend fun searchSong(
        @Path("query") query: String,
        @Header("Authorization") accessToken: String,
        @Header("Refresh-Token") refreshToken: String
    ): Response<SongSearchResponse>

    // Consultar las playlists
    @GET("yt/connect/playlist")
    suspend fun getPlaylists(
        @Header("Authorization") accessToken: String,
        @Header("Refresh-Token") refreshToken: String
    ): Response<PlaylistResponse>

    // Consultar playlists públicas
    @GET("yt/connect/playlist_public")
    suspend fun getPublicPlaylists(
        @Header("Authorization") accessToken: String,
        @Header("Refresh-Token") refreshToken: String
    ): Response<PlaylistResponse>

    // Consultar playlists privadas
    @GET("yt/connect/playlist_private")
    suspend fun getPrivatePlaylists(
        @Header("Authorization") accessToken: String,
        @Header("Refresh-Token") refreshToken: String
    ): Response<PlaylistResponse>

    // Obtener canciones de una playlist
    @GET("yt/connect/get_playlists/{id}")
    suspend fun getPlaylistSongs(
        @Path("id") playlistId: String,
        @Header("Authorization") accessToken: String,
        @Header("Refresh-Token") refreshToken: String
    ): Response<PlaylistSongsResponse>

    // Crear una nueva playlist
    @POST("yt/connect/playlist/add/{title}/{description}")
    suspend fun createPlaylist(
        @Path("title") title: String,
        @Path("description") description: String,
        @Header("Authorization") accessToken: String,
        @Header("Refresh-Token") refreshToken: String
    ): Response<CreatePlaylistResponse>

    // Eliminar una playlist
    @POST("yt/connect/playlist/remove/{id}")
    suspend fun removePlaylist(
        @Path("id") playlistId: String,
        @Header("Authorization") accessToken: String,
        @Header("Refresh-Token") refreshToken: String
    ): Response<RemovePlaylistResponse>

    // Insertar o eliminar canciones de una playlist
    @POST("yt/connect/playlist/item/{action}/{playlistId}/{itemId}")
    suspend fun managePlaylistItem(
        @Path("action") action: String,
        @Path("playlistId") playlistId: String,
        @Path("itemId") itemId: String,
        @Header("Authorization") accessToken: String,
        @Header("Refresh-Token") refreshToken: String
    ): Response<ManagePlaylistItemResponse>
}
