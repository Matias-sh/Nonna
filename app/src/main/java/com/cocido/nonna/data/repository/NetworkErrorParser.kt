package com.cocido.nonna.data.repository

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.cocido.nonna.util.UserMessages

/**
 * Parsea el body de error HTTP (JSON) para extraer un mensaje legible.
 * Usado por AuthRepository, ProfileViewModel y otros que muestran errores al usuario.
 */
object NetworkErrorParser {
    private val gson = Gson()
    private val CODE_WORD_REGEX = Regex("\\bcode\\b")
    private val HTML_TAG_REGEX = Regex("<\\s*(html|head|body|script|title|!doctype)", RegexOption.IGNORE_CASE)

    /**
     * Intenta extraer y normalizar mensaje de error desde un body JSON.
     * Nunca devuelve JSON crudo ni detalles internos del backend.
     */
    fun parse(body: String?, statusCode: Int? = null): String? {
        if (body.isNullOrBlank()) return null
        val rawMessage = try {
            val json = gson.fromJson(body, JsonObject::class.java)
            extractMessage(json)
        } catch (_: Exception) {
            null
        }
        val normalized = if (!rawMessage.isNullOrBlank()) {
            normalize(rawMessage, fromBackendMessage = true)
        } else {
            normalize(body, fromBackendMessage = false)
        }
        return sanitize(normalized, statusCode)
    }

    fun parseOrGeneric(body: String?, statusCode: Int? = null): String {
        return parse(body, statusCode) ?: fallbackByStatus(statusCode)
    }

    private fun extractMessage(json: JsonObject?): String? {
        if (json == null) return null

        // Prioridad: "details"/"detail" (mensaje accionable de negocio) por encima de "message" genérico.
        val candidates = listOf(
            json.get("details"),
            json.get("detail"),
            json.getAsJsonObject("errorDetails")?.get("details"),
            json.getAsJsonObject("errorDetails")?.get("detail"),
            json.getAsJsonObject("errorDetails")?.get("message"),
            json.get("message")
        )
        return candidates.firstNotNullOfOrNull(::extractReadableString)
    }

    private fun joinArray(array: JsonArray): String {
        return array.joinToString(separator = " ") { element ->
            runCatching { element.asString }.getOrNull().orEmpty()
        }.trim()
    }

    private fun extractReadableString(element: JsonElement?): String? {
        if (element == null || element.isJsonNull) return null
        return when {
            element.isJsonPrimitive -> element.asString.trim().takeIf { it.isNotBlank() }
            element.isJsonArray -> joinArray(element.asJsonArray).takeIf { it.isNotBlank() }
            element.isJsonObject -> {
                val obj = element.asJsonObject
                listOf("message", "detail", "details", "error")
                    .firstNotNullOfOrNull { key -> extractReadableString(obj.get(key)) }
            }
            else -> null
        }
    }

    /** "codigo" / "código" o la palabra inglesa "code" como palabra completa. */
    private fun mentionsCodigoOrCodeWord(lower: String): Boolean =
        "codigo" in lower || "código" in lower || CODE_WORD_REGEX.containsMatchIn(lower)

    /** Contexto típico de error al validar código de email o de recuperación de contraseña. */
    private fun isVerificationOrResetCodeContext(lower: String): Boolean =
        mentionsCodigoOrCodeWord(lower) ||
            "verification code" in lower ||
            "reset code" in lower ||
            "código de verificación" in lower ||
            ("code" in lower && ("reset" in lower || "recuper" in lower || "recovery" in lower)) ||
            ("password" in lower && "reset" in lower) ||
            ("contraseña" in lower && "recuper" in lower) ||
            ("contrasena" in lower && "recuper" in lower) ||
            (("reset" in lower || "recuper" in lower) && "token" in lower)

    private fun mentionsInvalidOrWrongCode(lower: String): Boolean =
        "invalido" in lower || "inválido" in lower || "invalid" in lower ||
            "incorrect" in lower || "incorrecto" in lower || "wrong" in lower ||
            "no coincide" in lower || "does not match" in lower ||
            "no es válido" in lower || "no es valido" in lower ||
            "not valid" in lower || "mismatch" in lower

