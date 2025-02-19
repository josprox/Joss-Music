package com.zionhuang.music.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.zionhuang.music.ui.screens.NotificationPermissionScreen

class NotificationPermissionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotificationPermissionScreen(
                context = this,
                onPermissionGranted = {
                    // Aquí puedes cerrar esta actividad cuando el permiso se conceda
                    finish()
                },
                onBackPressed = {
                    // Aquí puedes manejar el regreso si es necesario
                    finish()
                }
            )
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, NotificationPermissionActivity::class.java)
        }
    }
}