package com.cocido.nonna.domain.model

/**
 * Modelo de dominio para un baúl de recuerdos
 */
data class Vault(
    val id: VaultId,
    val name: String,
    val description: String?,
    val ownerId: UserId,
    val memberCount: Int,
    val createdAt: Long,
    val updatedAt: Long
)