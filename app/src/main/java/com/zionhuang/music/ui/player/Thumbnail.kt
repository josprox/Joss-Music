package com.zionhuang.music.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.constants.PlayerHorizontalPadding
import com.zionhuang.music.constants.ShowLyricsKey
import com.zionhuang.music.constants.ThumbnailCornerRadius
import com.zionhuang.music.ui.component.Lyrics
import com.zionhuang.music.utils.rememberPreference

@Composable
fun Thumbnail(
    sliderPositionProvider: () -> Long?,
    modifier: Modifier = Modifier,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val currentView = LocalView.current

    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val error by playerConnection.error.collectAsState()

    val showLyrics by rememberPreference(ShowLyricsKey, false)
    var dragAmount by remember { mutableStateOf(0f) }

    DisposableEffect(showLyrics) {
        currentView.keepScreenOn = showLyrics
        onDispose {
            currentView.keepScreenOn = false
        }
    }

    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = !showLyrics && error == null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = PlayerHorizontalPadding)
            ) {
                AsyncImage(
                    model = mediaMetadata?.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(ThumbnailCornerRadius * 2))
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    // Detectar la dirección del deslizamiento al finalizar
                                    if (dragAmount < 0) {
                                        // Deslizar a la izquierda: ir a la siguiente canción
                                        playerConnection.seekToNext()
                                    } else if (dragAmount > 0) {
                                        // Deslizar a la derecha: ir a la canción anterior
                                        playerConnection.seekToPrevious()
                                    }
                                    // Reiniciar el valor de dragAmount para el próximo gesto
                                    dragAmount = 0f
                                },
                                onHorizontalDrag = { change, dragDelta ->
                                    // Acumular la cantidad de desplazamiento
                                    dragAmount += dragDelta
                                }
                            )
                        }
                )
            }
        }

        AnimatedVisibility(
            visible = showLyrics && error == null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Lyrics(sliderPositionProvider = sliderPositionProvider)
        }

        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .padding(32.dp)
                .align(Alignment.Center)
        ) {
            error?.let { error ->
                PlaybackError(
                    error = error,
                    retry = playerConnection.player::prepare
                )
            }
        }
    }
}
