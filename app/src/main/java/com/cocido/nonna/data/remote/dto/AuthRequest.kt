package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTO para requests de autenticación
 */
data class AuthRequest(
    @Json(name = "email")
    val email: String,
    @Json(name = "password")
    val password: String
)





