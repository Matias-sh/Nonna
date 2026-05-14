package com.cocido.nonna.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaElevation
import com.cocido.nonna.ui.theme.NonnaTheme

enum class NonnaCardVariant {
    Elevated,
    Outlined,
    Filled
}

/**
 * Base card wrapper for consistent DS card styles.
 */
@Composable
fun NonnaCard(
    modifier: Modifier = Modifier,
    variant: NonnaCardVariant = NonnaCardVariant.Elevated,
    contentPadding: PaddingValues = PaddingValues(NonnaDimens.cardPadding),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = NonnaCorners.Card
    val colors = CardDefaults.cardColors(
        containerColor = when (variant) {
            NonnaCardVariant.Elevated,
            NonnaCardVariant.Outlined -> MaterialTheme.colorScheme.surface
            NonnaCardVariant.Filled -> MaterialTheme.colorScheme.surfaceVariant
        }
    )

    val elevation = CardDefaults.cardElevation(
        defaultElevation = when (variant) {
            NonnaCardVariant.Elevated -> NonnaElevation.lg
            NonnaCardVariant.Outlined,
            NonnaCardVariant.Filled -> NonnaElevation.none
        }
    )

    val border = if (variant == NonnaCardVariant.Outlined) {
        BorderStroke(NonnaDimens.borderWidth, MaterialTheme.colorScheme.outline)
    } else {
        null
    }

    val innerContent: @Composable () -> Unit = {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = { innerContent() }
        )
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = { innerContent() }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF7F0)
@Composable
private fun NonnaCardPreview() {
    NonnaTheme {
        NonnaCard {
            Text("Card elevated")
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF7F0)
@Composable
private fun NonnaCardOutlinedPreview() {
    NonnaTheme {
        NonnaCard(variant = NonnaCardVariant.Outlined) {
            Text("Card outlined")
        }
    }
}

