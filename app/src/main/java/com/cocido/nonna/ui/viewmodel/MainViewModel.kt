package com.cocido.nonna.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val isLoggedIn: Flow<Boolean> = authRepository.isLoggedIn

    init {
        // Sesión persistida en DataStore: al abrir la app validamos token contra /auth/me
        // para evitar estados inconsistentes (ej: emailVerificado desactualizado).
        viewModelScope.launch {
            authRepository.token.collect { token ->
                if (token.isNullOrBlank()) {
                    _authState.update { AuthState.LoggedOut }
                } else {
                    _authState.update { AuthState.Loading }
                    when (val meResult = authRepository.getMe()) {
                        is ApiResult.Success -> {
                            val user = meResult.data
                            _authState.update {
                                if (user.isEmailVerified()) AuthState.LoggedInVerified
                                else AuthState.LoggedInUnverified
                            }
                        }
                        is ApiResult.Error -> _authState.update { AuthState.LoggedOut }
                        else -> _authState.update { AuthState.LoggedOut }
                    }
                }
            }
        }
    }

    fun onEmailVerified() {
        _authState.update { AuthState.LoggedInVerified }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

sealed class AuthState {
    data object Loading : AuthState()
    data object LoggedInUnverified : AuthState()
    data object LoggedInVerified : AuthState()
    data object LoggedOut : AuthState()
}
