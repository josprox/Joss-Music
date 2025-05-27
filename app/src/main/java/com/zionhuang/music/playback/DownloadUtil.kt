package com.zionhuang.music.playback

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.media3.database.DatabaseProvider
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.zionhuang.innertube.YouTube
import com.zionhuang.music.constants.AudioQuality
import com.zionhuang.music.constants.AudioQualityKey
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.db.entities.FormatEntity
import com.zionhuang.music.db.entities.SongEntity
import com.zionhuang.music.di.AppModule.PlayerCache
import com.zionhuang.music.di.DownloadCache
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.utils.YTPlayerUtils
import com.zionhuang.music.utils.enumPreference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadUtil @Inject constructor(
    @ApplicationContext private val context: Context,
    val database: MusicDatabase,
    val databaseProvider: DatabaseProvider,
    @DownloadCache val downloadCache: SimpleCache,
    @PlayerCache val playerCache: SimpleCache,
) {
    private val connectivityManager = context.getSystemService<ConnectivityManager>()!!
    private val audioQuality by enumPreference(context, AudioQualityKey, AudioQuality.AUTO)
    private val songUrlCache = HashMap<String, Pair<String, Long>>()
    private val dataSourceFactory = ResolvingDataSource.Factory(
        CacheDataSource.Factory()
            .setCache(playerCache)
            .setUpstreamDataSourceFactory(
                OkHttpDataSource.Factory(
                    OkHttpClient.Builder()
                        .proxy(YouTube.proxy)
                        .build()
                )
            )
    ) { dataSpec ->
        val mediaId = dataSpec.key ?: error("No media id")
        val length = if (dataSpec.length >= 0) dataSpec.length else 1

        // 1. Si ya está en cache local, no se hace nada más.
        if (playerCache.isCached(mediaId, dataSpec.position, length)) {
            return@Factory dataSpec
        }

        // La opción 2 está deshabilitado ya que ahora no usaremos cache de las canciones, evitamos problemas de desofuscación.
        // 2. Si está en el songUrlCache y no ha expirado, usamos esa URL.
//        songUrlCache[mediaId]?.takeIf { it.second > System.currentTimeMillis() }?.let {
//            return@Factory dataSpec.withUri(it.first.toUri())
//        }

        var lastError: Exception? = null

        repeat(3) retry@{ attempt ->
            try {
                // 3. Obtener formato reproducido previamente (desde BD)
                val playedFormat = runBlocking(Dispatchers.IO) { database.format(mediaId).first() }

                // 4. Pedir nueva URL con lógica actual
                val playbackData = runBlocking(Dispatchers.IO) {
                    YTPlayerUtils.playerResponseForPlayback(
                        mediaId,
                        audioQuality = audioQuality,
                        connectivityManager = connectivityManager,
                    )
                }.getOrThrow()

                val format = playbackData.format

                // Guardar formato actualizado en la base de datos
                database.query {
                    upsert(
                        FormatEntity(
                            id = mediaId,
                            itag = format.itag,
                            mimeType = format.mimeType.split(";")[0],
                            codecs = format.mimeType.split("codecs=")[1].removeSurrounding("\""),
                            bitrate = format.bitrate,
                            sampleRate = format.audioSampleRate,
                            contentLength = format.contentLength!!,
                            loudnessDb = playbackData.audioConfig?.loudnessDb,
                        )
                    )
                }

                // Generar la URL final con rango forzado
                val streamUrl = playbackData.streamUrl.let {
                    "${it}&range=0-${format.contentLength ?: 10000000}"
                }

                // Cachear la URL hasta que expire
                songUrlCache[mediaId] = streamUrl to System.currentTimeMillis() + (playbackData.streamExpiresInSeconds * 1000L)

                // Retornar nuevo DataSpec con la URL resuelta
                return@Factory dataSpec.withUri(streamUrl.toUri())

            } catch (e: Exception) {
                lastError = e

                // Si no es error 403, no tiene sentido reintentar
                val is403 = e.message?.contains("403") == true || e.message?.contains("Forbidden") == true
                if (!is403) return@Factory dataSpec // usar el original (fallará, pero se verá en logs)
            }
        }

        // Si se llegó aquí, falló 3 veces: regresar el DataSpec original sin tocar nada (se maneja como error en otro lado si aplica)
        return@Factory dataSpec
    }

    val downloadNotificationHelper = DownloadNotificationHelper(context, ExoDownloadService.CHANNEL_ID)
    val downloadManager: DownloadManager = DownloadManager(context, databaseProvider, downloadCache, dataSourceFactory, Executor(Runnable::run)).apply {
        maxParallelDownloads = 3
        addListener(
            ExoDownloadService.TerminalStateNotificationHelper(
                context = context,
                notificationHelper = downloadNotificationHelper,
                nextNotificationId = ExoDownloadService.NOTIFICATION_ID + 1
            )
        )
    }
    val downloads = MutableStateFlow<Map<String, Download>>(emptyMap())


    fun getDownload(songId: String): Flow<Download?> = downloads.map { it[songId] }

    fun download(songs: List<MediaMetadata>) {
        songs.forEach { song -> downloadSong(song.id, song.title) }
    }

    fun download(song: MediaMetadata) {
        downloadSong(song.id, song.title)
    }

    fun download(song: SongEntity) {
        downloadSong(song.id, song.title)
    }

    private fun downloadSong(id: String, title: String) {
        val downloadRequest = DownloadRequest.Builder(id, id.toUri())
            .setCustomCacheKey(id)
            .setData(title.toByteArray())
            .build()
        DownloadService.sendAddDownload(
            context,
            ExoDownloadService::class.java,
            downloadRequest,
            false)
    }


    init {
        val result = mutableMapOf<String, Download>()
        val cursor = downloadManager.downloadIndex.getDownloads()
        while (cursor.moveToNext()) {
            result[cursor.download.request.id] = cursor.download
        }
        downloads.value = result
        downloadManager.addListener(
            object : DownloadManager.Listener {
                override fun onDownloadChanged(downloadManager: DownloadManager, download: Download, finalException: Exception?) {
                    downloads.update { map ->
                        map.toMutableMap().apply {
                            set(download.request.id, download)
                        }
                    }
                }
            }
        )
    }
}