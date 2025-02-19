package com.zionhuang.music.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zionhuang.music.BuildConfig
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.R
import com.zionhuang.music.utils.Updater
import kotlinx.coroutines.launch
import org.dotenv.vault.dotenvVault

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataUpdate(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    latestVersionName: String,
) {
    val dotenv = dotenvVault(BuildConfig.DOTENV_KEY) {
        directory = "/assets"
        filename = "env.vault" // instead of '.env', use 'env'
    }
    val homePage_web: String = dotenv["HOMEPAGE"]

    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val releaseDetails = remember { mutableStateOf<Updater.ReleaseDetails?>(null) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val result = Updater.getLatestReleaseDetails()
            result.onSuccess { details ->
                releaseDetails.value = details
            }.onFailure { error ->
                errorMessage.value = error.localizedMessage
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)))
        Spacer(Modifier.height(4.dp))

        Image(
            painter = painterResource(R.drawable.joss_music_logo),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground, BlendMode.SrcIn),
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .clickable { }
        )

        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = "Joss Music",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Google Play",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        releaseDetails.value?.let { details ->
            Text(
                text = stringResource(R.string.latestVersion)+": ${details.version}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = details.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(16.dp))

            if (latestVersionName > BuildConfig.VERSION_NAME) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(details.downloadUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(text = stringResource(R.string.new_version_available))
                }
            }else{
                Button(
                    onClick = {
                        uriHandler.openUri(homePage_web)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(text = stringResource(R.string.website_url))
                }
            }
        } ?: errorMessage.value?.let { error ->
            Text(
                text = "Error al obtener la información: $error",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }

    TopAppBar(
        title = { Text(stringResource(R.string.latestVersion)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp
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
