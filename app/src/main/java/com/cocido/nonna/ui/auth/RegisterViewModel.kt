package com.cocido.nonna.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.remote.api.AuthApiService
import com.cocido.nonna.data.remote.dto.RegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para el registro de nuevos usuarios
 * Maneja la lógica de creación de cuentas y estados de la UI
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authApiService: AuthApiService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    
    fun register(name: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            
            try {
                // Validar campos
                if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    _uiState.value = RegisterUiState.Error("Por favor completa todos los campos")
                    return@launch
                }
                
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    _uiState.value = RegisterUiState.Error("Por favor ingresa un email válido")
                    return@launch
                }
                
                if (password.length < 6) {
                    _uiState.value = RegisterUiState.Error("La contraseña debe tener al menos 6 caracteres")
                    return@launch
                }
                
                if (password != confirmPassword) {
                    _uiState.value = RegisterUiState.Error("Las contraseñas no coinciden")
                    return@launch
                }
                
                // Realizar registro
                val registerRequest = RegisterRequest(
                    name = name,
                    email = email,
                    password = password
                )
                val authResponse = authApiService.register(registerRequest)
                
                // TODO: Guardar tokens en SharedPreferences o DataStore
                // authInterceptor.saveAuthToken(authResponse.accessToken, authResponse.expiresIn)
                // authInterceptor.saveRefreshToken(authResponse.refreshToken)
                
                _uiState.value = RegisterUiState.Success
                
            } catch (e: Exception) {
                _uiState.value = RegisterUiState.Error(
                    message = when {
                        e.message?.contains("409") == true -> "Ya existe una cuenta con este email"
                        e.message?.contains("400") == true -> "Datos inválidos. Verifica la información"
                        e.message?.contains("network") == true -> "Error de conexión. Verifica tu internet"
                        else -> e.message ?: "Error desconocido al crear la cuenta"
                    }
                )
            }
        }
    }
}

/**
 * Estados de la UI para el registro
 */
sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

