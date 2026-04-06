package com.cocido.nonna.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners

data class InviteData(
    val email: String,
    val role: CofreRole,
    val message: String
)

@Composable
fun InviteFamilyModal(
    cofreName: String,
    onDismiss: () -> Unit,
    onInvite: (InviteData) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(CofreRole.Colaborador) }
    var message by remember { mutableStateOf("Te invito a construir el cofre de $cofreName juntos.") }
    
    val roles = listOf(
        RoleOption(
            role = CofreRole.Colaborador,
            description = "Puede agregar y editar sus propios recuerdos"
        ),
        RoleOption(
            role = CofreRole.Invitado,
            description = "Solo puede ver los recuerdos"
        ),
        RoleOption(
            role = CofreRole.Abuelo,
            description = "Modo simplificado para abuelos/as"
        )
    )
    
    val canSubmit = email.isNotBlank() && email.contains("@")
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                                text = "Invitar a la familia",
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
                            contentDescription = "Cerrar"
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
                        value = email,
                        onValueChange = { email = it },
                        label = "Email *",
                        placeholder = "familiar@email.com",
                        leadingIcon = Icons.Outlined.Email,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Role selection
                    Text(
                        text = "Rol",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        roles.forEach { roleOption ->
                            val isSelected = selectedRole == roleOption.role
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(NonnaCorners.Medium)
                                    .background(
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outline
                                        },
                                        shape = NonnaCorners.Medium
                                    )
                                    .clickable { selectedRole = roleOption.role }
                                    .padding(NonnaDimens.cardPadding)
                            ) {
                                Column {
                                    Text(
                                        text = roleOption.role.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = roleOption.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Personal message
                    NonnaTextArea(
                        value = message,
                        onValueChange = { message = it },
                        label = "Mensaje personal (opcional)",
                        placeholder = "Agregá un mensaje cálido...",
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
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
                            text = "Le enviaremos un email de invitación con tu mensaje y un link para unirse al cofre",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NonnaButton(
                            text = "Cancelar",
                            onClick = onDismiss,
                            style = NonnaButtonStyle.Outline,
                            modifier = Modifier.weight(1f)
                        )
                        NonnaButton(
                            text = "Enviar invitación",
                            onClick = {
                                onInvite(InviteData(
                                    email = email,
                                    role = selectedRole,
                                    message = message
                                ))
                            },
                            enabled = canSubmit,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

private data class RoleOption(
    val role: CofreRole,
    val description: String
)
