package com.cocido.nonna.ui.permissions

import com.cocido.nonna.data.remote.dto.UserDto
import com.cocido.nonna.ui.components.MemoryUiModel

/** Solo quien creó el recuerdo puede editarlo o eliminarlo (regla del backend AR01). */
fun canModifyMemory(memory: MemoryUiModel, currentUser: UserDto?): Boolean {
    if (currentUser == null) return false

    val memoryUserId = memory.addedByUserId?.trim().orEmpty()
    val currentUserId = currentUser.id.trim()
    if (memoryUserId.isNotBlank() && currentUserId.isNotBlank() && memoryUserId == currentUserId) {
        return true
    }

    val memoryEmail = memory.addedByEmail?.trim()?.lowercase().orEmpty()
    val currentEmail = currentUser.email.trim().lowercase()
    return memoryEmail.isNotBlank() && memoryEmail == currentEmail
}
