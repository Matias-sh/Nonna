package com.cocido.nonna.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.LaunchedEffect
import com.cocido.nonna.R
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.util.FormValidators

data class InviteData(
    val emails: List<String>
)

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun InviteFamilyModal(
    cofreName: String,
    onDismiss: () -> Unit,
    onInvite: (InviteData) -> Unit
) {
    var emailInput by remember { mutableStateOf("") }
    var emails by remember { mutableStateOf<List<String>>(emptyList()) }
    val canSubmit = emails.isNotEmpty() || FormValidators.isValidEmail(emailInput.trim())
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val overlayVisible = remember { MutableTransitionState(false) }
        LaunchedEffect(Unit) { overlayVisible.targetState = true }
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visibleState = overlayVisible,
                enter = fadeIn(tween(200, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(140)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.48f))
                        .clickable(onClick = onDismiss)
                )
            }
            AnimatedVisibility(
                visibleState = overlayVisible,
                enter = fadeIn(tween(260, delayMillis = 30, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        initialScale = 0.9f,
                        animationSpec = tween(280, delayMillis = 30, easing = FastOutSlowInEasing)
                    ) +
                    slideInVertically(
                        animationSpec = tween(280, delayMillis = 30, easing = FastOutSlowInEasing)
                    ) { it / 10 },
                exit = fadeOut(tween(140)) + scaleOut(targetScale = 0.96f) +
                    slideOutVertically(animationSpec = tween(140)) { it / 12 },
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
            ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            shape = NonnaCorners.ExtraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(NonnaDimens.cardPaddingLarge),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = NonnaCorners.Full
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = stringResource(R.string.invite_family_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = cofreName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.common_close)
                        )
                    }
                }
                
                // Content
                Column(
                    modifier = Modifier.padding(
                        start = NonnaDimens.cardPaddingLarge,
                        end = NonnaDimens.cardPaddingLarge,
                        bottom = NonnaDimens.cardPaddingLarge
                    )
                ) {
                    // Email
                    NonnaTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it.filterNot { ch -> ch.isWhitespace() } },
                        label = stringResource(R.string.invite_email_required),
                        placeholder = stringResource(R.string.invite_email_placeholder),
                        leadingIcon = Icons.Outlined.Email,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIconContent = {
                            InviteEmailTrailingAddPill(
                                emailInput = emailInput,
                                existingEmails = emails,
                                onAdded = { normalized ->
                                    emails = emails + normalized
                                    emailInput = ""
                                }
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val n = emailInput.trim().lowercase()
                                if (n.isNotBlank() && FormValidators.isValidEmail(n) &&
                                    !emails.any { it.equals(n, ignoreCase = true) }
                                ) {
                                    emails = emails + n
                                    emailInput = ""
                                }
                            }
                        )
                    )

                    if (emails.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            emails.forEach { email ->
                                TagChip(
                                    text = email,
                                    onRemove = { emails = emails - email }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Info
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = NonnaCorners.Medium
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.invite_info_text),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Actions
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NonnaButton(
                            text = stringResource(R.string.invite_send),
                            onClick = {
                                val fallbackEmail = emailInput.trim()
                                val finalEmails = if (emails.isEmpty() && FormValidators.isValidEmail(fallbackEmail)) {
                                    listOf(fallbackEmail)
                                } else {
                                    emails
                                }
                                onInvite(InviteData(emails = finalEmails))
                            },
                            enabled = canSubmit,
                            fullWidth = true
                        )
                        NonnaButton(
                            text = stringResource(R.string.common_cancel),
                            onClick = onDismiss,
                            style = NonnaButtonStyle.Outline,
                            fullWidth = true
                        )
                    }
                }
            }
        }
            }
        }
    }
}

@Composable
private fun TagChip(
    text: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = NonnaCorners.Full,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.common_remove),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
