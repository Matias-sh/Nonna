package com.cocido.nonna.domain.model

/**
 * Modelo de dominio para una relación familiar entre dos personas
 */
data class Relation(
    val from: PersonId,        // Persona origen de la relación
    val to: PersonId,          // Persona destino de la relación
    val type: RelationType     // Tipo de relación familiar
)

