package com.cocido.nonna.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id") private val idRaw: JsonElement? = null,
    @SerializedName("email") val email: String,
    @SerializedName("nombreUsuario") val nombreUsuario: String? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("persona") val persona: PersonaDto? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("avatar_url") val avatar_url: String? = null,
    @SerializedName("fotoPerfil") val fotoPerfil: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("created_at") val created_at: String? = null
) {
    /** Constructor para crear UserDto manualmente (ej. desde UsuarioDto o AuthRepository). */
    constructor(id: String, email: String, name: String? = null) : this(
        idRaw = JsonPrimitive(id),
        email = email,
        name = name
    )

    /** id como string (backend puede devolver número). */
    val id: String
        get() = when {
            idRaw == null -> ""
            idRaw.isJsonPrimitive -> {
                val p = idRaw.asJsonPrimitive
                if (p.isNumber) p.asInt.toString() else p.asString
            }
            else -> ""
        }

    /** Nombre completo (persona nombre+apellido) o nombre de usuario o email. */
    fun displayName(): String =
        listOfNotNull(persona?.nombre, persona?.apellido).joinToString(" ").trim()
            .ifEmpty { nombreUsuario ?: nombre ?: name ?: email.substringBefore("@") }

    /** Para mostrar en perfil/listas: nombre de usuario si existe, si no displayName. */
    fun displayNameOrUsername(): String = nombreUsuario ?: displayName()
}
