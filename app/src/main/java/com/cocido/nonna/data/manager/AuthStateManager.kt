package com.cocido.nonna.data.manager

import com.cocido.nonna.data.local.AuthPreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager para manejar el estado global de autenticación
 * y notificar cambios a toda la app
 */
@Singleton
class AuthStateManager @Inject constructor(
    private val authPreferences: AuthPreferences
) {

    private val _authStateEvents = MutableSharedFlow<AuthStateEvent>()
    val authStateEvents: SharedFlow<AuthStateEvent> = _authStateEvents.asSharedFlow()

    suspend fun notifyTokenExpired() {
        authPreferences.clearAuthData()
        _authStateEvents.emit(AuthStateEvent.TokenExpired)
    }

    suspend fun notifyLoggedOut() {
        authPreferences.clearAuthData()
        _authStateEvents.emit(AuthStateEvent.LoggedOut)
    }

    suspend fun notifyLoggedIn() {
        _authStateEvents.emit(AuthStateEvent.LoggedIn)
    }
}

sealed class AuthStateEvent {
    object LoggedIn : AuthStateEvent()
    object LoggedOut : AuthStateEvent()
    object TokenExpired : AuthStateEvent()
}