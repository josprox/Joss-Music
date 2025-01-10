package com.zionhuang.music.ui.screens.settings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zionhuang.music.BuildConfig
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.R
import com.zionhuang.music.ui.component.IconButton
import com.zionhuang.music.ui.utils.backToMain
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.* // Para Column, Row, Spacer, etc.
import androidx.compose.foundation.verticalScroll // Para el scroll
import androidx.compose.foundation.rememberScrollState // Para recordar el estado del scroll
import androidx.compose.material3.* // Para Material 3 components como Button, Text
import androidx.compose.ui.graphics.Color // Para usar colores
import androidx.compose.ui.unit.dp // Para manejar el padding, tamaño, etc.
import androidx.compose.ui.unit.sp // Para manejar el tamaño de fuente
import androidx.compose.ui.text.style.TextAlign // Para la alineación del texto
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val uriHandler = LocalUriHandler.current

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
                .size(100.dp) // Ajusta el tamaño de la imagen aquí
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .clickable { }
        )

        Row(
            verticalAlignment = Alignment.Top,
        ) {
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
                    .padding(
                        horizontal = 6.dp,
                        vertical = 2.dp
                    )
            )

            Spacer(Modifier.width(4.dp))

            Text(
                text = BuildConfig.FLAVOR.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    )
                    .padding(
                        horizontal = 6.dp,
                        vertical = 2.dp
                    )
            )

            if (BuildConfig.DEBUG) {
                Spacer(Modifier.width(4.dp))

                Text(
                    text = "Modo prueba",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.secondary,
                            shape = CircleShape
                        )
                        .padding(
                            horizontal = 6.dp,
                            vertical = 2.dp
                        )
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Una aplicación de JOSPROX MX",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(8.dp))

        Row {
            IconButton(
                onClick = { uriHandler.openUri("https://github.com/josprox/Joss-Music") }
            ) {
                Icon(
                    painter = painterResource(R.drawable.joss_music_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .padding(
                            horizontal = 6.dp,
                            vertical = 2.dp
                        )
                )
            }

            IconButton(
                onClick = { uriHandler.openUri("https://github.com/josprox/") }
            ) {
                Icon(
                    painter = painterResource(R.drawable.github),
                    contentDescription = null
                )
            }

            IconButton(
                onClick = { uriHandler.openUri("https://www.facebook.com/Josproxmx") }
            ) {
                Icon(
                    painter = painterResource(R.drawable.facebook),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(
                            horizontal = 6.dp,
                            vertical = 2.dp
                        )
                )
            }

            IconButton(
                onClick = { uriHandler.openUri("https://play.google.com/store/apps/dev?id=8312669195856231840") }
            ) {
                Icon(
                    painter = painterResource(R.drawable.google_play),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(
                            horizontal = 6.dp,
                            vertical = 2.dp
                        )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Descripción de la Aplicación
        Text(
            text = "Esta aplicación no recopila información del usuario. Los datos que proporciones se almacenarán en una base de datos la cuál puedes descargar. Además, al instalar esta aplicación, podrías recibir notificaciones push de nuestros servicios (Joss Red). Nuestros términos y servicios están disponibles en nuestra web oficial.",
            fontSize = 14.sp,
            modifier = Modifier.padding(10.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Soporte
        Text(
            text = "Soporte",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 10.dp)
        )
        Button(
            onClick = {
                uriHandler.openUri("https://josprox.com/soporte/")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(text = "Página de soporte")
        }

        // Web del Creador
        Text(
            text = "Web del Creador",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 10.dp)
        )
        Button(
            onClick = {
                uriHandler.openUri("https://josprox.com/")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(text = "Ir a JOSPROX | Joss Estrada")
        }

        // Aviso Importante
        Text(
            text = "Aviso Importante",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red,
            modifier = Modifier.padding(top = 10.dp)
        )
        Text(
            text = "Se muestran a continuación la política de privacidad y los términos y condiciones de las aplicaciones dentro de 'Joss Red'. Esta aplicación puede contener compras internas gestionadas en la web JOSPROX MX y MHT. Toda la información será exclusivamente la que tú insertes. Las herramientas de la aplicación no compartirán tus datos con terceros.",
            fontSize = 14.sp,
            modifier = Modifier.padding(10.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Política de Privacidad
        Text(
            text = "Política de Privacidad",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 10.dp)
        )
        Button(
            onClick = {
                uriHandler.openUri("https://josprox.com/privacidad/")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(text = "Política de Privacidad")
        }

        // Términos y Condiciones
        Text(
            text = "Términos y Condiciones",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 10.dp)
        )
        Button(
            onClick = {
                uriHandler.openUri("https://josprox.com/terminos-y-condiciones/")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(text = "Términos y Condiciones")
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Principal modificación desde Zion Huang",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(8.dp))

        Row {
            IconButton(
                onClick = { uriHandler.openUri("https://liberapay.com/zionhuang") }
            ) {
                Icon(
                    painter = painterResource(R.drawable.liberapay),
                    contentDescription = null
                )
            }

            IconButton(
                onClick = { uriHandler.openUri("https://www.buymeacoffee.com/zionhuang") }
            ) {
                Icon(
                    painter = painterResource(R.drawable.buymeacoffee),
                    contentDescription = null
                )
            }
        }

        Text(
            text = "Código nativo de ViMusic",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(8.dp))

        IconButton(
            onClick = { uriHandler.openUri("https://github.com/vfsfitvnm/ViMusic") }
        ) {
            Icon(
                painter = painterResource(R.drawable.github),
                contentDescription = null
            )
        }

        Text(
            text = "Esta es una aplicación con licencia GPL 3.0, no busca beneficiarse de ningún proyecto abierto, en caso de alguna violación, ponerse en contacto con soporte.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )

    }

    TopAppBar(
        title = { Text(stringResource(R.string.about)) },
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
