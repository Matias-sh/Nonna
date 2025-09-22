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
    @Json(name = "user")
    val user: UserDto
)

data class UserDto(
    @Json(name = "id")
    val id: Int,
    @Json(name = "email")
    val email: String,
    @Json(name = "username")
    val username: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "avatar")
    val avatar: String?,
    @Json(name = "phone")
    val phone: String,
    @Json(name = "birth_date")
    val birthDate: String?,
    @Json(name = "is_premium")
    val isPremium: Boolean,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "updated_at")
    val updatedAt: String
)

data class RefreshTokenRequest(
    @Json(name = "refresh_token")
    val refreshToken: String
)

