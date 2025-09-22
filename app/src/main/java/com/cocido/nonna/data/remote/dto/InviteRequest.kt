package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTO para requests de invitación a cofres
 */
data class InviteRequest(
    @Json(name = "email")
    val email: String,
    @Json(name = "role")
    val role: String = "viewer" // owner, editor, viewer
)




