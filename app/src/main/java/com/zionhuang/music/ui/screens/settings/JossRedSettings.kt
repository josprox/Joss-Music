package com.zionhuang.music.ui.screens.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.R
import com.zionhuang.music.constants.AutoSkipNextOnErrorKey
import com.zionhuang.music.constants.JossRedEnabledKey
import com.zionhuang.music.constants.JossRedMultimedia
import com.zionhuang.music.ui.component.IconButton
import com.zionhuang.music.ui.component.PreferenceEntry
import com.zionhuang.music.ui.component.SwitchPreference
import com.zionhuang.music.ui.utils.backToMain
import com.zionhuang.music.utils.rememberPreference

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JossRedSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    // Configuración próxima de Joss Red
    val (jossRedEnabled, onJossRedEnabledChange) = rememberPreference(key = JossRedEnabledKey, defaultValue = false)
    val (jossRedMultimedia, onJossRedMultimediaChange) = rememberPreference(key = JossRedMultimedia, defaultValue = false)

    //Variables de preferencia
    val (autoSkipNextOnError, onAutoSkipNextOnErrorChange) = rememberPreference(AutoSkipNextOnErrorKey, defaultValue = false)

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState())
            .padding(16.dp) // Agregar padding alrededor de todo el contenido
    ) {
        Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)))

        Text(stringResource(R.string.jossredSettings_welcome))

        Spacer(modifier = Modifier.height(8.dp))

        PreferenceEntry(
            title = { Text(stringResource(R.string.login)) },
            icon = { Icon(painterResource(R.drawable.person), null) },
            onClick = { navController.navigate("loginv2") }
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.enable_proxy)+" Joss Red") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.joss_music_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp) // Ajusta el tamaño a 24 dp
                        .clip(CircleShape) // Aplica la forma circular
                        .background(MaterialTheme.colorScheme.surfaceContainer) // Fondo
                )
            },
            checked = jossRedEnabled,
            onCheckedChange = onJossRedEnabledChange
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.playSongJR)) },
            description = stringResource(R.string.playSongJRDesc),
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.music_note),
                    contentDescription = null
                )
            },
            checked = jossRedMultimedia,
            onCheckedChange = onJossRedMultimediaChange
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.auto_skip_next_on_error)) },
            description = stringResource(R.string.auto_skip_next_on_error_desc),
            icon = { Icon(painterResource(R.drawable.skip_next), null) },
            checked = autoSkipNextOnError,
            onCheckedChange = onAutoSkipNextOnErrorChange
        )


    }

    TopAppBar(
        title = { Text(stringResource(R.string.content)+" Joss Red") },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}
