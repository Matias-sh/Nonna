package com.cocido.nonna.data.remote.dto

import com.google.gson.JsonElement

/**
 * Convierte un id JSON (número o string) a texto.
 * No usa [com.google.gson.JsonPrimitive.asInt]: con enteros fuera del rango de Int Gson lanza y la app puede cerrarse.
 */
/** Portada de audio: el backend puede enviar string URL u objeto con URL. */
internal fun JsonElement?.audioCoverUrlFromJson(): String? {
    if (this == null || isJsonNull) return null
    return try {
        when {
            isJsonPrimitive -> asJsonPrimitive.asString.trim().takeIf { it.isNotBlank() && it != "null" }
            isJsonObject -> listOf("url", "rutaArchivo", "href")
                .firstNotNullOfOrNull { key ->
                    asJsonObject.get(key)?.takeIf { el -> el.isJsonPrimitive }?.asString
                        ?.trim()
                        ?.takeIf { it.isNotBlank() && it != "null" }
                }
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}

internal fun JsonElement?.primitiveIdString(): String {
    if (this == null || !isJsonPrimitive) return ""
    val p = asJsonPrimitive
    return try {
        when {
            p.isNumber -> p.asBigDecimal.stripTrailingZeros().toPlainString()
            p.isString -> p.asString
            else -> ""
        }
    } catch (_: Exception) {
        try {
            when {
                p.isString -> p.asString
                else -> p.toString().trim('"')
            }
        } catch (_: Exception) {
            ""
        }
    }
}
