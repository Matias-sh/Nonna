package com.cocido.nonna.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cocido.nonna.ui.components.MemoryType
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.NonnaDetailScaffold
import com.cocido.nonna.ui.components.NonnaTextArea
import com.cocido.nonna.ui.components.NonnaTextField
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.PrimaryGradientEnd
import com.cocido.nonna.ui.theme.PrimaryGradientStart

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var cofreName by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var inviteEmails by remember { mutableStateOf("") }
    var selectedMemoryType by remember { mutableStateOf<MemoryType?>(null) }
    
    NonnaDetailScaffold {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // Progress bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(NonnaDimens.spacing24)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(NonnaCorners.Full)
                                .background(
                                    if (index < currentStep) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Paso $currentStep de 3",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(NonnaDimens.spacing24),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (currentStep) {
                1 -> OnboardingStep1(
                    cofreName = cofreName,
                    onCofreNameChange = { cofreName = it },
                    relation = relation,
                    onRelationChange = { relation = it },
                    description = description,
                    onDescriptionChange = { description = it }
                )
                2 -> OnboardingStep2(
                    inviteEmails = inviteEmails,
                    onInviteEmailsChange = { inviteEmails = it }
                )
                3 -> OnboardingStep3(
                    selectedType = selectedMemoryType,
                    onTypeSelected = { selectedMemoryType = it }
                )
            }
        }
        
        // Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NonnaDimens.spacing24),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 1) {
                TextButton(onClick = {
                    if (currentStep == 2 || currentStep == 3) {
                        onComplete()
                    }
                }) {
                    Text(
                        text = "Saltar por ahora",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            
            NonnaButton(
                text = if (currentStep == 3) "Crear cofre" else "Continuar",
                onClick = {
                    if (currentStep < 3) {
                        currentStep++
                    } else {
                        onComplete()
                    }
                },
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                iconPosition = com.cocido.nonna.ui.components.IconPosition.End,
                enabled = currentStep != 1 || cofreName.isNotBlank()
            )
        }
        }
    }
}

@Composable
private fun OnboardingStep1(
    cofreName: String,
    onCofreNameChange: (String) -> Unit,
    relation: String,
    onRelationChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PrimaryGradientStart.copy(alpha = 0.2f),
                            PrimaryGradientEnd.copy(alpha = 0.2f)
                        )
                    ),
                    shape = NonnaCorners.Large
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Inventory2,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "¿A quién vamos a honrar?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Creá el primer cofre para preservar sus memorias",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        NonnaTextField(
            value = cofreName,
            onValueChange = onCofreNameChange,
            label = "Nombre del cofre",
            placeholder = "Ej: Nonna Rosa, Abuelo Juan...",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        NonnaTextField(
            value = relation,
            onValueChange = onRelationChange,
            label = "Parentesco",
            placeholder = "Ej: Abuela, Abuelo, Tío, Tía...",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        NonnaTextArea(
            value = description,
            onValueChange = onDescriptionChange,
            label = "Una frase que la/lo describe (opcional)",
            placeholder = "Ej: La mejor cocinera del mundo, Siempre con una sonrisa...",
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun OnboardingStep2(
    inviteEmails: String,
    onInviteEmailsChange: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PrimaryGradientStart.copy(alpha = 0.2f),
                            PrimaryGradientEnd.copy(alpha = 0.2f)
                        )
                    ),
                    shape = NonnaCorners.Large
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.People,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Invitá a tu familia",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Este cofre es para construirlo juntos. Podés invitar a más personas después.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        NonnaTextArea(
            value = inviteEmails,
            onValueChange = onInviteEmailsChange,
            label = "Emails (separados por coma)",
            placeholder = "mama@email.com, hermana@email.com",
            minLines = 4,
            helperText = "Les enviaremos una invitación cálida para unirse al cofre",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = NonnaCorners.Medium
                )
                .padding(16.dp)
        ) {
            Text(
                text = "Cada persona que invites podrá agregar sus propios recuerdos y fotos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OnboardingStep3(
    selectedType: MemoryType?,
    onTypeSelected: (MemoryType) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PrimaryGradientStart.copy(alpha = 0.2f),
                            PrimaryGradientEnd.copy(alpha = 0.2f)
                        )
                    ),
                    shape = NonnaCorners.Large
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Favorite,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Guardá tu primer recuerdo",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Empecemos con algo simple. Podés agregar más después.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MemoryTypeOption(
                icon = Icons.Outlined.Image,
                title = "Foto",
                description = "Una imagen especial",
                isSelected = selectedType == MemoryType.Photo,
                onClick = { onTypeSelected(MemoryType.Photo) },
                modifier = Modifier.weight(1f)
            )
            
            MemoryTypeOption(
                icon = Icons.Outlined.AudioFile,
                title = "Audio",
                description = "Su voz o una historia",
                isSelected = selectedType == MemoryType.Audio,
                onClick = { onTypeSelected(MemoryType.Audio) },
                modifier = Modifier.weight(1f)
            )
            
            MemoryTypeOption(
                icon = Icons.Outlined.Description,
                title = "Texto",
                description = "Una anécdota o receta",
                isSelected = selectedType == MemoryType.Text,
                onClick = { onTypeSelected(MemoryType.Text) },
                modifier = Modifier.weight(1f)
            )
        }
        
        if (selectedType != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = NonnaCorners.Medium
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = "Perfecto. Vamos a crear tu cofre y después te ayudamos a subir tu primer recuerdo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun MemoryTypeOption(
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    Column(
        modifier = modifier
            .clip(NonnaCorners.Large)
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = NonnaCorners.Large
            )
            .clickable(onClick = onClick)
            .padding(NonnaDimens.spacing16),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
