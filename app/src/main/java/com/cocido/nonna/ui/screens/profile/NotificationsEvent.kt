package com.cocido.nonna.ui.screens.profile

sealed interface NotificationsEvent {
    data object Back : NotificationsEvent
    data object LoadInitial : NotificationsEvent
    data object LoadMore : NotificationsEvent
    data class ToggleOnlyUnread(val enabled: Boolean) : NotificationsEvent
    data class MarkAsRead(val id: Long) : NotificationsEvent
    data class OpenSubscriptionCenter(val notificationId: Long, val paymentId: String?) : NotificationsEvent
    data object ClearError : NotificationsEvent
}

