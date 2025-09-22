package com.cocido.nonna.data.repository

import android.util.Log
import com.cocido.nonna.data.local.AuthPreferences
import com.cocido.nonna.data.remote.api.AuthApiService
import com.cocido.nonna.data.remote.dto.AuthResponse
import com.cocido.nonna.data.remote.dto.LoginRequest
import com.cocido.nonna.data.remote.dto.RegisterRequest
import com.cocido.nonna.data.remote.dto.RefreshTokenRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de autenticación
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val authPreferences: AuthPreferences
) : AuthRepository {
    
    private val _isLoggedIn = MutableStateFlow(authPreferences.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private var _currentToken: String? = authPreferences.getAccessToken()
    
    override suspend fun login(request: LoginRequest): AuthResponse {
        try {
            Log.d("AuthRepository", "Intentando login para: ${request.email}")
            val response = authApiService.login(request)
            Log.d("AuthRepository", "Login exitoso: ${response.user.name}")
            
            // Guardar datos de autenticación
            authPreferences.saveAuthData(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                userId = response.user.id,
                email = response.user.email,
                name = response.user.name,
                avatar = response.user.avatar
            )
            
            _currentToken = response.accessToken
            _isLoggedIn.value = true
            return response
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en login: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun register(request: RegisterRequest): AuthResponse {
        val response = authApiService.register(request)
        
        // Guardar datos de autenticación
        authPreferences.saveAuthData(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            userId = response.user.id,
            email = response.user.email,
            name = response.user.name,
            avatar = response.user.avatar
        )
        
        _currentToken = response.accessToken
        _isLoggedIn.value = true
        return response
    }
    
    override suspend fun refreshToken(refreshToken: String): AuthResponse {
        val request = RefreshTokenRequest(refreshToken)
        val response = authApiService.refreshToken(request)
        
        // Actualizar token guardado
        authPreferences.saveAuthData(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            userId = response.user.id,
            email = response.user.email,
            name = response.user.name,
            avatar = response.user.avatar
        )
        
        _currentToken = response.accessToken
        return response
    }
    
    override suspend fun logout() {
        authPreferences.clearAuthData()
        _currentToken = null
        _isLoggedIn.value = false
    }
    
    override fun getCurrentToken(): String? = _currentToken
    
    override fun isLoggedIn(): Boolean = _isLoggedIn.value
    
    // Métodos adicionales para obtener información del usuario
    fun getCurrentUser(): com.cocido.nonna.data.remote.dto.UserDto? {
        return if (authPreferences.isLoggedIn()) {
            com.cocido.nonna.data.remote.dto.UserDto(
                id = authPreferences.getUserId(),
                email = authPreferences.getUserEmail() ?: "",
                username = authPreferences.getUserEmail() ?: "",
                name = authPreferences.getUserName() ?: "",
                avatar = authPreferences.getUserAvatar(),
                phone = "",
                birthDate = null,
                isPremium = false,
                createdAt = "",
                updatedAt = ""
            )
        } else null
    }
    
    fun updateUserProfile(name: String, avatar: String? = null) {
        authPreferences.updateUserProfile(name, avatar)
    }
    
    suspend fun checkAndRefreshToken(): Boolean {
        val refreshToken = authPreferences.getRefreshToken()
        return if (refreshToken != null && authPreferences.isTokenExpired()) {
            try {
                refreshToken(refreshToken)
                true
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error refreshing token: ${e.message}")
                logout()
                false
            }
        } else {
            true
        }
    }
}


