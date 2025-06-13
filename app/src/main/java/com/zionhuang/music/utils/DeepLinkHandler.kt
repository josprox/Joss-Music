package com.zionhuang.music.utils

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.music.ui.screens.Screens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class DeepLinkHandler(
    private val navController: NavController,
    private val coroutineScope: CoroutineScope
) {

    /**
     * Maneja los deep links iniciales cuando se abre la aplicación.
     */
    fun handleInitialDeepLink(intent: Intent?) {
        intent?.data?.let { uri ->
            handleUri(uri)
        }
    }

    /**
     * Maneja los deep links cuando la aplicación ya está abierta.
     */
    fun handleNewIntent(intent: Intent, onSharedSong: (SongItem) -> Unit) {
        val uri = intent.data ?: intent.extras?.getString(Intent.EXTRA_TEXT)?.toUri() ?: return
        handleUri(uri, onSharedSong)
    }

    /**
     * Centraliza el manejo de URIs, decidiendo si es un enlace de Joss Music o de YouTube.
     */
    private fun handleUri(uri: Uri, onSharedSong: ((SongItem) -> Unit)? = null) {
        when (uri.host) {
            "jossmusic.com" -> handleJossMusicLink(uri, onSharedSong)
            "youtu.be", "youtube.com", "www.youtube.com", "music.youtube.com" -> {
                // La lógica original para enlaces de YouTube
                handleYouTubeLink(uri, onSharedSong)
            }
            else -> {
                // Lógica para otros casos, si aplica
                if (onSharedSong == null) {
                    handleDeepLinkUri(uri) // Podría ser un deeplink interno
                }
            }
        }
    }

    /**
     * Nueva función para manejar específicamente los enlaces de jossmusic.com
     */
    private fun handleJossMusicLink(uri: Uri, onSharedSong: ((SongItem) -> Unit)?) {
        val pathSegments = uri.pathSegments
        if (pathSegments.size < 2) return // Debe tener al menos /type/id

        val type = pathSegments.getOrNull(0)
        val id = pathSegments.getOrNull(1) ?: return

        when (type) {
            "sound", "video" -> {
                // Es una canción o video, la tratamos como un video de YouTube.
                if (onSharedSong != null) {
                    fetchSongFromVideoId(id, onSharedSong)
                }
            }
            "album" -> {
                // Es un álbum. El ID es un playlistId que necesita ser procesado.
                handleYouTubeMusicAlbum(id)
            }
            "playlist" -> {
                // Es una playlist. El ID es directo.
                navController.navigate("online_playlist/$id")
            }
        }
    }

    /**
     * Lógica original para manejar enlaces de YouTube, ahora en su propia función.
     */
    private fun handleYouTubeLink(uri: Uri, onSharedSong: ((SongItem) -> Unit)?) {
        when (val path = uri.pathSegments.firstOrNull()) {
            "playlist" -> handlePlaylistLink(uri)
            "channel", "c" -> handleChannelLink(uri)
            else -> {
                if (onSharedSong != null) {
                    handleVideoLink(uri, path, onSharedSong)
                }
            }
        }
    }


    /**
     * Maneja URIs de deep links internos (si los tuvieras)
     */
    private fun handleDeepLinkUri(uri: Uri) {
        val pathSegments = uri.pathSegments
        when {
            pathSegments.contains("playlist") -> {
                val playlistId = uri.getQueryParameter("list") ?: pathSegments.lastOrNull()
                playlistId?.let {
                    if (it.startsWith("OLAK5uy_")) {
                        handleYouTubeMusicAlbum(it)
                    } else {
                        navController.navigate("online_playlist/$it")
                    }
                }
            }
            pathSegments.contains("artist") -> {
                val artistId = pathSegments.lastOrNull()
                artistId?.let {
                    navController.navigate("artist/$it")
                }
            }
            pathSegments.contains("album") -> {
                val albumId = pathSegments.lastOrNull()
                albumId?.let {
                    // Si llega aquí, es probable que ya sea un browseId
                    navController.navigate("album/$it")
                }
            }
            else -> {
                navController.navigate(Screens.Home.route)
            }
        }
    }

    /**
     * Maneja enlaces de playlist de YouTube
     */
    private fun handlePlaylistLink(uri: Uri) {
        uri.getQueryParameter("list")?.let { playlistId ->
            if (playlistId.startsWith("OLAK5uy_")) {
                // Es un álbum de YouTube Music
                handleYouTubeMusicAlbum(playlistId)
            } else {
                // Es una playlist normal
                navController.navigate("online_playlist/$playlistId")
            }
        }
    }

    /**
     * Maneja enlaces de canal/artista de YouTube
     */
    private fun handleChannelLink(uri: Uri) {
        uri.lastPathSegment?.let { artistId ->
            navController.navigate("artist/$artistId")
        }
    }

    /**
     * Maneja enlaces de videos de YouTube
     */
    private fun handleVideoLink(uri: Uri, path: String?, onSharedSong: (SongItem) -> Unit) {
        val videoId = when {
            path == "watch" -> uri.getQueryParameter("v")
            uri.host == "youtu.be" -> uri.lastPathSegment
            else -> null
        }

        videoId?.let { id ->
            fetchSongFromVideoId(id, onSharedSong)
        }
    }

    /**
     * Función refactorizada para obtener una canción a partir de un videoId.
     */
    private fun fetchSongFromVideoId(videoId: String, onSharedSong: (SongItem) -> Unit) {
        coroutineScope.launch {
            try {
                val songs = withContext(Dispatchers.IO) {
                    YouTube.queue(listOf(videoId))
                }
                songs.onSuccess { songList ->
                    songList.firstOrNull()?.let { song ->
                        onSharedSong(song)
                    }
                }.onFailure { exception ->
                    reportException(exception)
                }
            } catch (e: Exception) {
                reportException(e)
            }
        }
    }

    /**
     * Maneja álbumes de YouTube Music. Recibe un playlistId (que empieza con OLAK5uy_)
     * y obtiene el browseId del álbum para navegar.
     */
    private fun handleYouTubeMusicAlbum(playlistId: String) {
        coroutineScope.launch {
            try {
                YouTube.albumSongs(playlistId)
                    .onSuccess { songs ->
                        songs.firstOrNull()?.album?.id?.let { browseId ->
                            navController.navigate("album/$browseId")
                        }
                    }
                    .onFailure { exception ->
                        reportException(exception)
                    }
            } catch (e: Exception) {
                reportException(e)
            }
        }
    }

    private fun reportException(e: Exception) {
        // Tu lógica para reportar errores, por ejemplo, Log.e(...)
        e.printStackTrace()
    }

    // El objeto companion se mantiene igual
    companion object {
        /**
         * Verifica si una URI es un deep link válido
         */
        fun isValidDeepLink(uri: Uri?): Boolean {
            if (uri == null) return false

            return when {
                uri.host == "youtu.be" -> true
                uri.host == "youtube.com" || uri.host == "www.youtube.com" -> {
                    val path = uri.pathSegments.firstOrNull()
                    path in listOf("watch", "playlist", "channel", "c")
                }
                uri.pathSegments.any { it in listOf("playlist", "artist", "album") } -> true
                else -> false
            }
        }

        /**
         * Extrae información básica del deep link para logging
         */
        fun getDeepLinkInfo(uri: Uri): String {
            return buildString {
                append("Host: ${uri.host}, ")
                append("Path: ${uri.path}, ")
                append("Query: ${uri.query}")
            }
        }
    }
}
