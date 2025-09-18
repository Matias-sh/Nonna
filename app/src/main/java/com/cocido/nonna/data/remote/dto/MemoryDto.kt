package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTO para recuerdos sensoriales
 */
data class MemoryDto(
    @Json(name = "id")
    val id: String,
    @Json(name = "vault_id")
    val vaultId: String,
    @Json(name = "title")
    val title: String,
    @Json(name = "type")
    val type: String,
    @Json(name = "photo_remote_url")
    val photoRemoteUrl: String?,
    @Json(name = "audio_remote_url")
    val audioRemoteUrl: String?,
    @Json(name = "has_transcript")
    val hasTranscript: Boolean,
    @Json(name = "transcript")
    val transcript: String?,
    @Json(name = "people")
    val people: List<String>,
    @Json(name = "tags")
    val tags: List<String>,
    @Json(name = "date_taken")
    val dateTaken: Long?,
    @Json(name = "location")
    val location: String?,
    @Json(name = "created_at")
    val createdAt: Long,
    @Json(name = "updated_at")
    val updatedAt: Long
)


