package com.cocido.nonna.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para frases típicas familiares
 */
@Entity(tableName = "phrases")
data class PhraseEntity(
    @PrimaryKey
    val id: String,
    val vaultId: String,
    val text: String,
    val audioLocalPath: String?,
    val audioRemoteUrl: String?,
    val personId: String?,
    val createdAt: Long,
    val updatedAt: Long
)


