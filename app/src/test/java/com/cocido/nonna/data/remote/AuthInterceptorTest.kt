package com.cocido.nonna.data.remote

import com.cocido.nonna.data.local.TokenManager
import io.mockk.every
import io.mockk.mockk
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlinx.coroutines.flow.MutableStateFlow

class AuthInterceptorTest {

    @Test
    fun agregaBearerEnRutasProtegidas() {
        val tokenManager = mockk<TokenManager>()
        every { tokenManager.token } returns MutableStateFlow("token-abc")
        val interceptor = AuthInterceptor(tokenManager)
        Thread.sleep(30)

        val original = Request.Builder()
            .url("https://apinonna.pushsoftware.com.ar/cofres")
            .build()
        val intercepted = runInterceptor(interceptor, original)

        assertEquals("Bearer token-abc", intercepted.request.header("Authorization"))
    }

    @Test
    fun noAgregaBearerEnLogin() {
        val tokenManager = mockk<TokenManager>()
        every { tokenManager.token } returns MutableStateFlow("token-abc")
        val interceptor = AuthInterceptor(tokenManager)
        Thread.sleep(30)

        val original = Request.Builder()
            .url("https://apinonna.pushsoftware.com.ar/auth/login")
            .build()
        val intercepted = runInterceptor(interceptor, original)

        assertNull(intercepted.request.header("Authorization"))
    }

    private fun runInterceptor(interceptor: AuthInterceptor, original: Request): Response {
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns original
        every { chain.proceed(any()) } answers {
            val req = firstArg<Request>()
            Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("{}".toResponseBody("application/json".toMediaType()))
                .build()
        }
        return interceptor.intercept(chain)
    }
}
