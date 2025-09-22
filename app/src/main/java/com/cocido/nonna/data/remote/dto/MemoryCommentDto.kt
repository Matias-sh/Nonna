package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTOs para comentarios de recuerdos
 */

data class MemoryCommentDto(
    @Json(name = "id")
    val id: String,
    @Json(name = "text")
    val text: String,
    @Json(name = "user")
    val user: String,
    @Json(name = "user_name")
    val userName: String,
    @Json(name = "user_avatar")
    val userAvatar: String?,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "updated_at")
    val updatedAt: String
)

data class MemoryCommentCreateRequest(
    @Json(name = "text")
    val text: String
)

data class MemoryLikeDto(
    @Json(name = "user")
    val user: String,
    @Json(name = "user_name")
    val userName: String,
    @Json(name = "created_at")
    val createdAt: String
)
