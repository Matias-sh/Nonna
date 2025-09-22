package com.cocido.nonna.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.domain.usecase.CheckAuthStatusUseCase
import com.cocido.nonna.domain.usecase.GetCurrentUserUseCase
import com.cocido.nonna.domain.usecase.LogoutUseCase
import com.cocido.nonna.domain.usecase.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val checkAuthStatusUseCase: CheckAuthStatusUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            
            try {
                // Verificar estado de autenticación
                val isAuthValid = checkAuthStatusUseCase()
                if (!isAuthValid) {
                    _uiState.value = ProfileUiState.Error("Sesión expirada. Por favor, inicia sesión nuevamente.")
                    return@launch
                }
                
                val user = getCurrentUserUseCase()
                if (user != null) {
                    _uiState.value = ProfileUiState.Success(user)
                } else {
                    _uiState.value = ProfileUiState.Error("No se pudo cargar la información del usuario")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Error al cargar perfil: ${e.message}")
            }
        }
    }

    fun updateProfile(name: String, email: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            
            try {
                updateUserProfileUseCase(name, null)
                _uiState.value = ProfileUiState.Success(getCurrentUserUseCase()!!)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Error al actualizar perfil: ${e.message}")
            }
        }
    }

    fun updateProfileImage(imageUri: String) {
        viewModelScope.launch {
            try {
                val currentUser = getCurrentUserUseCase()
                if (currentUser != null) {
                    updateUserProfileUseCase(currentUser.name, imageUri)
                    _uiState.value = ProfileUiState.Success(getCurrentUserUseCase()!!)
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Error al actualizar foto: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                logoutUseCase()
                _uiState.value = ProfileUiState.LoggedOut
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Error al cerrar sesión: ${e.message}")
            }
        }
    }
}

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val user: com.cocido.nonna.data.remote.dto.UserDto) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
    object LoggedOut : ProfileUiState()
}
