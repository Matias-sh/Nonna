package com.cocido.nonna.domain.model

/**
 * Modelo de dominio para un recuerdo sensorial
 * Representa un recuerdo que puede incluir foto, audio, metadatos y etiquetas
 */
data class Memory(
    val id: MemoryId,
    val vaultId: VaultId,
    val title: String,
    val type: MemoryType,
    val photoLocalPath: String?,   // Ruta local cifrada si aplica
    val photoRemoteUrl: String?,   // URL remota provista por backend
    val audioLocalPath: String?,   // Ruta local del audio m4a cifrado
    val audioRemoteUrl: String?,   // URL remota del audio provista por backend
    val hasTranscript: Boolean,
    val transcript: String?,
    val people: List<PersonId>,
    val tags: List<String>,
    val dateTaken: Long?,          // Timestamp de cuando se tomó la foto
    val location: String?,
    val createdAt: Long,
    val updatedAt: Long
)

