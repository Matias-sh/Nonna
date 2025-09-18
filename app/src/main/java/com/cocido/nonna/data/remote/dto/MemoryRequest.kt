package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTO para crear/actualizar recuerdos sensoriales
 */
data class MemoryRequest(
    @Json(name = "title")
    val title: String,
    @Json(name = "type")
    val type: String,
    @Json(name = "has_transcript")
    val hasTranscript: Boolean = false,
    @Json(name = "transcript")
    val transcript: String? = null,
    @Json(name = "people")
    val people: List<String> = emptyList(),
    @Json(name = "tags")
    val tags: List<String> = emptyList(),
    @Json(name = "date_taken")
    val dateTaken: Long? = null,
    @Json(name = "location")
    val location: String? = null
)