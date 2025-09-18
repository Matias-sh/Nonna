package com.cocido.nonna.data.local.entity

import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.MemoryId
import com.cocido.nonna.domain.model.MemoryType
import com.cocido.nonna.domain.model.PersonId
import com.cocido.nonna.domain.model.VaultId

/**
 * Extensiones para convertir entre MemoryEntity y Memory
 */

fun MemoryEntity.toDomain(): Memory {
    return Memory(
        id = MemoryId(id),
        vaultId = VaultId(vaultId),
        title = title,
        type = MemoryType.valueOf(type),
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

fun Memory.toEntity(): MemoryEntity {
    return MemoryEntity(
        id = id.value,
        vaultId = vaultId.value,
        title = title,
        type = type.name,
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