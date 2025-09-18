package com.cocido.nonna.data.remote.interceptor

import android.content.SharedPreferences
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
    private val sharedPreferences: SharedPreferences
) : Interceptor {
    
    companion object {
        private const val AUTH_TOKEN_KEY = "auth_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val TOKEN_EXPIRY_KEY = "token_expiry"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // No agregar token a peticiones de autenticación
        if (originalRequest.url.encodedPath.contains("auth/")) {
            return chain.proceed(originalRequest)
        }
        
        val token = getAuthToken()
        if (token != null && !isTokenExpired()) {
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            
            val response = chain.proceed(authenticatedRequest)
            
            // Si el token expiró, intentar refrescarlo
            if (response.code == 401) {
                response.close()
                return handleTokenRefresh(chain, originalRequest)
            }
            
            return response
        }
        
        return chain.proceed(originalRequest)
    }
    
    private fun getAuthToken(): String? {
        return sharedPreferences.getString(AUTH_TOKEN_KEY, null)
    }
    
    private fun isTokenExpired(): Boolean {
        val expiryTime = sharedPreferences.getLong(TOKEN_EXPIRY_KEY, 0)
        return System.currentTimeMillis() >= expiryTime
    }
    
    private fun handleTokenRefresh(chain: Interceptor.Chain, originalRequest: Interceptor.Chain.Request): Response {
        // TODO: Implementar lógica de refresco de token
        // Por ahora, simplemente proceder sin token
        return chain.proceed(originalRequest)
    }
    
    fun saveAuthToken(token: String, expiresIn: Long) {
        sharedPreferences.edit()
            .putString(AUTH_TOKEN_KEY, token)
            .putLong(TOKEN_EXPIRY_KEY, System.currentTimeMillis() + (expiresIn * 1000))
            .apply()
    }
    
    fun saveRefreshToken(refreshToken: String) {
        sharedPreferences.edit()
            .putString(REFRESH_TOKEN_KEY, refreshToken)
            .apply()
    }
    
    fun clearTokens() {
        sharedPreferences.edit()
            .remove(AUTH_TOKEN_KEY)
            .remove(REFRESH_TOKEN_KEY)
            .remove(TOKEN_EXPIRY_KEY)
            .apply()
    }
}

