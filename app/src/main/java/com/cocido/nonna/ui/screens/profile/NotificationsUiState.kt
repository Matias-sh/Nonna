package com.cocido.nonna.ui.screens.profile

import com.cocido.nonna.data.repository.NotificationUiModel

data class NotificationsUiState(
    val items: List<NotificationUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val onlyUnread: Boolean = false,
    val errorMessage: String? = null,
    val markingReadIds: Set<Long> = emptySet()
)

