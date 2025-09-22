package com.cocido.nonna.data.remote.dto

import com.cocido.nonna.data.local.entity.MemoryEntity

/**
 * Extensión para convertir DTO a entidad local
 */
fun MemoryDto.dtoToEntity(): MemoryEntity {
    return MemoryEntity(
        id = id,
        vaultId = vault,
        title = title,
        type = type,
        photoLocalPath = null,
        photoRemoteUrl = photo,
        audioLocalPath = null,
        audioRemoteUrl = audio,
        hasTranscript = false,
        transcript = null,
        people = emptyList(),
        tags = tags,
        dateTaken = dateTaken?.let { parseDate(it) },
        location = location,
        createdAt = parseDate(createdAt) ?: System.currentTimeMillis(),
        updatedAt = parseDate(updatedAt) ?: System.currentTimeMillis(),
        syncStatus = "SYNCED"
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