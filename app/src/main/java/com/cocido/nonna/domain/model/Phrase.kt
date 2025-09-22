package com.cocido.nonna.domain.model

/**
 * Modelo de dominio para una frase de conversación
 */
data class Phrase(
    val id: PhraseId,
    val text: String,
    val audioPath: String?,
    val personId: PersonId?,
    val createdAt: Long,
    val updatedAt: Long
)