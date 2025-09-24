package com.cocido.nonna.domain.model

/**
 * Modelo de dominio para un usuario
 */
data class User(
    val id: UserId,
    val email: String,
    val name: String,
    val profileImageUrl: String?,
    val createdAt: Long,
    val updatedAt: Long
)

