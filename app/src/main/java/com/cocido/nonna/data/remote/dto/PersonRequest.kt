package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTO para requests de personas
 */
data class PersonRequest(
    @Json(name = "full_name")
    val fullName: String,
    @Json(name = "birth_date")
    val birthDate: Long?,
    @Json(name = "death_date")
    val deathDate: Long?,
    @Json(name = "avatar_url")
    val avatarUrl: String?,
    @Json(name = "notes")
    val notes: String?
)





