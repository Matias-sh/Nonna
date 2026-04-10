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
        val original = chain.request()
        val requestBuilder = original.newBuilder()
        val path = original.url.encodedPath
        val skipAuthHeader = path == "/auth/login" || path == "/auth/signup"

        val token = runCatching {
            runBlocking { tokenManager.token.first() }
        }.getOrNull()

        if (!skipAuthHeader && !token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        // Dejamos que Retrofit/OkHttp definan el Content-Type correcto según el cuerpo.
        // Solo forzamos Accept para indicar que esperamos JSON en la respuesta.
        requestBuilder.addHeader("Accept", "application/json")

        return chain.proceed(requestBuilder.build())
    }
}
