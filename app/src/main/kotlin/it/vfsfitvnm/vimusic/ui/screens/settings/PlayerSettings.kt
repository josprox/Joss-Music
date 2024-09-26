package it.vfsfitvnm.vimusic.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import it.vfsfitvnm.vimusic.LocalPlayerAwareWindowInsets
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.ui.components.themed.Header
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.isAtLeastAndroid6
import it.vfsfitvnm.vimusic.utils.persistentQueueKey
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.resumePlaybackWhenDeviceConnectedKey
import it.vfsfitvnm.vimusic.utils.skipSilenceKey
import it.vfsfitvnm.vimusic.utils.toast
import it.vfsfitvnm.vimusic.utils.volumeNormalizationKey

@ExperimentalAnimationApi
@Composable
fun PlayerSettings() {
    val context = LocalContext.current
    val (colorPalette) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current

    var persistentQueue by rememberPreference(persistentQueueKey, false)
    var resumePlaybackWhenDeviceConnected by rememberPreference(
        resumePlaybackWhenDeviceConnectedKey,
        false
    )
    var skipSilence by rememberPreference(skipSilenceKey, false)
    var volumeNormalization by rememberPreference(volumeNormalizationKey, false)

    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    Column(
        modifier = Modifier
            .background(colorPalette.background0)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                    .asPaddingValues()
            )
    ) {
        Header(title = "Reproducción y audio")

        SettingsEntryGroupText(title = "Reproducción")

        SwitchSettingEntry(
            title = "Guardar cola",
            text = "Guardar y restaurar canciones en reproducción",
            isChecked = persistentQueue,
            onCheckedChange = {
                persistentQueue = it
            }
        )

        if (isAtLeastAndroid6) {
            SwitchSettingEntry(
                title = "Reanudar la reproducción",
                text = "Cuando se conecta un dispositivo con cable o bluetooth",
                isChecked = resumePlaybackWhenDeviceConnected,
                onCheckedChange = {
                    resumePlaybackWhenDeviceConnected = it
                }
            )
        }

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = "AUDIO")

        SwitchSettingEntry(
            title = "Silencio",
            text = "Omitir partes silenciosas durante la reproducción",
            isChecked = skipSilence,
            onCheckedChange = {
                skipSilence = it
            }
        )

        SwitchSettingEntry(
            title = "Normalización de volumen",
            text = "Ajuste el volumen a un nivel fijo",
            isChecked = volumeNormalization,
            onCheckedChange = {
                volumeNormalization = it
            }
        )

        SettingsEntry(
            title = "Ecualizador",
            text = "Interactuar con el ecualizador del sistema",
            onClick = {
                val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                    putExtra(AudioEffect.EXTRA_AUDIO_SESSION, binder?.player?.audioSessionId)
                    putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                    putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                }

                try {
                    activityResultLauncher.launch(intent)
                } catch (e: ActivityNotFoundException) {
                    context.toast("No pude encontrar una aplicación para ecualizar el audio")
                }
            }
        )
    }
}
