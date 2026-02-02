package com.cocido.nonna.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        // Sesión persistida en DataStore: al abrir la app se lee el token y se muestra Home o Welcome
        viewModelScope.launch {
            authRepository.token.collect { token ->
                _authState.update {
                    if (!token.isNullOrBlank()) AuthState.LoggedIn else AuthState.LoggedOut
                }
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
}
