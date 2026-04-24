package com.cocido.nonna.ui.permissions

import com.cocido.nonna.data.remote.dto.UserDto
import com.cocido.nonna.ui.components.CofreUiModel

fun canManageCofre(cofre: CofreUiModel?): Boolean = cofre?.isOwner == true

fun resolveOwnership(cofre: CofreUiModel, currentUser: UserDto?): CofreUiModel {
    val userEmail = currentUser?.email?.trim()?.lowercase().orEmpty()
    if (userEmail.isBlank()) return cofre

    val ownerEmailNormalized = cofre.ownerEmail?.trim()?.lowercase()
    val isExplicitOwner = ownerEmailNormalized != null && ownerEmailNormalized == userEmail
    val appearsAsInvited = cofre.invited.any { it.email.trim().lowercase() == userEmail }

    val normalizedOwner = when {
        isExplicitOwner -> true
        appearsAsInvited -> false
        ownerEmailNormalized.isNullOrBlank() -> cofre.isOwner
        else -> false
    }
    return if (normalizedOwner == cofre.isOwner) cofre else cofre.copy(isOwner = normalizedOwner)
}
