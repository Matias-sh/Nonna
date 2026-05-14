package com.cocido.nonna.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.VisualTransformation
import com.cocido.nonna.ui.theme.NonnaSpacing
import com.cocido.nonna.ui.theme.InputBackground
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners

@Composable
fun NonnaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    label: String? = null,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    trailingIconContent: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    errorMessage: String? = null,
    helperText: String? = null,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isFocused -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }
    
    Column(modifier = modifier) {
        // Label
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Spacer(modifier = Modifier.height(NonnaSpacing.sm))
        }
        
        // Input field
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .let { base -> if (testTag != null) base.testTag(testTag) else base },
            enabled = enabled,
            readOnly = readOnly,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(NonnaCorners.Input)
                        .then(
                            if (onClick != null) {
                                Modifier.clickable(onClick = onClick)
                            } else {
                                Modifier
                            }
                        )
                        .background(InputBackground)
                        .border(
                            width = NonnaDimens.borderWidth,
                            color = borderColor,
                            shape = NonnaCorners.Input
                        )
                        .padding(horizontal = NonnaDimens.inputPadding)
                        .heightIn(min = NonnaDimens.inputHeight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(if (singleLine) Alignment.CenterStart else Alignment.TopStart)
                            .padding(vertical = if (!singleLine) NonnaSpacing.md else NonnaSpacing.none),
                        verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
                    ) {
                        if (leadingIcon != null) {
                            Icon(
                                imageVector = leadingIcon,
                                contentDescription = null,
                                modifier = Modifier.size(NonnaDimens.inputIconSize),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(NonnaSpacing.md))
                        }
                        
                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                        
                        if (trailingIcon != null) {
                            Spacer(modifier = Modifier.width(NonnaSpacing.md))
                            Icon(
                                imageVector = trailingIcon,
                                contentDescription = null,
                                modifier = Modifier.size(NonnaDimens.inputIconSize),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (trailingIconContent != null) {
                            Spacer(modifier = Modifier.width(NonnaSpacing.md))
                            trailingIconContent()
                        }
                    }
                }
            }
        )
        
        // Helper/Error text
        if (errorMessage != null && isError) {
            Spacer(modifier = Modifier.height(NonnaSpacing.xs))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else if (helperText != null) {
            Spacer(modifier = Modifier.height(NonnaSpacing.xs))
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NonnaTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    minLines: Int = 4,
    maxLines: Int = 8,
    isError: Boolean = false,
    errorMessage: String? = null,
    helperText: String? = null
) {
    NonnaTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = false,
        minLines = minLines,
        maxLines = maxLines,
        isError = isError,
        errorMessage = errorMessage,
        helperText = helperText
    )
}
