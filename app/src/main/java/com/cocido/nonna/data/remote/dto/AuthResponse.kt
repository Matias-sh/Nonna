package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTO para responses de autenticación
 */
data class AuthResponse(
    @Json(name = "access_token")
    val accessToken: String,
    @Json(name = "refresh_token")
    val refreshToken: String,
    @Json(name = "expires_in")
    val expiresIn: Long,
    @Json(name = "user_id")
    val userId: String
)


