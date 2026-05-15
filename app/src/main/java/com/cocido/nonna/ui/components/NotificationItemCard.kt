package com.cocido.nonna.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.nonna.R
import com.cocido.nonna.data.repository.NotificationUiModel
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.NonnaSpacing
import com.cocido.nonna.ui.theme.NonnaTheme
import androidx.compose.ui.res.stringResource

@Composable
fun NotificationItemCard(
    item: NotificationUiModel,
    markingAsRead: Boolean,
    onMarkAsRead: () -> Unit,
    onOpenSubscriptionCenter: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    NonnaCard(
        modifier = modifier.fillMaxWidth(),
        variant = if (item.isRead) NonnaCardVariant.Elevated else NonnaCardVariant.Filled
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (!item.isRead) {
                    Text(
                        text = stringResource(R.string.notifications_badge_new),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = NonnaCorners.Small
                            )
                            .padding(horizontal = NonnaSpacing.sm, vertical = NonnaSpacing.xs)
                    )
                }
            }

            if (item.message.isNotBlank()) {
                Spacer(modifier = Modifier.height(NonnaSpacing.sm))
                Text(
                    text = item.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item.createdAt?.takeIf { it.isNotBlank() }?.let { created ->
                Spacer(modifier = Modifier.height(NonnaSpacing.sm))
                Text(
                    text = created,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!item.isRead) {
                Spacer(modifier = Modifier.height(NonnaSpacing.md))
                NonnaButton(
                    text = if (markingAsRead) {
                        stringResource(R.string.notifications_marking_read)
                    } else {
                        stringResource(R.string.notifications_mark_read)
                    },
                    onClick = onMarkAsRead,
                    enabled = !markingAsRead,
                    style = NonnaButtonStyle.Outline,
                    size = NonnaButtonSize.Small
                )
            }

            if (onOpenSubscriptionCenter != null) {
                Spacer(modifier = Modifier.height(NonnaSpacing.sm))
                NonnaButton(
                    text = stringResource(R.string.notifications_open_subscription),
                    onClick = onOpenSubscriptionCenter,
                    style = NonnaButtonStyle.Primary,
                    size = NonnaButtonSize.Small
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF7F0)
@Composable
private fun NotificationItemCardPreviewUnread() {
    NonnaTheme {
        NotificationItemCard(
            item = NotificationUiModel(
                id = 1,
                title = "Nueva invitación",
                message = "Te invitaron al cofre de Ana",
                type = "invite",
                payloadType = "invitacion_cofre",
                paymentId = null,
                isRead = false,
                createdAt = "hace 5 minutos"
            ),
            markingAsRead = false,
            onMarkAsRead = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF7F0)
@Composable
private fun NotificationItemCardPreviewRead() {
    NonnaTheme {
        NotificationItemCard(
            item = NotificationUiModel(
                id = 2,
                title = "Recordatorio",
                message = "Ya podés ver tus recuerdos",
                type = "reminder",
                payloadType = null,
                paymentId = null,
                isRead = true,
                createdAt = "ayer"
            ),
            markingAsRead = false,
            onMarkAsRead = {}
        )
    }
}

