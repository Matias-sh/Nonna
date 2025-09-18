package com.cocido.nonna.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.cocido.nonna.data.local.converter.ListStringConverter

/**
 * Entidad Room para cofres familiares
 */
@Entity(tableName = "vaults")
@TypeConverters(ListStringConverter::class)
data class VaultEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val ownerUid: String,
    val memberUids: List<String>,
    val createdAt: Long
)


