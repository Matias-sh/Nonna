package com.cocido.nonna.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.NonnaTheme

enum class NonnaButtonStyle {
    Primary,
    Secondary,
    Outline,
    Ghost,
    Destructive
}

enum class NonnaButtonSize {
    Small,
    Medium,
    Large
}

@Composable
fun NonnaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: NonnaButtonStyle = NonnaButtonStyle.Primary,
    size: NonnaButtonSize = NonnaButtonSize.Medium,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.Start,
    fullWidth: Boolean = false
) {
    val interactionSource = rememberMotionInteractionSource()
    val elevation = nonnaButtonElevation(style)
    val buttonModifier = if (fullWidth) {
        modifier.fillMaxWidth()
    } else {
        modifier
    }.nonnaInteractiveScale(interactionSource = interactionSource, pressed = 0.985f)
    
    val height = when (size) {
        NonnaButtonSize.Small -> NonnaDimens.buttonHeightSmall
        NonnaButtonSize.Medium -> NonnaDimens.buttonHeight
        NonnaButtonSize.Large -> NonnaDimens.buttonHeightLarge
    }
    
    val contentPadding = when (size) {
        NonnaButtonSize.Small -> PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        NonnaButtonSize.Medium -> PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        NonnaButtonSize.Large -> PaddingValues(horizontal = 32.dp, vertical = 14.dp)
    }
    
    val iconSize = when (size) {
        NonnaButtonSize.Small -> 16.dp
        NonnaButtonSize.Medium -> 20.dp
        NonnaButtonSize.Large -> 24.dp
    }
    
    when (style) {
        NonnaButtonStyle.Primary -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier.height(height),
                enabled = enabled,
                interactionSource = interactionSource,
                shape = NonnaCorners.Button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                ),
                elevation = elevation,
                contentPadding = contentPadding
            ) {
                ButtonContent(text, icon, iconPosition, iconSize)
            }
        }
        
        NonnaButtonStyle.Secondary -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier.height(height),
                enabled = enabled,
                interactionSource = interactionSource,
                shape = NonnaCorners.Button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f)
                ),
                elevation = elevation,
                contentPadding = contentPadding
            ) {
                ButtonContent(text, icon, iconPosition, iconSize)
            }
        }
        
        NonnaButtonStyle.Outline -> {
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier.height(height),
                enabled = enabled,
                interactionSource = interactionSource,
                shape = NonnaCorners.Button,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    }
                ),
                elevation = elevation,
                contentPadding = contentPadding
            ) {
                ButtonContent(text, icon, iconPosition, iconSize)
            }
        }
        
        NonnaButtonStyle.Ghost -> {
            TextButton(
                onClick = onClick,
                modifier = buttonModifier.height(height),
                enabled = enabled,
                interactionSource = interactionSource,
                shape = NonnaCorners.Button,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                elevation = elevation,
                contentPadding = contentPadding
            ) {
                ButtonContent(text, icon, iconPosition, iconSize)
            }
        }
        
        NonnaButtonStyle.Destructive -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier.height(height),
                enabled = enabled,
                interactionSource = interactionSource,
                shape = NonnaCorners.Button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onError.copy(alpha = 0.5f)
                ),
                elevation = elevation,
                contentPadding = contentPadding
            ) {
                ButtonContent(text, icon, iconPosition, iconSize)
            }
        }
    }
}

@Composable
private fun nonnaButtonElevation(style: NonnaButtonStyle): ButtonElevation {
    return when (style) {
        NonnaButtonStyle.Primary,
        NonnaButtonStyle.Secondary,
        NonnaButtonStyle.Destructive -> ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp,
            focusedElevation = 4.dp,
            hoveredElevation = 4.dp,
            disabledElevation = 0.dp
        )

        NonnaButtonStyle.Outline -> ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp,
            focusedElevation = 1.dp,
            hoveredElevation = 1.dp,
            disabledElevation = 0.dp
        )

        NonnaButtonStyle.Ghost -> ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 1.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            disabledElevation = 0.dp
        )
    }
}

enum class IconPosition {
    Start,
    End
}

@Composable
private fun ButtonContent(
    text: String,
    icon: ImageVector?,
    iconPosition: IconPosition,
    iconSize: androidx.compose.ui.unit.Dp
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null && iconPosition == IconPosition.Start) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Text(text = text)
        
        if (icon != null && iconPosition == IconPosition.End) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, backgroundColor = 0xFFFAF7F0)
@Composable
private fun NonnaButtonPreview() {
    NonnaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NonnaButton(text = "Primary Button", onClick = {})
            NonnaButton(text = "Secondary", onClick = {}, style = NonnaButtonStyle.Secondary)
            NonnaButton(text = "Outline", onClick = {}, style = NonnaButtonStyle.Outline)
            NonnaButton(text = "Ghost", onClick = {}, style = NonnaButtonStyle.Ghost)
            NonnaButton(text = "Destructive", onClick = {}, style = NonnaButtonStyle.Destructive)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF7F0)
@Composable
private fun NonnaButtonWithIconPreview() {
    NonnaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NonnaButton(
                text = "Con icono",
                onClick = {},
                icon = Icons.Default.Add
            )
            NonnaButton(
                text = "Icono al final",
                onClick = {},
                icon = Icons.Default.Favorite,
                iconPosition = IconPosition.End
            )
            NonnaButton(
                text = "Full Width",
                onClick = {},
                fullWidth = true
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF7F0)
@Composable
private fun NonnaButtonSizesPreview() {
    NonnaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NonnaButton(text = "Small", onClick = {}, size = NonnaButtonSize.Small)
            NonnaButton(text = "Medium", onClick = {}, size = NonnaButtonSize.Medium)
            NonnaButton(text = "Large", onClick = {}, size = NonnaButtonSize.Large)
            NonnaButton(text = "Disabled", onClick = {}, enabled = false)
        }
    }
}
