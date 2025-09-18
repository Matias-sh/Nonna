package com.cocido.nonna.domain.model

/**
 * Modelo de dominio para un cofre familiar
 * Representa un espacio colaborativo donde los familiares pueden compartir recuerdos
 */
data class Vault(
    val id: VaultId,
    val name: String,              // Nombre del cofre familiar
    val ownerUid: String,          // UID del propietario del cofre
    val memberUids: List<String>,  // Lista de UIDs de miembros del cofre
    val createdAt: Long            // Timestamp de creación
)

