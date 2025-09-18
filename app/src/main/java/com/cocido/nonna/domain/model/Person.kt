package com.cocido.nonna.domain.model

/**
 * Modelo de dominio para una persona en el árbol genealógico
 */
data class Person(
    val id: PersonId,
    val fullName: String,
    val birthDate: Long?,          // Timestamp de nacimiento
    val deathDate: Long?,          // Timestamp de fallecimiento (null si está vivo)
    val avatarUrl: String?,        // URL del avatar/foto de perfil
    val notes: String?             // Notas adicionales sobre la persona
)

