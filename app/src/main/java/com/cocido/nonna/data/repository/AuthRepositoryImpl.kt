package com.cocido.nonna.data.repository

import android.util.Log
import com.cocido.nonna.data.local.AuthPreferences
import com.cocido.nonna.data.remote.api.AuthApiService
import com.cocido.nonna.data.remote.dto.LoginRequest
import com.cocido.nonna.data.remote.dto.RegisterRequest
import com.cocido.nonna.data.remote.dto.RefreshTokenRequest
import com.cocido.nonna.domain.model.User
import com.cocido.nonna.domain.model.UserId
import com.cocido.nonna.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
    
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            Log.d("AuthRepositoryImpl", "Iniciando login para: $email")
            val request = LoginRequest(email = email, password = password)
            Log.d("AuthRepositoryImpl", "Enviando request al API")
            val response = authApiService.login(request)
            Log.d("AuthRepositoryImpl", "Respuesta recibida del API")
            Log.d("AuthRepositoryImpl", "Access token: ${response.accessToken}")
            Log.d("AuthRepositoryImpl", "Refresh token: ${response.refreshToken}")
            
            // Guardar datos de autenticación
            Log.d("AuthRepositoryImpl", "Guardando datos de autenticación")
            authPreferences.saveAuthData(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                userId = response.user.id,
                email = response.user.email,
                name = response.user.name,
                avatar = response.user.avatar,
                expiresAt = null // TODO: Calcular expiración del token
            )
            Log.d("AuthRepositoryImpl", "Datos de autenticación guardados")
            
            val user = User(
                id = UserId(response.user.id.toString()),
                email = response.user.email,
                name = response.user.name,
                profileImageUrl = response.user.avatar,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            Log.d("AuthRepositoryImpl", "Login completado exitosamente")
            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthRepositoryImpl", "Error en login: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun register(email: String, password: String, name: String): Result<User> {
        return try {
            val request = RegisterRequest(email = email, password = password, name = name)
            val response = authApiService.register(request)
            
            // Guardar datos de autenticación
            authPreferences.saveAuthData(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                userId = response.user.id,
                email = response.user.email,
                name = response.user.name,
                avatar = response.user.avatar,
                expiresAt = null // TODO: Calcular expiración del token
            )
            
            val user = User(
                id = UserId(response.user.id.toString()),
                email = response.user.email,
                name = response.user.name,
                profileImageUrl = response.user.avatar,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun logout(): Result<Unit> {
        return try {
            authPreferences.clearAuthData()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getCurrentUser(): Flow<User?> {
        return if (authPreferences.isLoggedIn() && !authPreferences.isTokenExpired()) {
            val user = User(
                id = UserId(authPreferences.getUserId().toString()),
                email = authPreferences.getUserEmail() ?: "",
                name = authPreferences.getUserName() ?: "",
                profileImageUrl = authPreferences.getUserAvatar(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            flowOf(user)
        } else {
            flowOf(null)
        }
    }
    
    override suspend fun getCurrentUserId(): UserId? {
        return if (authPreferences.isLoggedIn() && !authPreferences.isTokenExpired()) {
            UserId(authPreferences.getUserId().toString())
        } else {
            null
        }
    }
    
    override suspend fun refreshToken(): Result<Unit> {
        return try {
            val refreshToken = authPreferences.getRefreshToken()
            if (refreshToken != null) {
                val request = RefreshTokenRequest(refreshToken = refreshToken)
                val response = authApiService.refreshToken(request)
                
                authPreferences.saveAuthData(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                    userId = authPreferences.getUserId(),
                    email = authPreferences.getUserEmail() ?: "",
                    name = authPreferences.getUserName() ?: "",
                    avatar = authPreferences.getUserAvatar(),
                    expiresAt = null // TODO: Calcular expiración del token
                )
                
                Result.success(Unit)
            } else {
                Result.failure(Exception("No hay refresh token disponible"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun isAuthenticated(): Boolean {
        return authPreferences.isLoggedIn() && !authPreferences.isTokenExpired()
    }
}