package com.zionhuang.music.ui.screens.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.R
import com.zionhuang.music.constants.AccessTokenKey
import com.zionhuang.music.constants.AutoSkipNextOnErrorKey
import com.zionhuang.music.constants.JossRedEnabledKey
import com.zionhuang.music.constants.RefreshTokenKey
import com.zionhuang.music.ui.component.IconButton
import com.zionhuang.music.ui.component.PreferenceEntry
import com.zionhuang.music.ui.component.PreferenceGroupTitle
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
    var accessToken by rememberPreference(AccessTokenKey, "")
    var refreshToken by rememberPreference(RefreshTokenKey, "")
    val (jossRedEnabled, onJossRedEnabledChange) = rememberPreference(key = JossRedEnabledKey, defaultValue = false)

    // Variables para los nuevos campos de entrada
    var manualAccessToken by remember { mutableStateOf(accessToken) }
    var manualRefreshToken by remember { mutableStateOf(refreshToken) }

    //Variables de preferencia
    val (autoSkipNextOnError, onAutoSkipNextOnErrorChange) = rememberPreference(AutoSkipNextOnErrorKey, defaultValue = false)

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState())
            .padding(16.dp) // Agregar padding alrededor de todo el contenido
    ) {
        Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)))

        Text("El sistema está en desarrollo, puedes ir preparando tus credenciales para una futura actualización.")

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
            title = { Text(stringResource(R.string.auto_skip_next_on_error)) },
            description = stringResource(R.string.auto_skip_next_on_error_desc),
            icon = { Icon(painterResource(R.drawable.skip_next), null) },
            checked = autoSkipNextOnError,
            onCheckedChange = onAutoSkipNextOnErrorChange
        )

        // Mostrar los tokens
        Text("Access Token:")
        Text(
            text = accessToken.takeIf { it.isNotEmpty() } ?: "No token available"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Refresh Token:")
        Text(
            text = refreshToken.takeIf { it.isNotEmpty() } ?: "No token available"
        )

        // Formulario para ingresar tokens manualmente
        Spacer(modifier = Modifier.height(16.dp))

        Text("Enter Access Token Manually:")
        TextField(
            value = manualAccessToken,
            onValueChange = { manualAccessToken = it },
            label = { Text("Access Token") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Enter Refresh Token Manually:")
        TextField(
            value = manualRefreshToken,
            onValueChange = { manualRefreshToken = it },
            label = { Text("Refresh Token") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para guardar los tokens
        Button(
            onClick = {
                // Guardar los tokens manualmente en las preferencias
                accessToken = manualAccessToken
                refreshToken = manualRefreshToken
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Tokens")
        }
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