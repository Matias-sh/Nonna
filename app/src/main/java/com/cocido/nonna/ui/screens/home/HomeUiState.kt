package com.cocido.nonna.ui.screens.home

import com.cocido.nonna.data.remote.dto.UserDto
import com.cocido.nonna.ui.components.CofreInvitationUiModel
import com.cocido.nonna.ui.components.CofreUiModel

data class HomeUiState(
    val cofres: List<CofreUiModel> = emptyList(),
    val user: UserDto? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val featuredInvitation: CofreInvitationUiModel? = null,
    val invitationAcceptLoading: Boolean = false,
    val invitationAcceptError: String? = null
)

