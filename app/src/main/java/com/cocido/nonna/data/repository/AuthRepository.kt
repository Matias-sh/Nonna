package com.cocido.nonna.data.repository

import com.cocido.nonna.data.remote.dto.AuthResponse
import com.cocido.nonna.data.remote.dto.LoginRequest
import com.cocido.nonna.data.remote.dto.RegisterRequest

/**
 * Interfaz del repositorio de autenticación
 */
interface AuthRepository {
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun register(request: RegisterRequest): AuthResponse
    suspend fun refreshToken(refreshToken: String): AuthResponse
    suspend fun logout()
    fun getCurrentToken(): String?
    fun isLoggedIn(): Boolean
}


