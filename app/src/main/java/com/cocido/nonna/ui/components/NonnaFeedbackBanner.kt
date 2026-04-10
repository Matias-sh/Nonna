package com.cocido.nonna.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cocido.nonna.ui.theme.NonnaCorners

enum class NonnaFeedbackType { Success, Error, Info }

@Composable
fun NonnaBottomFeedbackBanner(
    visible: Boolean,
    message: String,
    type: NonnaFeedbackType,
    includeNavigationBarsPadding: Boolean = true,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector
    val iconTint = when (type) {
        NonnaFeedbackType.Success -> MaterialTheme.colorScheme.primary
        NonnaFeedbackType.Error -> MaterialTheme.colorScheme.error
        NonnaFeedbackType.Info -> MaterialTheme.colorScheme.secondary
    }
    icon = when (type) {
        NonnaFeedbackType.Success -> Icons.Outlined.CheckCircle
        NonnaFeedbackType.Error -> Icons.Outlined.ErrorOutline
        NonnaFeedbackType.Info -> Icons.Outlined.Info
    }

    AnimatedVisibility(
        visible = visible && message.isNotBlank(),
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier
            .fillMaxWidth()
            .then(if (includeNavigationBarsPadding) Modifier.navigationBarsPadding() else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Surface(
            shape = NonnaCorners.Card,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
