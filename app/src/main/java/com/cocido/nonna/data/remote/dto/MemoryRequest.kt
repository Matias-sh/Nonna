package com.cocido.nonna.data.remote.dto

import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.MemoryType
import com.google.gson.annotations.SerializedName

/**
 * DTO para crear/actualizar recuerdos sensoriales
 */
data class MemoryRequest(
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("photo")
    val photo: String? = null,
    
    @SerializedName("audio")
    val audio: String? = null,
    
    @SerializedName("video")
    val video: String? = null,
    
    @SerializedName("date_taken")
    val dateTaken: String? = null,
    
    @SerializedName("location")
    val location: String? = null,
    
    @SerializedName("tags")
    val tags: List<String> = emptyList(),
    
    @SerializedName("vault")
    val vault: String
)

/**
 * Extensión para convertir modelo de dominio a request
 */
fun Memory.toRequest(): MemoryRequest {
    return MemoryRequest(
        title = title,
        description = null,
        type = when (type) {
            MemoryType.PHOTO_ONLY -> "photo"
            MemoryType.AUDIO_ONLY -> "audio"
            MemoryType.PHOTO_WITH_AUDIO -> "video"
            MemoryType.RECIPE -> "recipe"
            MemoryType.NOTE -> "note"
        },
        photo = photoRemoteUrl,
        audio = audioRemoteUrl,
        video = null,
        dateTaken = dateTaken?.let { formatDate(it) },
        location = location,
        tags = tags,
        vault = vaultId.value
    )
}

private fun formatDate(timestamp: Long): String {
    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
    return formatter.format(java.util.Date(timestamp))
}