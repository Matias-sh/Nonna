package com.cocido.nonna.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.cocido.nonna.data.local.converter.RelationTypeConverter

/**
 * Entidad Room para relaciones familiares
 */
@Entity(tableName = "relations")
@TypeConverters(RelationTypeConverter::class)
data class RelationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fromPersonId: String,
    val toPersonId: String,
    val type: String, // RelationType como String
    val createdAt: Long
)





