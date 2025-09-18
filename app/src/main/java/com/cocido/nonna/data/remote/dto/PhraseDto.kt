package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTO para frases típicas familiares
 */
data class PhraseDto(
    @Json(name = "id")
    val id: String,
    @Json(name = "vault_id")
    val vaultId: String,
    @Json(name = "text")
    val text: String,
    @Json(name = "audio_remote_url")
    val audioRemoteUrl: String?,
    @Json(name = "person_id")
    val personId: String?,
    @Json(name = "created_at")
    val createdAt: Long,
    @Json(name = "updated_at")
    val updatedAt: Long
)


