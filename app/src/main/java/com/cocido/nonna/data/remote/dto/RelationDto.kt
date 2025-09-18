package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTO para relaciones familiares
 */
data class RelationDto(
    @Json(name = "id")
    val id: Long,
    @Json(name = "from_person_id")
    val fromPersonId: String,
    @Json(name = "to_person_id")
    val toPersonId: String,
    @Json(name = "type")
    val type: String,
    @Json(name = "created_at")
    val createdAt: Long
)


