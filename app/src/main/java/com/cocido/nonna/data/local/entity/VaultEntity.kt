package com.cocido.nonna.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para baúles
 */
@Entity(tableName = "vaults")
data class VaultEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String?,
    val ownerId: String,
    val memberCount: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: String = "PENDING_SYNC"
)