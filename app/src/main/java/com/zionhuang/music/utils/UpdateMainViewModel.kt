package com.zionhuang.music.utils

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpdateMainViewModel(private val application: Application) : ViewModel() {

    // Estado para mostrar/ocultar el badge (sin cambios)
    private val _showUpdateBadge = MutableStateFlow(false)
    val showUpdateBadge = _showUpdateBadge.asStateFlow()

    private val _latestVersionName = MutableStateFlow<String?>(null)
    val latestVersionName = _latestVersionName.asStateFlow()

    // +++ NUEVO ESTADO PARA LA VERSIÓN ACTUAL +++
    // Guardará la versión de la app instalada (ej: "1.2.3")
    private val _currentVersionName = MutableStateFlow("")
    val currentVersionName = _currentVersionName.asStateFlow()

    init {
        checkForUpdates()
    }

    private fun checkForUpdates() {
        viewModelScope.launch {
            // Obtener la versión más reciente del servidor
            val latestVersionResult = Updater.getLatestVersionName()

            // Obtener la versión actual de la app instalada
            val currentVersion = try {
                application.packageManager.getPackageInfo(application.packageName, 0).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                "" // Fallback en caso de error
            }
            // ++ ACTUALIZAR EL NUEVO ESTADO ++
            if (currentVersion != null) {
                _currentVersionName.value = currentVersion
            }

            // La lógica de comparación sigue igual
            latestVersionResult.onSuccess { fetchedVersionName ->
                _latestVersionName.value = fetchedVersionName
                if (currentVersion != null) {
                    if (currentVersion.isNotEmpty() && fetchedVersionName > currentVersion.toString()) {
                        _showUpdateBadge.value = true
                    }
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