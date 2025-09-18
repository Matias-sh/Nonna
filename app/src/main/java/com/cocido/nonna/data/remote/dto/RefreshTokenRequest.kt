package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTO para request de refresh token
 */
data class RefreshTokenRequest(
    @Json(name = "refresh_token")
    val refreshToken: String
)


