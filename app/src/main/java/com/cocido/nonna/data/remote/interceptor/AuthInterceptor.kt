package com.cocido.nonna.data.remote.interceptor

import com.cocido.nonna.data.local.AuthPreferences
import com.cocido.nonna.data.manager.AuthStateManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor para agregar tokens de autenticación a las peticiones
 * y manejar el refresco automático de tokens
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val authStateManager: AuthStateManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // No agregar token solo a peticiones específicas de autenticación
        if (originalRequest.url.encodedPath.contains("auth/login") ||
            originalRequest.url.encodedPath.contains("auth/register") ||
            originalRequest.url.encodedPath.contains("auth/refresh")) {
            return chain.proceed(originalRequest)
        }

        val token = authPreferences.getAccessToken()
        if (token != null) {
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()

            val response = chain.proceed(authenticatedRequest)

            // Si el token expiró (401), intentar refrescarlo
            if (response.code == 401) {
                response.close()
                return handleTokenRefresh(chain, originalRequest)
            }

            return response
        }

        return chain.proceed(originalRequest)
    }

    private fun handleTokenRefresh(chain: Interceptor.Chain, originalRequest: okhttp3.Request): Response {
        // Por ahora, simplemente notificar que el token expiró y proceder sin token
        // TODO: Implementar refresh cuando se resuelva el ciclo de dependencias
        runBlocking {
            authStateManager.notifyTokenExpired()
        }
        return chain.proceed(originalRequest)
    }
}