    private fun mentionsCodeExpired(lower: String): Boolean =
        "expired" in lower || "expir" in lower || "venc" in lower || "caduc" in lower

    private fun normalize(raw: String, fromBackendMessage: Boolean): String {
        val value = raw.trim()
        if (value.isBlank()) return UserMessages.GENERIC_ERROR

        val lower = value.lowercase()
        return when {
            "service unavailable" in lower || "503" == lower || ("503" in lower && "unavailable" in lower) ->
                UserMessages.SERVER_UNREACHABLE
            "email must be an email" in lower || "email inválido" in lower ->
                UserMessages.INVALID_EMAIL
            "must be an email" in lower && "invitadosemails" in lower ->
                UserMessages.INVALID_INVITE_EMAIL
            "parentesco must be one of" in lower ->
                "El parentesco no es válido. Elegí una opción sugerida o usá \"Otro\"."
            "already exists" in lower || "ya existe" in lower || "nombre de usuario en uso" in lower ->
                UserMessages.DUPLICATE_ACCOUNT
            isVerificationOrResetCodeContext(lower) && mentionsCodeExpired(lower) ->
                UserMessages.EXPIRED_VERIFICATION_CODE
            isVerificationOrResetCodeContext(lower) && mentionsInvalidOrWrongCode(lower) ->
                UserMessages.WRONG_VERIFICATION_CODE
            ("password" in lower || "contrasena" in lower || "contraseña" in lower) &&
                !isVerificationOrResetCodeContext(lower) ->
                UserMessages.INVALID_PASSWORD_RULES
            "already verified" in lower || "ya está verificado" in lower || "ya esta verificado" in lower ->
                "Tu correo ya estaba verificado."
            "ya invitado" in lower || "already invited" in lower ->
                "Ese email ya tiene una invitación pendiente."
            "ya es miembro" in lower || "already member" in lower ->
                "Ese usuario ya forma parte del cofre."
            "no se encontró" in lower && "usuario" in lower ->
                "No encontramos ese usuario en el sistema."
            "límite de" in lower || "limite de" in lower ->
                value
            lower == "bad request" || lower == "bad request exception" || lower == "bad_request" ->
                "Revisá los datos ingresados e intentá de nuevo."
            "datos de entrada no válidos" in lower || "datos de entrada no validos" in lower ->
                "No pudimos procesar la solicitud. Revisá los datos y, si es por límite del plan, actualizá tu suscripción."
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
            else -> {
                // Si viene de "message" del backend, lo mostramos para no ocultar la causa real
                // (ej.: límite de plan, validación puntual, etc).
                if (fromBackendMessage) {
                    value
                } else {
                    // Evita exponer JSON crudo cuando no pudimos parsear estructura.
                    if (value.startsWith("{") || value.startsWith("[")) {
                        UserMessages.GENERIC_REQUEST_ERROR
                    } else {
                        value
                    }
                }
            }
        }
    }

    private fun sanitize(message: String?, statusCode: Int?): String? {
        val value = message?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val lower = value.lowercase()
        if (looksSensitive(lower, value)) {
            return fallbackByStatus(statusCode)
        }
        return value
    }

    private fun looksSensitive(lower: String, raw: String): Boolean {
        if (HTML_TAG_REGEX.containsMatchIn(raw)) return true
        if (raw.length > 320) return true
        return "<!doctype" in lower ||
            "<html" in lower ||
            "<body" in lower ||
            "</" in lower ||
            "exception" in lower ||
            "stacktrace" in lower ||
            "traceback" in lower ||
            "sqlstate" in lower ||
            "org.springframework" in lower ||
            "nestjs" in lower ||
            "at com." in lower ||
            " at " in lower && "line " in lower
    }

    private fun fallbackByStatus(statusCode: Int?): String {
        return when (statusCode) {
            401 -> UserMessages.INVALID_CREDENTIALS
            403 -> "No tenés permisos para realizar esta acción."
            404 -> "No encontramos lo que estás buscando."
            408, 504 -> UserMessages.REQUEST_TIMEOUT
            429 -> "Hay demasiadas solicitudes. Esperá unos segundos e intentá nuevamente."
            500, 502, 503 -> UserMessages.SERVER_UNREACHABLE
            else -> UserMessages.GENERIC_REQUEST_ERROR
        }
    }
}
