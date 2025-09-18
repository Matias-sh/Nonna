package com.cocido.nonna.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para personas del árbol genealógico
 */
@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey
    val id: String,
    val fullName: String,
    val birthDate: Long?,
    val deathDate: Long?,
    val avatarUrl: String?,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long
)


