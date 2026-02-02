package com.cocido.nonna.data.remote

import com.cocido.nonna.data.local.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Añade el token Bearer a las peticiones que requieren autenticación.
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenManager.token.first() }
        val request = chain.request().newBuilder()
        if (!token.isNullOrBlank()) {
            request.addHeader("Authorization", "Bearer $token")
        }
        request.addHeader("Accept", "application/json")
        request.addHeader("Content-Type", "application/json")
        return chain.proceed(request.build())
    }
}
