package com.cocido.nonna.ui.screens.profile

import com.cocido.nonna.data.remote.dto.SuscripcionActualDto
import com.cocido.nonna.data.remote.dto.UserDto

data class ProfileUiState(
    val user: UserDto? = null,
    val suscripcion: SuscripcionActualDto? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

