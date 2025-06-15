package com.zionhuang.music.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.R
import com.zionhuang.music.utils.UpdateMainViewModel
import com.zionhuang.music.ui.component.IconButton
import com.zionhuang.music.ui.component.PreferenceEntry
import com.zionhuang.music.ui.utils.backToMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: UpdateMainViewModel,
) {
    val uriHandler = LocalUriHandler.current

    val showUpdate by viewModel.showUpdateBadge.collectAsState()
    val latestVersion by viewModel.latestVersionName.collectAsState()
    val currentVersion by viewModel.currentVersionName.collectAsState()

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)))

        PreferenceEntry(
            title = { Text(stringResource(R.string.appearance)) },
            icon = { Icon(painterResource(R.drawable.palette), null) },
            onClick = { navController.navigate("settings/appearance") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.content)) },
            icon = { Icon(painterResource(R.drawable.language), null) },
            onClick = { navController.navigate("settings/content") }
        )
        PreferenceEntry(
            title = { Text("Joss Red") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.joss_music_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                )
            },
            onClick = { navController.navigate("JossRedSettings") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.player_and_audio)) },
            icon = { Icon(painterResource(R.drawable.play), null) },
            onClick = { navController.navigate("settings/player") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.storage)) },
            icon = { Icon(painterResource(R.drawable.storage), null) },
            onClick = { navController.navigate("settings/storage") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.privacy)) },
            icon = { Icon(painterResource(R.drawable.security), null) },
            onClick = { navController.navigate("settings/privacy") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.discord_integration)) },
            icon = { Icon(painterResource(R.drawable.discord), null) },
            onClick = { navController.navigate("settings/discord") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.backup_restore)) },
            icon = { Icon(painterResource(R.drawable.restore), null) },
            onClick = { navController.navigate("settings/backup_restore") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.about)) },
            icon = { Icon(painterResource(R.drawable.info), null) },
            onClick = { navController.navigate("settings/about") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.donate)) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.joss_music_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                )
            },
            onClick = { uriHandler.openUri("https://www.paypal.me/jossestradamx") },
            modifier = Modifier
                .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape) // Borde)
        )

        if (showUpdate && latestVersion != null) {
            PreferenceEntry(
                title = {
                    Text(text = stringResource(R.string.new_version_available))
                },
                description = stringResource(R.string.current) + ": $currentVersion  →  "+ stringResource(R.string.newString) + ": $latestVersion",
                icon = {
                    BadgedBox(
                        badge = { Badge() }
                    ) {
                        Icon(painterResource(R.drawable.update), null)
                    }
                },
                onClick = { navController.navigate("settings/update") }
            )
        } else {
            PreferenceEntry(
                title = {
                    Text(text = stringResource(R.string.app_version))
                },
                description = stringResource(R.string.latestVersion) + ": $currentVersion",
                onClick = { navController.navigate("settings/update") }
            )
        }
    }

    TopAppBar(
        title = { Text(stringResource(R.string.settings)) },
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