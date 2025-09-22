package com.cocido.nonna.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para frases de conversación
 */
@Entity(tableName = "phrases")
data class PhraseEntity(
    @PrimaryKey
    val id: String,
    val text: String,
    val audioPath: String?,
    val personId: String?,
    val createdAt: Long,
    val updatedAt: Long
)