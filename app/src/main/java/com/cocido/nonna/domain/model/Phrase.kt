package com.cocido.nonna.domain.model

/**
 * Modelo de dominio para una frase típica familiar
 * Usado en el modo conversación para reproducir frases características
 */
data class Phrase(
    val id: String,
    val vaultId: VaultId,
    val text: String,             // Texto de la frase
    val audioLocalPath: String?,  // Ruta local del audio (si existe)
    val audioRemoteUrl: String?,  // URL remota del audio
    val personId: PersonId?,      // Persona que dijo la frase
    val createdAt: Long,
    val updatedAt: Long
)

