package com.zionhuang.music.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.stringResource
import com.zionhuang.music.R

@Composable
fun NotificationPermissionScreen(
    context: Context? = null,
    onPermissionGranted: (() -> Unit)? = null,
    onBackPressed: () -> Unit // Agregamos un parámetro para manejar el regreso
) {
    var notificationsEnabled by remember { mutableStateOf(false) }

    // Verificamos si las notificaciones están habilitadas
    LaunchedEffect(Unit) {
        notificationsEnabled = context?.let { areNotificationsEnabled(it) } ?: false
    }

    // Si las notificaciones están habilitadas, mostramos el contenido
    if (notificationsEnabled) {
        // Mostrar el contenido de la pantalla si las notificaciones están habilitadas
        ContentScreen(onBackPressed = onBackPressed) // Pasamos la lógica de regreso aquí
    } else {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.enaNotifications),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.enaNotificationsText),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        context?.let {
                            openNotificationSettings(it)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.goToSettings),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ContentScreen(onBackPressed: () -> Unit) {
    // Este es el contenido que se muestra después de que el usuario haya habilitado las notificaciones
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Notifications Enabled",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.enaNotificationsTextTrue),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Añadiendo un botón para simular el regreso
            Button(
                onClick = onBackPressed, // Llamamos a la función onBackPressed cuando el usuario hace clic
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.back),
                    color = Color.White
                )
            }
        }

        // Manejo de regreso con el botón físico del dispositivo
        BackHandler {
            onBackPressed() // Llamamos a onBackPressed cuando el usuario presiona el botón de regreso físico
        }
    }
}

private fun areNotificationsEnabled(context: Context): Boolean {
    return NotificationManagerCompat.from(context).areNotificationsEnabled()
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        data = Uri.parse("package:${context.packageName}")
    }

    // Verificar si la actividad está disponible antes de lanzarla
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        // En caso de que no haya una actividad que pueda manejar la intención, podrías mostrar un mensaje de error
        // o intentar redirigir a una acción alternativa.
        Toast.makeText(context, "Unable to access app information", Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNotificationPermissionScreen() {
    // Usamos un contexto nulo aquí, solo para fines de preview
    NotificationPermissionScreen(onBackPressed = {})
}
