package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTO para requests de frases típicas familiares
 */
data class PhraseRequest(
    @Json(name = "text")
    val text: String,
    @Json(name = "audio_remote_url")
    val audioRemoteUrl: String?,
    @Json(name = "person_id")
    val personId: String?
)




