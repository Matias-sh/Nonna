package com.cocido.nonna.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para el registro de nuevos usuarios
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            
            registerUseCase(name, email, password)
                .onSuccess { authResponse ->
                    _uiState.value = RegisterUiState.Success(authResponse)
                }
                .onFailure { exception ->
                    _uiState.value = RegisterUiState.Error(
                        message = exception.message ?: "Error desconocido al crear la cuenta"
                    )
                }
        }
    }
    
    fun clearError() {
        if (_uiState.value is RegisterUiState.Error) {
            _uiState.value = RegisterUiState.Idle
        }
    }
}

/**
 * Estados de la UI para el registro
 */
sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val authResponse: com.cocido.nonna.data.remote.dto.AuthResponse) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}