package com.cocido.nonna.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cocido.nonna.R
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.util.FormValidators

/**
 * Pastilla "Agregar" alineada con el campo de email (invitaciones / crear cofre).
 */
@Composable
fun InviteEmailTrailingAddPill(
    emailInput: String,
    existingEmails: List<String>,
    onAdded: (normalizedEmail: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val normalized = emailInput.trim().lowercase()
    val canAdd = normalized.isNotBlank() &&
        FormValidators.isValidEmail(normalized) &&
        !existingEmails.any { it.equals(normalized, ignoreCase = true) }
    Surface(
        modifier = modifier,
        shape = NonnaCorners.Full,
        color = if (canAdd) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        }
    ) {
        Text(
            text = stringResource(R.string.common_add),
            modifier = Modifier
                .clickable(enabled = canAdd) {
                    onAdded(normalized)
                }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (canAdd) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            }
        )
    }
}
