package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTO para personas del árbol genealógico
 */
data class PersonDto(
    @Json(name = "id")
    val id: String,
    @Json(name = "full_name")
    val fullName: String,
    @Json(name = "birth_date")
    val birthDate: Long?,
    @Json(name = "death_date")
    val deathDate: Long?,
    @Json(name = "avatar_url")
    val avatarUrl: String?,
    @Json(name = "notes")
    val notes: String?,
    @Json(name = "created_at")
    val createdAt: Long,
    @Json(name = "updated_at")
    val updatedAt: Long
)


