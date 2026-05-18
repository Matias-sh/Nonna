package com.cocido.nonna.data.remote

import com.cocido.nonna.data.local.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Añade el token Bearer a las peticiones que requieren autenticación.
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    @Volatile
    private var cachedToken: String? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            tokenManager.token.collectLatest { token ->
                cachedToken = token
            }
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requestBuilder = original.newBuilder()
        val path = original.url.encodedPath
        val skipAuthHeader = path == "/auth/login" ||
            path == "/auth/signup" ||
            path == "/auth/password-reset/request-code" ||
            path == "/auth/password-reset/verify-code" ||
            path == "/auth/password-reset/confirm"

        val token = cachedToken

        if (!skipAuthHeader && !token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        // Dejamos que Retrofit/OkHttp definan el Content-Type correcto según el cuerpo.
        // Solo forzamos Accept para indicar que esperamos JSON en la respuesta.
        requestBuilder.addHeader("Accept", "application/json")

        return chain.proceed(requestBuilder.build())
    }
}
