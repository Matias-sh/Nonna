package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTO para crear/actualizar recuerdos sensoriales
 */
data class MemoryRequest(
    @Json(name = "title")
    val title: String,
    @Json(name = "description")
    val description: String = "",
    @Json(name = "type")
    val type: String,
    @Json(name = "date_taken")
    val dateTaken: String? = null,
    @Json(name = "location")
    val location: String = "",
    @Json(name = "tags")
    val tags: List<String> = emptyList(),
    @Json(name = "vault")
    val vault: String
)