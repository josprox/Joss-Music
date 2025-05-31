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

class DeepLinkHandler(
    private val navController: NavController,
    private val coroutineScope: CoroutineScope
) {

    /**
     * Maneja los deep links iniciales cuando se abre la aplicación
     */
    fun handleInitialDeepLink(intent: Intent?) {
        intent?.data?.let { uri ->
            handleDeepLinkUri(uri)
        }
    }

    /**
     * Maneja los deep links cuando la aplicación ya está abierta
     */
    fun handleNewIntent(intent: Intent, onSharedSong: (SongItem) -> Unit) {
        val uri = intent.data ?: intent.extras?.getString(Intent.EXTRA_TEXT)?.toUri() ?: return

        when (val path = uri.pathSegments.firstOrNull()) {
            "playlist" -> handlePlaylistLink(uri)
            "channel", "c" -> handleChannelLink(uri)
            else -> handleVideoLink(uri, path, onSharedSong)
        }
    }

    /**
     * Maneja URIs de deep links
     */
    private fun handleDeepLinkUri(uri: Uri) {
        val pathSegments = uri.pathSegments
        when {
            pathSegments.contains("playlist") -> {
                val playlistId = pathSegments.lastOrNull()
                playlistId?.let {
                    navController.navigate("online_playlist/$it")
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
            uri.host == "youtu.be" -> path
            else -> null
        }

        videoId?.let { id ->
            coroutineScope.launch {
                try {
                    val songs = withContext(Dispatchers.IO) {
                        YouTube.queue(listOf(id))
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
    }

    /**
     * Maneja álbumes de YouTube Music (playlist con OLAK5uy_)
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