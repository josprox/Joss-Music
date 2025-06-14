package com.zionhuang.music.utils

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Archivo: UpdateMainViewModel.kt

class UpdateMainViewModel(private val application: Application) : ViewModel() {

    // Estado para mostrar/ocultar el badge (sin cambios)
    private val _showUpdateBadge = MutableStateFlow(false)
    val showUpdateBadge = _showUpdateBadge.asStateFlow()

    // +++ NUEVO ESTADO PARA EXPONER LA VERSIÓN +++
    // Así otras partes de la UI pueden acceder al nombre de la versión más reciente.
    private val _latestVersionName = MutableStateFlow<String?>(null)
    val latestVersionName = _latestVersionName.asStateFlow()

    init {
        checkForUpdates()
    }

    private fun checkForUpdates() {
        viewModelScope.launch {
            val latestVersionResult = Updater.getLatestVersionName()

            latestVersionResult.onSuccess { fetchedVersionName ->
                // Actualiza el nuevo estado con la versión obtenida
                _latestVersionName.value = fetchedVersionName

                val currentVersionName = try {
                    application.packageManager.getPackageInfo(application.packageName, 0).versionName
                } catch (e: PackageManager.NameNotFoundException) { null }

                // La lógica del badge sigue igual
                if (currentVersionName != null && fetchedVersionName > currentVersionName) {
                    _showUpdateBadge.value = true
                }

            }.onFailure {
                it.printStackTrace()
                _showUpdateBadge.value = false
            }
        }
    }
}

class UpdateMainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UpdateMainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UpdateMainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}