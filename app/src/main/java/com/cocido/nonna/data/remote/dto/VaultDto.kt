package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTOs para cofres (vaults)
 */

data class VaultDto(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "description")
    val description: String,
    @Json(name = "owner")
    val owner: String,
    @Json(name = "owner_name")
    val ownerName: String,
    @Json(name = "is_public")
    val isPublic: Boolean,
    @Json(name = "member_count")
    val memberCount: Int,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "updated_at")
    val updatedAt: String
)

data class VaultMemberDto(
    @Json(name = "id")
    val id: Int,
    @Json(name = "user")
    val user: String,
    @Json(name = "user_name")
    val userName: String,
    @Json(name = "user_email")
    val userEmail: String,
    @Json(name = "role")
    val role: String,
    @Json(name = "joined_at")
    val joinedAt: String
)

data class VaultCreateRequest(
    @Json(name = "name")
    val name: String,
    @Json(name = "description")
    val description: String,
    @Json(name = "is_public")
    val isPublic: Boolean = false
)