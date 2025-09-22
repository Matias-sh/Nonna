package com.cocido.nonna.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor para agregar token de autenticación a las peticiones
 */
@Singleton
class AuthInterceptor @Inject constructor() : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // TODO: Obtener token de autenticación del usuario logueado
        val token = "Bearer your_token_here" // Temporal
        
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", token)
            .header("Content-Type", "application/json")
            .build()
        
        return chain.proceed(newRequest)
    }
}