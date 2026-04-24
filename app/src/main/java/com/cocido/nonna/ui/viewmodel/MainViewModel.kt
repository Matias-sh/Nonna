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
import kotlinx.coroutines.flow.distinctUntilChanged
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
        // Sesión persistida en DataStore: validamos token contra /auth/me.
        // distinctUntilChanged: DataStore emite en cada edit aunque el access_token no cambie;
        // sin esto volvíamos a Loading y se desmontaba todo el NavHost (crashes al navegar / volver).
        // Si ya hay sesión, refrescamos /me en silencio sin pantalla de carga ni destruir la UI.
        viewModelScope.launch {
            authRepository.token
                .distinctUntilChanged()
                .collect { token ->
                    if (token.isNullOrBlank()) {
                        _authState.update { AuthState.LoggedOut }
                        return@collect
                    }
                    val sessionAlreadyActive =
                        _authState.value is AuthState.LoggedInVerified ||
                            _authState.value is AuthState.LoggedInUnverified
                    if (!sessionAlreadyActive) {
                        _authState.update { AuthState.Loading }
                    }
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
