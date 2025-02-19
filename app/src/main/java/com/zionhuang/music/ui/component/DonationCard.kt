package com.zionhuang.music.ui.component

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zionhuang.music.R
import java.util.Calendar

@Composable
fun DonationCardWithTiming(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("donation_card", Context.MODE_PRIVATE)
    val lastShownTimestamp = sharedPreferences.getLong("last_shown", 0L)

    val currentTime = Calendar.getInstance().timeInMillis
    val twoDaysInMillis = 2 * 24 * 60 * 60 * 1000L // 2 days in milliseconds

    val showDonationCard = currentTime - lastShownTimestamp >= twoDaysInMillis

    // Mostrar la tarjeta solo si debe mostrarse
    if (showDonationCard) {
        DonationCard(modifier = modifier)

        // Guardar la fecha actual como la última vez que se mostró la tarjeta
        with(sharedPreferences.edit()) {
            putLong("last_shown", currentTime)
            apply()
        }
    }
}

@Composable
fun DonationCard(modifier: Modifier = Modifier) {
    // Estado local para controlar la visibilidad de la tarjeta
    var isVisible by remember { mutableStateOf(true) }

    val uriHandler = LocalUriHandler.current

    // Mostrar la tarjeta solo si isVisible es true
    if (isVisible) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.supportApp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.card_donation),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Fila para los botones
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly, // Espacia los botones uniformemente
                    modifier = Modifier.fillMaxWidth() // Ocupar el ancho completo
                ) {
                    Button(
                        onClick = { uriHandler.openUri("https://www.paypal.me/jossestradamx") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f) // Hace que el botón ocupe espacio
                    ) {
                        Text(text = stringResource(R.string.donate), color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp)) // Espacio entre los botones
                    Button(
                        onClick = { isVisible = false }, // Cierra la tarjeta
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f) // Hace que el botón ocupe espacio
                    ) {
                        Text(text = stringResource(R.string.close), color = Color.White) // Texto del botón de cerrar
                    }
                }
            }
        }
    }
}

