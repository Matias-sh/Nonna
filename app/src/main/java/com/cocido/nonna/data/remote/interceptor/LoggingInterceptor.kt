package com.cocido.nonna.data.remote.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.io.IOException

/**
 * Interceptor para logging de peticiones y respuestas HTTP
 */
class LoggingInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Log de la petición
        Log.d("HTTP_REQUEST", "URL: ${request.url}")
        Log.d("HTTP_REQUEST", "Method: ${request.method}")
        Log.d("HTTP_REQUEST", "Headers: ${request.headers}")
        
        // Log del body de la petición
        request.body?.let { body ->
            try {
                val buffer = Buffer()
                body.writeTo(buffer)
                Log.d("HTTP_REQUEST", "Body: ${buffer.readUtf8()}")
            } catch (e: IOException) {
                Log.e("HTTP_REQUEST", "Error reading request body", e)
            }
        }
        
        val response = chain.proceed(request)
        
        // Log de la respuesta
        Log.d("HTTP_RESPONSE", "Code: ${response.code}")
        Log.d("HTTP_RESPONSE", "Message: ${response.message}")
        Log.d("HTTP_RESPONSE", "Headers: ${response.headers}")
        
        // Log del body de la respuesta
        val responseBody = response.body
        if (responseBody != null) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer
            val responseBodyString = buffer.clone().readUtf8()
            Log.d("HTTP_RESPONSE", "Body: $responseBodyString")
        }
        
        return response
    }
}
