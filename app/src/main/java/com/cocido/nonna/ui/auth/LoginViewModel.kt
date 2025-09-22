package com.cocido.nonna.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para el login de usuarios
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            
            loginUseCase(email, password)
                .onSuccess { authResponse ->
                    _uiState.value = LoginUiState.Success(authResponse)
                    // Después de un breve delay, navegar al home
                    kotlinx.coroutines.delay(1000)
                    _uiState.value = LoginUiState.NavigateToHome
                }
                .onFailure { exception ->
                    _uiState.value = LoginUiState.Error(
                        message = exception.message ?: "Error desconocido al iniciar sesión"
                    )
                }
        }
    }
    
    fun clearError() {
        if (_uiState.value is LoginUiState.Error) {
            _uiState.value = LoginUiState.Idle
        }
    }
}

/**
 * Estados de la UI para el login
 */
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val authResponse: com.cocido.nonna.data.remote.dto.AuthResponse) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
    object NavigateToHome : LoginUiState()
}