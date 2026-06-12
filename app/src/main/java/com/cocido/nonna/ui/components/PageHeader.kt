package com.cocido.nonna.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cocido.nonna.R
import com.cocido.nonna.ui.theme.NonnaDimens

private val BackButtonSize = 36.dp

private val BackButtonStartPadding =
    (NonnaDimens.screenPaddingHorizontal - (BackButtonSize - NonnaDimens.iconSizeLarge) / 2)
        .coerceAtLeast(0.dp)

data class HeaderAction(
    val label: String,
    val icon: ImageVector? = null,
    val onClick: () -> Unit
)

@Composable
fun NonnaBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .padding(start = BackButtonStartPadding)
            .size(BackButtonSize)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.common_back),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun NonnaHeaderIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(BackButtonSize)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

@Composable
fun ScreenTitleSection(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PageHeader(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    showTitleAndSubtitle: Boolean = false,
    elevated: Boolean = true,
    useSurface: Boolean = true,
    onBack: (() -> Unit)? = null,
    action: HeaderAction? = null,
    trailingIcons: (@Composable RowScope.() -> Unit)? = null
) {
    val verticalPadding = if (showTitleAndSubtitle) {
        NonnaDimens.spacing16
    } else {
        NonnaDimens.spacing8
    }

    val headerContent: @Composable () -> Unit = {
        BoxWithConstraints {
            val compactHeader = maxWidth < 360.dp
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = verticalPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (onBack != null) {
                            NonnaBackButton(onClick = onBack)
                        }

                        if (showTitleAndSubtitle && !title.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (!subtitle.isNullOrBlank()) {
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        } else if (onBack != null) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    if (trailingIcons != null) {
                        Row(
                            modifier = Modifier.padding(end = BackButtonStartPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            content = trailingIcons
                        )
                    } else if (action != null && !compactHeader) {
                        Spacer(modifier = Modifier.width(12.dp))
                        NonnaButton(
                            text = action.label,
                            onClick = action.onClick,
                            icon = action.icon,
                            size = NonnaButtonSize.Small,
                            modifier = Modifier.padding(end = NonnaDimens.screenPaddingHorizontal)
                        )
                    }
                }

                if (action != null && compactHeader) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = NonnaDimens.screenPaddingHorizontal),
                        horizontalArrangement = Arrangement.End
                    ) {
                        NonnaButton(
                            text = action.label,
                            onClick = action.onClick,
                            icon = action.icon,
                            size = NonnaButtonSize.Small
                        )
                    }
                }
            }
        }
    }

    if (useSurface) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = if (elevated) 1.dp else 0.dp
        ) {
            headerContent()
        }
    } else {
        headerContent()
    }
}

@Composable
fun SimpleHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = NonnaDimens.screenPaddingHorizontal,
                vertical = NonnaDimens.spacing24
            )
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
