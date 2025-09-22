package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTO para requests de relaciones familiares
 */
data class RelationRequest(
    @Json(name = "from_person_id")
    val fromPersonId: String,
    @Json(name = "to_person_id")
    val toPersonId: String,
    @Json(name = "type")
    val type: String
)




