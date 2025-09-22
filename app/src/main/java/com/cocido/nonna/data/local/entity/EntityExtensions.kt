package com.cocido.nonna.data.local.entity

import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.MemoryId
import com.cocido.nonna.domain.model.MemoryType
import com.cocido.nonna.domain.model.PersonId
import com.cocido.nonna.domain.model.Vault
import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.domain.model.UserId

/**
 * Extensión para convertir entidad a modelo de dominio
 */
fun MemoryEntity.toDomain(): Memory {
    return Memory(
        id = MemoryId(id),
        vaultId = VaultId(vaultId),
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
        photoLocalPath = photoLocalPath,
        photoRemoteUrl = photoRemoteUrl,
        audioLocalPath = audioLocalPath,
        audioRemoteUrl = audioRemoteUrl,
        hasTranscript = hasTranscript,
        transcript = transcript,
        people = people.map { PersonId(it) },
        tags = tags,
        dateTaken = dateTaken,
        location = location,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Extensión para convertir modelo de dominio a entidad
 */
fun Memory.toEntity(): MemoryEntity {
    return MemoryEntity(
        id = id.value,
        vaultId = vaultId.value,
        title = title,
        type = when (type) {
            MemoryType.PHOTO_ONLY -> "photo"
            MemoryType.AUDIO_ONLY -> "audio"
            MemoryType.PHOTO_WITH_AUDIO -> "video"
            MemoryType.RECIPE -> "recipe"
            MemoryType.NOTE -> "note"
        },
        photoLocalPath = photoLocalPath,
        photoRemoteUrl = photoRemoteUrl,
        audioLocalPath = audioLocalPath,
        audioRemoteUrl = audioRemoteUrl,
        hasTranscript = hasTranscript,
        transcript = transcript,
        people = people.map { it.value },
        tags = tags,
        dateTaken = dateTaken,
        location = location,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = "PENDING_SYNC"
    )
}

/**
 * Extensión para convertir entidad de baúl a modelo de dominio
 */
fun VaultEntity.toDomain(): Vault {
    return Vault(
        id = VaultId(id),
        name = name,
        description = description,
        ownerId = UserId(ownerId),
        memberCount = memberCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Extensión para convertir modelo de dominio de baúl a entidad
 */
fun Vault.toEntity(): VaultEntity {
    return VaultEntity(
        id = id.value,
        name = name,
        description = description,
        ownerId = ownerId.value,
        memberCount = memberCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = "PENDING_SYNC"
    )
}
