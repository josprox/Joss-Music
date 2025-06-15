package com.zionhuang.music.ui.screens.settings

import android.content.Intent
import android.text.Spanned
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.zionhuang.music.BuildConfig
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.R
import com.zionhuang.music.utils.UpdateMainViewModel
import com.zionhuang.music.utils.Updater
import io.noties.markwon.Markwon
import kotlinx.coroutines.launch
import org.dotenv.vault.dotenvVault

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataUpdate(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: UpdateMainViewModel
) {
    val dotenv = dotenvVault(BuildConfig.DOTENV_KEY) {
        directory = "/assets"
        filename = "env.vault"
    }
    val homePageWeb: String = dotenv["HOMEPAGE"]

    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val showUpdate by viewModel.showUpdateBadge.collectAsState()
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
            colorFilter = ColorFilter.tint(colorScheme.onBackground, BlendMode.SrcIn),
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(colorScheme.surfaceContainer)
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
                color = colorScheme.secondary,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = colorScheme.secondary,
                        shape = CircleShape
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        releaseDetails.value?.let { details ->
            Text(
                text = stringResource(R.string.latestVersion) + ": ${details.version}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(8.dp))

            val imageUrls = remember(details.description) { extractImageUrls(details.description) }
            val markwon = Markwon.create(context)
            val spanned = markwon.toMarkdown(details.description)
            val markdownText = spannedToAnnotatedString(spanned)

            // Mostrar Markdown como texto
            Text(
                text = markdownText,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(16.dp))

            // Mostrar imágenes extraídas del Markdown
            imageUrls.forEach { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(16.dp))
            }

            if (showUpdate) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, details.downloadUrl.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(text = stringResource(R.string.new_version_available))
                }
            } else {
                Button(
                    onClick = {
                        uriHandler.openUri(homePageWeb)
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
                color = colorScheme.error,
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

// Función para extraer URLs de imágenes del Markdown
fun extractImageUrls(markdown: String): List<String> {
    val regex = Regex("""!\[.*?]\((.*?)\)""")
    return regex.findAll(markdown).map { it.groupValues[1] }.toList()
}

// Función para convertir Spanned a AnnotatedString
fun spannedToAnnotatedString(spanned: Spanned): AnnotatedString {
    return buildAnnotatedString {
        append(spanned.toString())
    }
}
