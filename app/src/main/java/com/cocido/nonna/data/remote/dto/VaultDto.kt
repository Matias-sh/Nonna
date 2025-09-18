package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTO para cofres familiares
 */
data class VaultDto(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "owner_uid")
    val ownerUid: String,
    @Json(name = "member_uids")
    val memberUids: List<String>,
    @Json(name = "created_at")
    val createdAt: Long
)


