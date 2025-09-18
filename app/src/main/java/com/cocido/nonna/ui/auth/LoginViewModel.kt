package com.cocido.nonna.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.remote.api.AuthApiService
import com.cocido.nonna.data.remote.dto.LoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para el login de usuarios
 * Maneja la lógica de autenticación y estados de la UI
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authApiService: AuthApiService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            
            try {
                // Validar campos
                if (email.isBlank() || password.isBlank()) {
                    _uiState.value = LoginUiState.Error("Por favor completa todos los campos")
                    return@launch
                }
                
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    _uiState.value = LoginUiState.Error("Por favor ingresa un email válido")
                    return@launch
                }
                
                // Realizar login
                val loginRequest = LoginRequest(email = email, password = password)
                val authResponse = authApiService.login(loginRequest)
                
                // TODO: Guardar tokens en SharedPreferences o DataStore
                // authInterceptor.saveAuthToken(authResponse.accessToken, authResponse.expiresIn)
                // authInterceptor.saveRefreshToken(authResponse.refreshToken)
                
                _uiState.value = LoginUiState.Success
                
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(
                    message = when {
                        e.message?.contains("401") == true -> "Email o contraseña incorrectos"
                        e.message?.contains("network") == true -> "Error de conexión. Verifica tu internet"
                        else -> e.message ?: "Error desconocido al iniciar sesión"
                    }
                )
            }
        }
    }
}

/**
 * Estados de la UI para el login
 */
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

