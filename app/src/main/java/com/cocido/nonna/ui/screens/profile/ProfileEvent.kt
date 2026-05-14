package com.cocido.nonna.ui.screens.profile

import com.cocido.nonna.ui.components.NonnaTab

sealed interface ProfileEvent {
    data class SelectTab(val tab: NonnaTab) : ProfileEvent
    data object EditProfile : ProfileEvent
    data object OpenSubscriptionCenter : ProfileEvent
    data object OpenInvitations : ProfileEvent
    data object Logout : ProfileEvent
}

