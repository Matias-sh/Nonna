package com.cocido.nonna.data.remote.dto

import com.google.gson.JsonPrimitive
import com.google.gson.annotations.SerializedName

/** Backend espera email + contrasena (no "password"). */
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("contrasena") val password: String
)

data class SignupRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("name") val name: String? = null
)

/** Respuesta real del backend: access_token + usuario (persona anidada). */
data class PersonaDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("apellido") val apellido: String? = null
)

data class UsuarioDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("nombreUsuario") val nombreUsuario: String? = null,
    @SerializedName("email") val email: String? = null,
    /** Estado del usuario devuelto por el backend; no se envía en signup. */
    @SerializedName("activo") val activo: Boolean? = null,
    @SerializedName("emailVerificado") val emailVerificado: Boolean? = null,
    @SerializedName("ultimoAcceso") val ultimoAcceso: String? = null,
    @SerializedName("fotoPerfil") val fotoPerfil: String? = null,
    @SerializedName("persona") val persona: PersonaDto? = null
) {
    fun toUserDto(): UserDto = UserDto(
        idRaw = JsonPrimitive(id?.toString() ?: ""),
        email = email ?: "",
        nombreUsuario = nombreUsuario,
        persona = persona,
        fotoPerfil = fotoPerfil,
        emailVerificado = emailVerificado,
        name = listOf(persona?.nombre, persona?.apellido).filterNotNull().joinToString(" ").ifEmpty { nombreUsuario ?: "" }
    )
}

data class AuthResponse(
    @SerializedName("token") val token: String? = null,
    @SerializedName("accessToken") val accessToken: String? = null,
    @SerializedName("access_token") val accessTokenField: String? = null,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("refresh_token") val refreshTokenField: String? = null,
    @SerializedName("user") val user: UserDto? = null,
    @SerializedName("usuario") val usuario: UsuarioDto? = null
) {
    /** Token usable: backend devuelve access_token; también soportamos token/accessToken. */
    val tokenValue: String? get() = token ?: accessToken ?: accessTokenField
    val refreshTokenValue: String? get() = refreshToken ?: refreshTokenField
}

/** Backend espera email, contrasena (nueva), confirmarContrasena. */
data class ChangePasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("contrasena") val newPassword: String,
    @SerializedName("confirmarContrasena") val confirmPassword: String
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class VerifyEmailRequest(
    @SerializedName("codigo") val codigo: String
)

data class VerificationResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("usuario") val usuario: UsuarioDto? = null,
    @SerializedName("user") val user: UserDto? = null
) {
    fun userDtoOrNull(): UserDto? = user ?: usuario?.toUserDto()
}

data class GenericMessageResponse(
    @SerializedName("message") val message: String? = null
)
