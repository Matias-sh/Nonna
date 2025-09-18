package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTOs para autenticación
 */

data class LoginRequest(
    @Json(name = "email")
    val email: String,
    @Json(name = "password")
    val password: String
)

data class RegisterRequest(
    @Json(name = "email")
    val email: String,
    @Json(name = "password")
    val password: String,
    @Json(name = "name")
    val name: String
)

data class AuthResponse(
    @Json(name = "access_token")
    val accessToken: String,
    @Json(name = "refresh_token")
    val refreshToken: String,
    @Json(name = "expires_in")
    val expiresIn: Long,
    @Json(name = "user")
    val user: UserDto
)

data class UserDto(
    @Json(name = "id")
    val id: String,
    @Json(name = "email")
    val email: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "avatar_url")
    val avatarUrl: String?
)

data class RefreshTokenRequest(
    @Json(name = "refresh_token")
    val refreshToken: String
)

