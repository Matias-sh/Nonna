package com.cocido.nonna.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
        viewModelScope.launch {
            combine(
                authRepository.token,
                authRepository.emailVerificado
            ) { token, emailVerificado ->
                when {
                    token.isNullOrBlank() -> AuthState.LoggedOut
                    // emailVerificado == false (explícito): el usuario se registró pero no verificó
                    emailVerificado == false -> AuthState.EmailPendingVerification
                    // null (no guardado) o true: usuario previo o ya verificado → permitir acceso
                    else -> AuthState.LoggedIn
                }
            }.collect { state ->
                _authState.update { state }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

sealed class AuthState {
    data object Loading : AuthState()
    data object LoggedIn : AuthState()
    data object LoggedOut : AuthState()
    /** Token válido pero email sin verificar. Navegar a EmailVerificationScreen. */
    data object EmailPendingVerification : AuthState()
}
