package com.cocido.nonna.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject

/**
 * Parsea el body de error HTTP (JSON) para extraer un mensaje legible.
 * Usado por AuthRepository, ProfileViewModel y otros que muestran errores al usuario.
 */
object NetworkErrorParser {
    private val gson = Gson()

    /**
     * Intenta extraer mensaje de error desde un body JSON.
     * Busca en orden: errorDetails.message, message, o devuelve los primeros 200 caracteres del body.
     */
    fun parse(body: String?): String? {
        if (body.isNullOrBlank()) return null
        return try {
            val json = gson.fromJson(body, JsonObject::class.java) ?: return body.take(200)
            json.getAsJsonObject("errorDetails")?.get("message")?.takeIf { it.isJsonPrimitive }?.getAsString()
                ?: json.get("message")?.takeIf { it.isJsonPrimitive }?.getAsString()
                ?: body.take(200)
        } catch (_: Exception) {
            body.take(200)
        }
    }
}
