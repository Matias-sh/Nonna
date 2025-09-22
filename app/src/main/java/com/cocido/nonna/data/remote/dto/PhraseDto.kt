package com.cocido.nonna.data.remote.dto

import com.squareup.moshi.Json

/**
 * DTOs para frases de conversación
 */

data class PhraseDto(
    @Json(name = "id")
    val id: String,
    @Json(name = "text")
    val text: String,
    @Json(name = "translation")
    val translation: String,
    @Json(name = "category")
    val category: String,
    @Json(name = "context")
    val context: String,
    @Json(name = "person_mentioned")
    val personMentioned: String,
    @Json(name = "language")
    val language: String,
    @Json(name = "audio_file")
    val audioFile: String?,
    @Json(name = "audio_duration")
    val audioDuration: Double?,
    @Json(name = "tags")
    val tags: List<String>,
    @Json(name = "is_favorite")
    val isFavorite: Boolean,
    @Json(name = "usage_count")
    val usageCount: Int,
    @Json(name = "playbacks_count")
    val playbacksCount: Int,
    @Json(name = "vault")
    val vault: String,
    @Json(name = "vault_name")
    val vaultName: String,
    @Json(name = "created_by")
    val createdBy: String,
    @Json(name = "created_by_name")
    val createdByName: String,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "updated_at")
    val updatedAt: String
)

data class PhraseCreateRequest(
    @Json(name = "text")
    val text: String,
    @Json(name = "translation")
    val translation: String = "",
    @Json(name = "category")
    val category: String = "other",
    @Json(name = "context")
    val context: String = "",
    @Json(name = "person_mentioned")
    val personMentioned: String = "",
    @Json(name = "language")
    val language: String = "es",
    @Json(name = "tags")
    val tags: List<String> = emptyList(),
    @Json(name = "is_favorite")
    val isFavorite: Boolean = false,
    @Json(name = "vault")
    val vault: String
)

data class PhrasePlaybackDto(
    @Json(name = "id")
    val id: String,
    @Json(name = "phrase")
    val phrase: String,
    @Json(name = "phrase_text")
    val phraseText: String,
    @Json(name = "user")
    val user: String,
    @Json(name = "user_name")
    val userName: String,
    @Json(name = "played_at")
    val playedAt: String,
    @Json(name = "duration_played")
    val durationPlayed: Double?
)

