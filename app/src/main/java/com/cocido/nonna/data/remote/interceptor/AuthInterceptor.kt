package com.cocido.nonna.data.remote.interceptor

import com.cocido.nonna.data.local.AuthPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor para agregar token de autenticación a las peticiones
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val authPreferences: AuthPreferences
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val accessToken = authPreferences.getAccessToken()
        val token = if (accessToken != null && !authPreferences.isTokenExpired()) {
            "Bearer $accessToken"
        } else {
            null
        }
        
        val newRequest = originalRequest.newBuilder()
            .apply {
                if (token != null) {
                    header("Authorization", token)
                }
            }
            .header("Content-Type", "application/json")
            .build()
        
        return chain.proceed(newRequest)
    }
}