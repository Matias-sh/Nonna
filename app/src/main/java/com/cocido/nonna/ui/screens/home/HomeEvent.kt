package com.cocido.nonna.ui.screens.home

import com.cocido.nonna.ui.components.NonnaTab

sealed interface HomeEvent {
    data class SelectTab(val tab: NonnaTab) : HomeEvent
    data object CreateCofre : HomeEvent
    data object AddMemory : HomeEvent
    data class ContinueCofre(val cofreId: String) : HomeEvent
    data object OpenInvitations : HomeEvent
    data class AcceptInvitation(val invitationId: String) : HomeEvent
    data object ClearInvitationAcceptError : HomeEvent
    data object DismissFeaturedInvitation : HomeEvent
}

