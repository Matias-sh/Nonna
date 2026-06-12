package com.cocido.nonna.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id") private val idRaw: JsonElement? = null,
    @SerializedName("email") val email: String = "",
    @SerializedName("nombreUsuario") val nombreUsuario: String? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("apellido") val apellido: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("persona") val persona: PersonaDto? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("avatar_url") val avatar_url: String? = null,
    @SerializedName("fotoPerfil") val fotoPerfil: String? = null,
    @SerializedName("urlFotoPerfil") val urlFotoPerfil: String? = null,
    @SerializedName("foto_perfil") val fotoPerfilSnake: String? = null,
    @SerializedName("url_foto_perfil") val urlFotoPerfilSnake: String? = null,
    @SerializedName("emailVerificado") val emailVerificado: Boolean? = null,
    @SerializedName("email_verificado") val emailVerificadoSnake: Boolean? = null,
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
        get() = idRaw.primitiveIdString()

    /** Nombre completo (persona nombre+apellido) o nombre de usuario o email. */
    fun displayName(): String {
        listOfNotNull(persona?.nombre, persona?.apellido).joinToString(" ").trim()
            .takeIf { it.isNotBlank() }
            ?.let { return it }
        listOfNotNull(nombre, apellido).joinToString(" ").trim()
            .takeIf { it.isNotBlank() }
            ?.let { return it }
        return nombreUsuario?.trim()?.takeIf { it.isNotBlank() }
            ?: name?.trim()?.takeIf { it.isNotBlank() }
            ?: email.substringBefore("@")
    }

    /** Para mostrar en perfil/listas: nombre de usuario si existe, si no displayName. */
    fun displayNameOrUsername(): String = nombreUsuario ?: displayName()

    fun isEmailVerified(): Boolean = emailVerificado ?: emailVerificadoSnake ?: false

    fun profileImageUrl(): String? {
        val raw = listOfNotNull(
            avatarUrl,
            avatar_url,
            fotoPerfil,
            urlFotoPerfil,
            fotoPerfilSnake,
            urlFotoPerfilSnake
        ).firstOrNull { !it.isNullOrBlank() && it != "string" }?.trim() ?: return null

        return when {
            raw.startsWith("http://") || raw.startsWith("https://") -> raw
            raw.startsWith("/") -> "https://apinonna.pushsoftware.com.ar$raw"
            else -> "https://apinonna.pushsoftware.com.ar/$raw"
        }
    }
}
