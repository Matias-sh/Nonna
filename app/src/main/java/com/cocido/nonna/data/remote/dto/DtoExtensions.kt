package com.cocido.nonna.data.remote.dto

import com.cocido.nonna.data.local.entity.MemoryEntity
import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.MemoryId
import com.cocido.nonna.domain.model.MemoryType
import com.cocido.nonna.domain.model.PersonId
import com.cocido.nonna.domain.model.VaultId

/**
 * Extensiones para convertir entre DTOs y entidades/modelos de dominio
 */

fun MemoryDto.toDomain(): Memory {
    return Memory(
        id = MemoryId(id),
        vaultId = VaultId(vault),
        title = title,
        type = mapStringToMemoryType(type),
        photoLocalPath = null, // Los DTOs no incluyen rutas locales
        photoRemoteUrl = photo,
        audioLocalPath = null, // Los DTOs no incluyen rutas locales
        audioRemoteUrl = audio,
        hasTranscript = false, // No disponible en el DTO actual
        transcript = null,
        people = emptyList(), // No disponible en el DTO actual
        tags = tags,
        dateTaken = dateTaken?.let { 
            try {
                java.time.LocalDate.parse(it).atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC) * 1000
            } catch (e: Exception) {
                null
            }
        },
        location = location,
        createdAt = try {
            java.time.LocalDateTime.parse(createdAt).toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        } catch (e: Exception) {
            System.currentTimeMillis()
        },
        updatedAt = try {
            java.time.LocalDateTime.parse(updatedAt).toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    )
}

fun MemoryDto.toEntity(): MemoryEntity {
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
        dateTaken = dateTaken?.let { 
            try {
                java.time.LocalDate.parse(it).atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC) * 1000
            } catch (e: Exception) {
                null
            }
        },
        location = location,
        createdAt = try {
            java.time.LocalDateTime.parse(createdAt).toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        } catch (e: Exception) {
            System.currentTimeMillis()
        },
        updatedAt = try {
            java.time.LocalDateTime.parse(updatedAt).toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        } catch (e: Exception) {
            System.currentTimeMillis()
        },
        syncStatus = "SYNCED"
    )
}

fun MemoryEntity.toDto(): MemoryDto {
    return MemoryDto(
        id = id,
        title = title,
        description = "",
        type = type,
        photo = photoRemoteUrl,
        audio = audioRemoteUrl,
        video = null,
        dateTaken = dateTaken?.let { 
            java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneOffset.UTC).toLocalDate().toString()
        },
        location = location ?: "",
        tags = tags,
        vault = vaultId,
        vaultName = "",
        createdBy = "",
        createdByName = "",
        likesCount = 0,
        commentsCount = 0,
        isLiked = false,
        createdAt = java.time.Instant.ofEpochMilli(createdAt).atZone(java.time.ZoneOffset.UTC).toLocalDateTime().toString(),
        updatedAt = java.time.Instant.ofEpochMilli(updatedAt).atZone(java.time.ZoneOffset.UTC).toLocalDateTime().toString()
    )
}

fun MemoryDto.toRequest(): MemoryRequest {
    return MemoryRequest(
        title = title,
        description = description,
        type = type,
        dateTaken = dateTaken,
        location = location,
        tags = tags,
        vault = vault
    )
}

fun Memory.toRequest(): MemoryRequest {
    return MemoryRequest(
        title = title,
        description = "",
        type = mapMemoryTypeToString(type),
        dateTaken = dateTaken?.let {
            java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneOffset.UTC).toLocalDate().toString()
        },
        location = location ?: "",
        tags = tags,
        vault = vaultId.value
    )
}

private fun mapStringToMemoryType(typeString: String): MemoryType {
    return when (typeString.lowercase()) {
        "photo" -> MemoryType.PHOTO_ONLY
        "audio" -> MemoryType.AUDIO_ONLY
        "video" -> MemoryType.PHOTO_ONLY // Map video to photo_only since VIDEO_ONLY doesn't exist
        "photo_with_audio" -> MemoryType.PHOTO_WITH_AUDIO
        "recipe" -> MemoryType.RECIPE
        "note" -> MemoryType.NOTE
        else -> MemoryType.PHOTO_ONLY
    }
}

private fun mapMemoryTypeToString(type: MemoryType): String {
    return when (type) {
        MemoryType.PHOTO_ONLY -> "photo"
        MemoryType.AUDIO_ONLY -> "audio"
        MemoryType.PHOTO_WITH_AUDIO -> "photo_with_audio"
        MemoryType.RECIPE -> "recipe"
        MemoryType.NOTE -> "note"
    }
}

