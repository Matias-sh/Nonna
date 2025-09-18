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
        vaultId = VaultId(vaultId),
        title = title,
        type = MemoryType.valueOf(type),
        photoLocalPath = null, // Los DTOs no incluyen rutas locales
        photoRemoteUrl = photoRemoteUrl,
        audioLocalPath = null, // Los DTOs no incluyen rutas locales
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

fun MemoryDto.toEntity(): MemoryEntity {
    return MemoryEntity(
        id = id,
        vaultId = vaultId,
        title = title,
        type = type,
        photoLocalPath = null,
        photoRemoteUrl = photoRemoteUrl,
        audioLocalPath = null,
        audioRemoteUrl = audioRemoteUrl,
        hasTranscript = hasTranscript,
        transcript = transcript,
        people = people,
        tags = tags,
        dateTaken = dateTaken,
        location = location,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = "SYNCED"
    )
}

fun MemoryEntity.toDto(): MemoryDto {
    return MemoryDto(
        id = id,
        vaultId = vaultId,
        title = title,
        type = type,
        photoRemoteUrl = photoRemoteUrl,
        audioRemoteUrl = audioRemoteUrl,
        hasTranscript = hasTranscript,
        transcript = transcript,
        people = people,
        tags = tags,
        dateTaken = dateTaken,
        location = location,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun MemoryDto.toRequest(): MemoryRequest {
    return MemoryRequest(
        title = title,
        type = type,
        hasTranscript = hasTranscript,
        transcript = transcript,
        people = people,
        tags = tags,
        dateTaken = dateTaken,
        location = location
    )
}

fun Memory.toRequest(): MemoryRequest {
    return MemoryRequest(
        title = title,
        type = type.name,
        hasTranscript = hasTranscript,
        transcript = transcript,
        people = people.map { it.value },
        tags = tags,
        dateTaken = dateTaken,
        location = location
    )
}

