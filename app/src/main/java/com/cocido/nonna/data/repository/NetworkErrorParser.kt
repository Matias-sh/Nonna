package com.cocido.nonna.data.repository

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.cocido.nonna.util.UserMessages

/**
 * Parsea el body de error HTTP (JSON) para extraer un mensaje legible.
 * Usado por AuthRepository, ProfileViewModel y otros que muestran errores al usuario.
 */
object NetworkErrorParser {
    private val gson = Gson()

    /**
     * Intenta extraer y normalizar mensaje de error desde un body JSON.
     * Nunca devuelve JSON crudo ni detalles internos del backend.
     */
    fun parse(body: String?): String? {
        if (body.isNullOrBlank()) return null
        val rawMessage = try {
            val json = gson.fromJson(body, JsonObject::class.java)
            extractMessage(json)
        } catch (_: Exception) {
            null
        }
        return normalize(rawMessage ?: body)
    }

    private fun extractMessage(json: JsonObject?): String? {
        if (json == null) return null

        val detailsMessage = json.getAsJsonObject("errorDetails")?.get("message")
        if (detailsMessage != null) {
            if (detailsMessage.isJsonPrimitive) return detailsMessage.asString
            if (detailsMessage.isJsonArray) return joinArray(detailsMessage.asJsonArray)
        }

        val message = json.get("message")
        if (message != null) {
            if (message.isJsonPrimitive) return message.asString
            if (message.isJsonArray) return joinArray(message.asJsonArray)
        }
        return null
    }

    private fun joinArray(array: JsonArray): String {
        return array.joinToString(separator = " ") { element ->
            runCatching { element.asString }.getOrNull().orEmpty()
        }.trim()
    }

    private fun normalize(raw: String): String {
        val value = raw.trim()
        if (value.isBlank()) return UserMessages.GENERIC_ERROR

        val lower = value.lowercase()
        return when {
            "email must be an email" in lower || "email inválido" in lower ->
                UserMessages.INVALID_EMAIL
            "must be an email" in lower && "invitadosemails" in lower ->
                UserMessages.INVALID_INVITE_EMAIL
            "already exists" in lower || "ya existe" in lower || "nombre de usuario en uso" in lower ->
                UserMessages.DUPLICATE_ACCOUNT
            "password" in lower || "contrasena" in lower || "contraseña" in lower ->
                UserMessages.INVALID_PASSWORD_RULES
            "codigo" in lower && ("expired" in lower || "expir" in lower || "venc" in lower) ->
                UserMessages.EXPIRED_VERIFICATION_CODE
            "codigo" in lower && ("invalido" in lower || "inválido" in lower || "incorrect" in lower) ->
                UserMessages.WRONG_VERIFICATION_CODE
            "already verified" in lower || "ya está verificado" in lower || "ya esta verificado" in lower ->
                "Tu correo ya estaba verificado."
            "ya invitado" in lower || "already invited" in lower ->
                "Ese email ya tiene una invitación pendiente."
            "ya es miembro" in lower || "already member" in lower ->
                "Ese usuario ya forma parte del cofre."
            "no se encontró" in lower && "usuario" in lower ->
                "No encontramos ese usuario en el sistema."
            "bad request" in lower || "statuscode\":400" in lower ->
                "Revisá los datos ingresados e intentá de nuevo."
            "unauthorized" in lower || "401" == lower || "credenciales" in lower ->
                UserMessages.INVALID_CREDENTIALS
            "forbidden" in lower || "403" == lower ->
                "No tenés permisos para realizar esta acción."
            "not found" in lower || "404" == lower ->
                "No encontramos lo que estás buscando."
            "timeout" in lower || "timed out" in lower ->
                "La operación tardó demasiado. Intentá nuevamente."
            "network" in lower || "failed to connect" in lower || "enotfound" in lower ->
                UserMessages.NO_INTERNET
            else -> UserMessages.GENERIC_REQUEST_ERROR
        }
    }
}
