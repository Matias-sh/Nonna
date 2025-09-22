package com.cocido.nonna.data.remote.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Interceptor para manejar errores HTTP y convertir respuestas HTML en excepciones
 */
class ErrorHandlingInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        
        // Verificar si la respuesta es exitosa
        if (!response.isSuccessful) {
            val responseBody = response.body?.string() ?: ""
            
            Log.e("ErrorHandlingInterceptor", "HTTP Error ${response.code}: ${response.message}")
            Log.e("ErrorHandlingInterceptor", "Response body: $responseBody")
            
            // Si la respuesta contiene HTML, es probable que sea una página de error
            if (responseBody.contains("<html") || responseBody.contains("<!DOCTYPE")) {
                throw IOException("El servidor devolvió una página HTML en lugar de JSON. Posible error de configuración del servidor.")
            }
            
            // Para otros errores, crear una excepción descriptiva
            when (response.code) {
                400 -> throw IOException("Solicitud inválida: $responseBody")
                401 -> throw IOException("No autorizado: $responseBody")
                403 -> throw IOException("Acceso denegado: $responseBody")
                404 -> throw IOException("Endpoint no encontrado: $responseBody")
                500 -> throw IOException("Error interno del servidor: $responseBody")
                else -> throw IOException("Error HTTP ${response.code}: $responseBody")
            }
        }
        
        return response
    }
}
