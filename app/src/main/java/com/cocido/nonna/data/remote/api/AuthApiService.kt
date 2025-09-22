package com.cocido.nonna.data.remote.api

import com.cocido.nonna.data.remote.dto.AuthResponse
import com.cocido.nonna.data.remote.dto.LoginRequest
import com.cocido.nonna.data.remote.dto.RefreshTokenRequest
import com.cocido.nonna.data.remote.dto.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Servicio de API para autenticación
 */
interface AuthApiService {
    
    @POST("auth/login/")
    suspend fun login(@Body request: LoginRequest): AuthResponse
    
    @POST("auth/register/")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
    
    @POST("auth/refresh/")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): AuthResponse
}