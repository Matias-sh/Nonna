package com.cocido.nonna.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cocido.nonna.R
import com.cocido.nonna.ui.components.FilterChip
import com.cocido.nonna.ui.components.FilterChipsRow
import com.cocido.nonna.ui.components.NonnaBottomFeedbackBanner
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonSize
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.NonnaDetailScaffold
import com.cocido.nonna.ui.components.NonnaFeedbackType
import com.cocido.nonna.ui.components.PageHeader
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.viewmodel.InvitationsViewModel
import kotlinx.coroutines.flow.collectLatest

private enum class InviteTab { Received, Sent }

@Composable
fun InvitationsScreen(
    onBack: () -> Unit,
    deepLinkedInvitationId: String? = null,
    viewModel: InvitationsViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val received by viewModel.pendingInvitations.collectAsState()
    val sent by viewModel.sentInvitations.collectAsState()
    var selectedTab by remember { mutableStateOf(InviteTab.Received) }
    var feedbackVisible by remember { mutableStateOf(false) }
    var feedbackType by remember { mutableStateOf(NonnaFeedbackType.Success) }
    var feedbackMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(deepLinkedInvitationId) {
        if (!deepLinkedInvitationId.isNullOrBlank()) {
            selectedTab = InviteTab.Received
        }
    }
    LaunchedEffect(Unit) {
        viewModel.successMessage.collectLatest { msg ->
            feedbackType = NonnaFeedbackType.Success
            feedbackMessage = msg
            feedbackVisible = true
        }
    }
    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { msg ->
            feedbackType = NonnaFeedbackType.Error
            feedbackMessage = msg
            feedbackVisible = true
        }
    }

    val tabs = listOf(
        FilterChip(InviteTab.Received.name, stringResource(R.string.invites_received_tab)),
        FilterChip(InviteTab.Sent.name, stringResource(R.string.invites_sent_tab))
    )

    NonnaDetailScaffold {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                PageHeader(
                    title = stringResource(R.string.invites_title),
                    subtitle = stringResource(R.string.invites_subtitle),
                    onBack = onBack
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = NonnaDimens.screenPaddingHorizontal)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    FilterChipsRow(
                        chips = tabs,
                        selectedChipId = selectedTab.name,
                        onChipSelected = { selectedTab = InviteTab.valueOf(it) },
                        scrollable = false
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val activeList = if (selectedTab == InviteTab.Received) received else sent
                    val visibleList = if (selectedTab == InviteTab.Received && !deepLinkedInvitationId.isNullOrBlank()) {
                        activeList.sortedByDescending { it.id == deepLinkedInvitationId }
                    } else {
                        activeList
                    }
                    if (isLoading && activeList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            androidx.compose.material3.CircularProgressIndicator()
                        }
                    } else if (activeList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(R.string.invites_empty_state),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(visibleList, key = { it.id }) { invitation ->
                                val isDeepLinkedTarget = invitation.id == deepLinkedInvitationId
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = NonnaCorners.Card,
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDeepLinkedTarget) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(NonnaDimens.cardPadding)) {
                                        Text(
                                            text = invitation.cofreName.ifBlank { stringResource(R.string.common_chest) },
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val cover = invitation.cofreCoverImageUrl?.takeIf { it.isNotBlank() && it != "string" }
                                        if (cover != null) {
                                            AsyncImage(
                                                model = cover,
                                                contentDescription = invitation.cofreName,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(120.dp)
                                                    .clip(RoundedCornerShape(12.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Inventory2,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                                )
                                                Spacer(modifier = Modifier.size(6.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        invitation.inviterName?.takeIf { it.isNotBlank() }?.let { inviter ->
                                            Text(
                                                text = stringResource(
                                                    R.string.invite_welcome_modal_message_with_name,
                                                    inviter,
                                                    invitation.cofreName.ifBlank { stringResource(R.string.common_chest) }
                                                ),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                        invitation.cofreDescription?.takeIf { it.isNotBlank() }?.let { desc ->
                                            Text(
                                                text = desc,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                        Text(
                                            text = invitation.inviteeEmail,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        if (selectedTab == InviteTab.Received) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                NonnaButton(
                                                    text = stringResource(R.string.common_accept),
                                                    onClick = { viewModel.acceptInvitation(invitation.id) },
                                                    size = NonnaButtonSize.Small,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                NonnaButton(
                                                    text = stringResource(R.string.common_reject),
                                                    onClick = { viewModel.rejectInvitation(invitation.id) },
                                                    style = NonnaButtonStyle.Outline,
                                                    size = NonnaButtonSize.Small,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        } else {
                                            val statusText = when {
                                                invitation.accepted -> stringResource(R.string.invites_status_accepted)
                                                invitation.expired -> stringResource(R.string.invites_status_expired)
                                                else -> stringResource(R.string.invites_status_pending)
                                            }
                                            Text(
                                                text = statusText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (!invitation.accepted) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                NonnaButton(
                                                    text = stringResource(R.string.invites_cancel_button),
                                                    onClick = { viewModel.cancelInvitation(invitation.id) },
                                                    style = NonnaButtonStyle.Outline,
                                                    size = NonnaButtonSize.Small
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            NonnaBottomFeedbackBanner(
                visible = feedbackVisible,
                message = feedbackMessage,
                type = feedbackType,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
