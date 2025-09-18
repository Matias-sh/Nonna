package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTO para requests de cofres familiares
 */
data class VaultRequest(
    @Json(name = "name")
    val name: String,
    @Json(name = "member_uids")
    val memberUids: List<String> = emptyList()
)


