package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

data class VaultDto(
    @Json(name = "id")
    val id: String, // UUID string
    @Json(name = "name")
    val name: String,
    @Json(name = "description")
    val description: String?,
    @Json(name = "owner")
    val owner: Int, // User ID
    @Json(name = "owner_name")
    val ownerName: String?,
    @Json(name = "member_count")
    val memberCount: Int?,
    @Json(name = "is_public")
    val isPublic: Boolean?,
    @Json(name = "members")
    val members: List<VaultMemberDto>? = null, // Only in detail view
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "updated_at")
    val updatedAt: String
)

data class VaultMemberDto(
    @Json(name = "id")
    val id: Int,
    @Json(name = "user")
    val user: Int,
    @Json(name = "user_name")
    val userName: String?,
    @Json(name = "user_email")
    val userEmail: String?,
    @Json(name = "role")
    val role: String, // "owner", "admin", "member", "viewer"
    @Json(name = "joined_at")
    val joinedAt: String
)

data class CreateVaultRequest(
    @Json(name = "name")
    val name: String,
    @Json(name = "description")
    val description: String?
)

data class JoinVaultRequest(
    @Json(name = "vault_code")
    val vaultCode: String
)

data class InviteMemberRequest(
    @Json(name = "email")
    val email: String,
    @Json(name = "role")
    val role: String = "member"
)