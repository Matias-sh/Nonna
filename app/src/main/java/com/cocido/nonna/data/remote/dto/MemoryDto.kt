package com.cocido.nonna.data.remote.dto

import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.MemoryId
import com.cocido.nonna.domain.model.MemoryType
import com.cocido.nonna.domain.model.PersonId
import com.cocido.nonna.domain.model.VaultId
import com.google.gson.annotations.SerializedName

/**
 * DTO para recuerdos sensoriales desde la API
 */
data class MemoryDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("photo")
    val photo: String?,
    
    @SerializedName("audio")
    val audio: String?,
    
    @SerializedName("video")
    val video: String?,
    
    @SerializedName("date_taken")
    val dateTaken: String?,
    
    @SerializedName("location")
    val location: String?,
    
    @SerializedName("tags")
    val tags: List<String>,
    
    @SerializedName("vault")
    val vault: String,
    
    @SerializedName("vault_name")
    val vaultName: String?,
    
    @SerializedName("created_by")
    val createdBy: String,
    
    @SerializedName("created_by_name")
    val createdByName: String?,
    
    @SerializedName("likes_count")
    val likesCount: Int,
    
    @SerializedName("comments_count")
    val commentsCount: Int,
    
    @SerializedName("is_liked")
    val isLiked: Boolean,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String
)

/**
 * Extensión para convertir DTO a modelo de dominio
 */
fun MemoryDto.toDomain(): Memory {
    return Memory(
        id = MemoryId(id),
        vaultId = VaultId(vault),
        title = title,
        type = when (type) {
            "photo" -> MemoryType.PHOTO_ONLY
            "audio" -> MemoryType.AUDIO_ONLY
            "video" -> MemoryType.PHOTO_WITH_AUDIO
            "recipe" -> MemoryType.RECIPE
            "note" -> MemoryType.NOTE
            "story" -> MemoryType.NOTE
            else -> MemoryType.PHOTO_ONLY
        },
        photoLocalPath = null,
        photoRemoteUrl = photo,
        audioLocalPath = null,
        audioRemoteUrl = audio,
        hasTranscript = false,
        transcript = null,
        people = emptyList(), // TODO: Implementar cuando esté disponible
        tags = tags,
        dateTaken = dateTaken?.let { parseDate(it) },
        location = location,
        createdAt = parseDate(createdAt) ?: System.currentTimeMillis(),
        updatedAt = parseDate(updatedAt) ?: System.currentTimeMillis()
    )
}

private fun parseDate(dateString: String): Long? {
    return try {
        // Formato ISO 8601: 2024-01-15T10:30:00Z
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
            .parse(dateString)?.time
    } catch (e: Exception) {
        null
    }
}