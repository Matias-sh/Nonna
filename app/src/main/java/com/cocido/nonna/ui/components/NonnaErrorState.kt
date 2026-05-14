package com.cocido.nonna.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.nonna.ui.theme.NonnaTheme

/**
 * Base error state with optional retry action.
 */
@Composable
fun NonnaErrorState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    retryLabel: String? = null,
    onRetry: (() -> Unit)? = null
) {
    EmptyState(
        icon = Icons.Outlined.Warning,
        title = title,
        description = description,
        modifier = modifier,
        action = if (!retryLabel.isNullOrBlank() && onRetry != null) {
            {
                NonnaButton(
                    text = retryLabel,
                    onClick = onRetry,
                    style = NonnaButtonStyle.Primary
                )
            }
        } else {
            null
        }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF7F0)
@Composable
private fun NonnaErrorStatePreview() {
    NonnaTheme {
        NonnaErrorState(
            title = "Algo salio mal",
            description = "No pudimos cargar la informacion.",
            retryLabel = "Reintentar",
            onRetry = {}
        )
    }
}

