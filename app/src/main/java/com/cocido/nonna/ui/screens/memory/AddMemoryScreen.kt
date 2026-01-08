package com.cocido.nonna.ui.screens.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cocido.nonna.ui.components.AudioRecorderComponent
import com.cocido.nonna.ui.components.EmotionalTag
import com.cocido.nonna.ui.components.MemoryType
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.NonnaTextArea
import com.cocido.nonna.ui.components.NonnaTextField
import com.cocido.nonna.ui.components.PageHeader
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AddMemoryStep {
    Type, Content, Details
}

@Composable
fun AddMemoryScreen(
    cofreId: String?,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    var step by remember { mutableStateOf(AddMemoryStep.Type) }
    var selectedType by remember { mutableStateOf<MemoryType?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var textContent by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var emotionalTag by remember { mutableStateOf<EmotionalTag?>(null) }
    var hasAudioRecording by remember { mutableStateOf(false) }
    var hasImageSelected by remember { mutableStateOf(false) }
    
    val stepTitle = when (step) {
        AddMemoryStep.Type -> "Elegí el tipo de recuerdo"
        AddMemoryStep.Content -> "Subí o creá el contenido"
        AddMemoryStep.Details -> "Agregá los detalles"
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        PageHeader(
            title = "Agregar Recuerdo",
            subtitle = stepTitle,
            onBack = {
                when (step) {
                    AddMemoryStep.Type -> onBack()
                    AddMemoryStep.Content -> step = AddMemoryStep.Type
                    AddMemoryStep.Details -> step = AddMemoryStep.Content
                }
            }
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(NonnaDimens.screenPaddingHorizontal)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            when (step) {
                AddMemoryStep.Type -> TypeSelectionStep(
                    onTypeSelected = { type ->
                        selectedType = type
                        step = AddMemoryStep.Content
                    }
                )
                
                AddMemoryStep.Content -> ContentStep(
                    type = selectedType!!,
                    textContent = textContent,
                    onTextContentChange = { textContent = it },
                    hasAudioRecording = hasAudioRecording,
                    onAudioRecorded = { hasAudioRecording = true },
                    hasImageSelected = hasImageSelected,
                    onImageSelected = { hasImageSelected = true },
                    onBackToType = { step = AddMemoryStep.Type },
                    onContinue = { step = AddMemoryStep.Details }
                )
                
                AddMemoryStep.Details -> DetailsStep(
                    type = selectedType!!,
                    title = title,
                    onTitleChange = { title = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    textContent = textContent,
                    date = date,
                    onDateChange = { date = it },
                    emotionalTag = emotionalTag,
                    onEmotionalTagChange = { emotionalTag = it },
                    onBack = { step = AddMemoryStep.Content },
                    onSave = onSave,
                    canSave = title.isNotBlank()
                )
            }
        }
    }
}

@Composable
private fun TypeSelectionStep(
    onTypeSelected: (MemoryType) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¿Qué tipo de recuerdo querés guardar?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TypeOption(
                icon = Icons.Outlined.Image,
                title = "Foto",
                description = "Una imagen especial o documento",
                onClick = { onTypeSelected(MemoryType.Photo) },
                modifier = Modifier.weight(1f)
            )
            
            TypeOption(
                icon = Icons.Outlined.AudioFile,
                title = "Audio",
                description = "Su voz, una canción o historia",
                onClick = { onTypeSelected(MemoryType.Audio) },
                modifier = Modifier.weight(1f)
            )
            
            TypeOption(
                icon = Icons.Outlined.Description,
                title = "Texto",
                description = "Una anécdota, receta o carta",
                onClick = { onTypeSelected(MemoryType.Text) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TypeOption(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(NonnaCorners.Large)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = NonnaCorners.Large
            )
            .clickable(onClick = onClick)
            .padding(NonnaDimens.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
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

@Composable
private fun ContentStep(
    type: MemoryType,
    textContent: String,
    onTextContentChange: (String) -> Unit,
    hasAudioRecording: Boolean,
    onAudioRecorded: () -> Unit,
    hasImageSelected: Boolean,
    onImageSelected: () -> Unit,
    onBackToType: () -> Unit,
    onContinue: () -> Unit
) {
    Column {
        when (type) {
            MemoryType.Photo -> PhotoContent(
                hasImage = hasImageSelected,
                onImageSelected = onImageSelected
            )
            MemoryType.Audio -> AudioContent(
                hasRecording = hasAudioRecording,
                onRecorded = onAudioRecorded
            )
            MemoryType.Text -> TextContent(
                content = textContent,
                onContentChange = onTextContentChange
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        TextButton(
            onClick = onBackToType,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Volver a elegir tipo",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // For text, show continue button
        if (type == MemoryType.Text && textContent.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NonnaButton(
                    text = "Volver",
                    onClick = onBackToType,
                    style = NonnaButtonStyle.Outline,
                    modifier = Modifier.weight(1f)
                )
                NonnaButton(
                    text = "Continuar",
                    onClick = onContinue,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PhotoContent(
    hasImage: Boolean,
    onImageSelected: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(NonnaCorners.Large)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = NonnaCorners.Large
            )
            .clickable(onClick = onImageSelected),
        contentAlignment = Alignment.Center
    ) {
        if (hasImage) {
            // Show image preview
            Text(
                text = "Imagen seleccionada",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.Upload,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Subir foto",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "o arrastrá una imagen aquí",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AudioContent(
    hasRecording: Boolean,
    onRecorded: () -> Unit
) {
    AudioRecorderComponent(
        onRecordingComplete = { onRecorded() }
    )
}

@Composable
private fun TextContent(
    content: String,
    onContentChange: (String) -> Unit
) {
    NonnaTextArea(
        value = content,
        onValueChange = onContentChange,
        label = "Escribí el recuerdo",
        placeholder = "Contá la historia, la receta, la anécdota...",
        minLines = 12,
        helperText = "Escribí con calma. No hay apuro.",
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailsStep(
    type: MemoryType,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    textContent: String,
    date: String,
    onDateChange: (String) -> Unit,
    emotionalTag: EmotionalTag?,
    onEmotionalTagChange: (EmotionalTag?) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean
) {
    Column {
        // Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = NonnaCorners.Large
                )
                .padding(NonnaDimens.cardPaddingLarge)
        ) {
            when (type) {
                MemoryType.Photo -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Foto seleccionada",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                MemoryType.Audio -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AudioFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Audio grabado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                MemoryType.Text -> {
                    Text(
                        text = textContent.take(200) + if (textContent.length > 200) "..." else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        NonnaTextField(
            value = title,
            onValueChange = onTitleChange,
            label = "Título *",
            placeholder = "Ej: Cumpleaños de los 80, Receta de fideos...",
            modifier = Modifier.fillMaxWidth()
        )
        
        // Description (only for photo/audio)
        if (type != MemoryType.Text) {
            Spacer(modifier = Modifier.height(16.dp))
            
            NonnaTextArea(
                value = description,
                onValueChange = onDescriptionChange,
                label = "Contexto o historia (opcional)",
                placeholder = "Contá algo sobre este recuerdo...",
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Date
        NonnaTextField(
            value = date,
            onValueChange = onDateChange,
            label = "Fecha (aproximada)",
            leadingIcon = Icons.Outlined.CalendarMonth,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Emotional tag
        Text(
            text = "¿Cómo te hace sentir? (opcional)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EmotionalTag.entries.forEach { tag ->
                val isSelected = emotionalTag == tag
                
                Surface(
                    onClick = {
                        onEmotionalTagChange(if (isSelected) null else tag)
                    },
                    shape = NonnaCorners.Medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = tag.emoji,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tag.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NonnaButton(
                text = "Volver",
                onClick = onBack,
                style = NonnaButtonStyle.Outline,
                modifier = Modifier.weight(1f)
            )
            NonnaButton(
                text = "Guardar recuerdo",
                onClick = onSave,
                enabled = canSave,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
