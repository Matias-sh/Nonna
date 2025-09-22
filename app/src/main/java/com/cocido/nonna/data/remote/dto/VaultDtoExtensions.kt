package com.cocido.nonna.data.remote.dto

import com.cocido.nonna.domain.model.Vault
import com.cocido.nonna.domain.model.VaultId
import java.time.Instant
import java.time.format.DateTimeFormatter

fun VaultDto.toDomain(): Vault {
    return Vault(
        id = VaultId(id), // id is already a String (UUID)
        name = name,
        ownerUid = owner.toString(),
        memberUids = members?.map { it.user.toString() } ?: emptyList(),
        createdAt = parseDate(createdAt)
    )
}

fun Vault.toDto(): CreateVaultRequest {
    return CreateVaultRequest(
        name = name,
        description = null // TODO: Agregar description al modelo de dominio si es necesario
    )
}

private fun parseDate(dateString: String): Long {
    return try {
        Instant.parse(dateString).toEpochMilli()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}
