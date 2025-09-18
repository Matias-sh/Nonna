package com.cocido.nonna.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la configuración de la aplicación
 * Maneja preferencias de usuario y configuración de cuenta
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                sharedPreferences.edit()
                    .putBoolean("dark_mode", enabled)
                    .apply()
                
                val currentState = _uiState.value
                if (currentState is SettingsUiState.Success) {
                    _uiState.value = currentState.copy(isDarkMode = enabled)
                }
                
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(
                    message = "Error al cambiar el modo oscuro: ${e.message}"
                )
            }
        }
    }
    
    fun setNostalgicRoom(enabled: Boolean) {
        viewModelScope.launch {
            try {
                sharedPreferences.edit()
                    .putBoolean("nostalgic_room", enabled)
                    .apply()
                
                val currentState = _uiState.value
                if (currentState is SettingsUiState.Success) {
                    _uiState.value = currentState.copy(isNostalgicRoom = enabled)
                }
                
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(
                    message = "Error al cambiar la habitación nostálgica: ${e.message}"
                )
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            try {
                // TODO: Implementar logout real
                // authRepository.logout()
                // clearUserData()
                
                _uiState.value = SettingsUiState.LogoutSuccess
                
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(
                    message = "Error al cerrar sesión: ${e.message}"
                )
            }
        }
    }
    
    fun exportData() {
        viewModelScope.launch {
            try {
                // TODO: Implementar exportación de datos
                // val exportData = dataRepository.exportAllData()
                // fileManager.saveExportFile(exportData)
                
                _uiState.value = SettingsUiState.Success(
                    userName = "Usuario",
                    userEmail = "usuario@ejemplo.com",
                    isDarkMode = sharedPreferences.getBoolean("dark_mode", false),
                    isNostalgicRoom = sharedPreferences.getBoolean("nostalgic_room", false)
                )
                
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(
                    message = "Error al exportar datos: ${e.message}"
                )
            }
        }
    }
    
    fun createBackup() {
        viewModelScope.launch {
            try {
                // TODO: Implementar creación de respaldo
                // backupManager.createBackup()
                
                _uiState.value = SettingsUiState.Success(
                    userName = "Usuario",
                    userEmail = "usuario@ejemplo.com",
                    isDarkMode = sharedPreferences.getBoolean("dark_mode", false),
                    isNostalgicRoom = sharedPreferences.getBoolean("nostalgic_room", false)
                )
                
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(
                    message = "Error al crear respaldo: ${e.message}"
                )
            }
        }
    }
}

/**
 * Estados de la UI para configuración
 */
sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Success(
        val userName: String,
        val userEmail: String,
        val isDarkMode: Boolean,
        val isNostalgicRoom: Boolean
    ) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
    object LogoutSuccess : SettingsUiState()
}


