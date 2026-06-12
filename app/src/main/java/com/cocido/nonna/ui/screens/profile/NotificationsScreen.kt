package com.cocido.nonna.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.nonna.R
import com.cocido.nonna.ui.components.FilterChip
import com.cocido.nonna.ui.components.FilterChipsRow
import com.cocido.nonna.ui.components.NotificationItemCard
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonSize
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.NonnaDetailScaffold
import com.cocido.nonna.ui.components.PageHeader
import com.cocido.nonna.ui.components.ScreenTitleSection
import com.cocido.nonna.ui.components.RefreshOnResume
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaSpacing
import com.cocido.nonna.ui.viewmodel.NotificationsViewModel
import java.util.Locale

@Composable
fun NotificationsRoute(
    onBack: () -> Unit,
    onOpenSubscriptionCenter: (paymentId: String?) -> Unit = {},
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val onlyUnread by viewModel.onlyUnread.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val markingReadIds by viewModel.markingReadIds.collectAsStateWithLifecycle()

    val uiState = NotificationsUiState(
        items = items,
        isLoading = isLoading,
        isLoadingMore = isLoadingMore,
        onlyUnread = onlyUnread,
        errorMessage = errorMessage,
        markingReadIds = markingReadIds
    )

    LaunchedEffect(Unit) {
        viewModel.load()
    }
    RefreshOnResume(minIntervalMs = 2000L) { viewModel.refreshOnResume() }

    NotificationsScreen(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                NotificationsEvent.Back -> onBack()
                NotificationsEvent.LoadInitial -> viewModel.load(forceRefresh = true)
                NotificationsEvent.LoadMore -> viewModel.loadMore()
                is NotificationsEvent.ToggleOnlyUnread -> viewModel.toggleOnlyUnread(event.enabled)
                is NotificationsEvent.MarkAsRead -> viewModel.markAsRead(event.id)
                is NotificationsEvent.OpenSubscriptionCenter -> {
                    viewModel.markAsRead(event.notificationId)
                    onOpenSubscriptionCenter(event.paymentId)
                }
                NotificationsEvent.ClearError -> viewModel.clearError()
            }
        }
    )
}

@Composable
fun NotificationsScreen(
    uiState: NotificationsUiState,
    onEvent: (NotificationsEvent) -> Unit
) {
    val tabs = listOf(
        FilterChip("all", stringResource(R.string.notifications_all_tab)),
        FilterChip("unread", stringResource(R.string.notifications_unread_tab))
    )

    NonnaDetailScaffold {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            PageHeader(
                onBack = { onEvent(NotificationsEvent.Back) }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = NonnaDimens.screenPaddingHorizontal)
            ) {
                ScreenTitleSection(
                    title = stringResource(R.string.notifications_title),
                    subtitle = stringResource(R.string.notifications_subtitle)
                )
                Spacer(modifier = Modifier.height(NonnaSpacing.lg))
                FilterChipsRow(
                    chips = tabs,
                    selectedChipId = if (uiState.onlyUnread) "unread" else "all",
                    onChipSelected = { selected ->
                        onEvent(NotificationsEvent.ToggleOnlyUnread(selected == "unread"))
                    },
                    scrollable = false
                )
                Spacer(modifier = Modifier.height(NonnaSpacing.lg))

                when {
                    uiState.isLoading && uiState.items.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            androidx.compose.material3.CircularProgressIndicator()
                        }
                    }

                    uiState.items.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(R.string.notifications_empty_state),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(NonnaSpacing.md)
                        ) {
                            items(uiState.items, key = { it.id }) { item ->
                                NotificationItemCard(
                                    item = item,
                                    markingAsRead = uiState.markingReadIds.contains(item.id),
                                    onMarkAsRead = { onEvent(NotificationsEvent.MarkAsRead(item.id)) },
                                    onOpenSubscriptionCenter = if (item.requiresSubscriptionCenterCta()) {
                                        {
                                            onEvent(
                                                NotificationsEvent.OpenSubscriptionCenter(
                                                    notificationId = item.id,
                                                    paymentId = item.paymentId
                                                )
                                            )
                                        }
                                    } else {
                                        null
                                    }
                                )
                            }
                        }
                    }
                }

                if (!uiState.errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(NonnaSpacing.md))
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(NonnaSpacing.sm))
                    NonnaButton(
                        text = stringResource(R.string.notifications_clear_message),
                        onClick = { onEvent(NotificationsEvent.ClearError) },
                        style = NonnaButtonStyle.Outline,
                        size = NonnaButtonSize.Small
                    )
                }

                Spacer(modifier = Modifier.height(NonnaSpacing.md))
                NonnaButton(
                    text = if (uiState.isLoadingMore) {
                        stringResource(R.string.notifications_loading_more)
                    } else {
                        stringResource(R.string.notifications_load_more)
                    },
                    onClick = { onEvent(NotificationsEvent.LoadMore) },
                    enabled = !uiState.isLoadingMore,
                    style = NonnaButtonStyle.Outline,
                    fullWidth = true
                )
                Spacer(modifier = Modifier.height(NonnaSpacing.lg))
            }
        }
    }
}

private fun com.cocido.nonna.data.repository.NotificationUiModel.requiresSubscriptionCenterCta(): Boolean {
    val normalizedType = type.trim().uppercase(Locale.ROOT)
    if (normalizedType == "SUSCRIPCION_RENOVACION_REQUERIDA") return true

    val normalizedPayloadType = payloadType?.trim()?.lowercase(Locale.ROOT).orEmpty()
    return normalizedPayloadType == "suscripcion_renovacion_requerida"
}

