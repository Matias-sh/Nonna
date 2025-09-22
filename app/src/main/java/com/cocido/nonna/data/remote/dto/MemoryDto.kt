package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTO para recuerdos sensoriales
 */
data class MemoryDto(
    @Json(name = "id")
    val id: String,
    @Json(name = "title")
    val title: String,
    @Json(name = "description")
    val description: String,
    @Json(name = "type")
    val type: String,
    @Json(name = "photo")
    val photo: String?,
    @Json(name = "audio")
    val audio: String?,
    @Json(name = "video")
    val video: String?,
    @Json(name = "date_taken")
    val dateTaken: String?,
    @Json(name = "location")
    val location: String,
    @Json(name = "tags")
    val tags: List<String>,
    @Json(name = "vault")
    val vault: String,
    @Json(name = "vault_name")
    val vaultName: String,
    @Json(name = "created_by")
    val createdBy: String,
    @Json(name = "created_by_name")
    val createdByName: String,
    @Json(name = "likes_count")
    val likesCount: Int,
    @Json(name = "comments_count")
    val commentsCount: Int,
    @Json(name = "is_liked")
    val isLiked: Boolean,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "updated_at")
    val updatedAt: String
)



