package com.cocido.nonna.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.cocido.nonna.data.local.converter.ListStringConverter
import com.cocido.nonna.data.local.converter.MemoryTypeConverter

/**
 * Entidad Room para recuerdos sensoriales
 */
@Entity(tableName = "memories")
@TypeConverters(MemoryTypeConverter::class, ListStringConverter::class)
data class MemoryEntity(
    @PrimaryKey
    val id: String,
    val vaultId: String,
    val title: String,
    val type: String, // MemoryType como String
    val photoLocalPath: String?,
    val photoRemoteUrl: String?,
    val audioLocalPath: String?,
    val audioRemoteUrl: String?,
    val hasTranscript: Boolean,
    val transcript: String?,
    val people: List<String>, // Lista de PersonId como String
    val tags: List<String>,
    val dateTaken: Long?,
    val location: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: String = "PENDING_SYNC" // Para estrategia offline-first
)


