package com.cocido.nonna.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.PrimaryGradientEnd
import com.cocido.nonna.ui.theme.PrimaryGradientStart
import com.cocido.nonna.ui.theme.TagAlegreBackground
import com.cocido.nonna.ui.theme.TagAlegreText
import com.cocido.nonna.ui.theme.TagCalmoBackground
import com.cocido.nonna.ui.theme.TagCalmoText
import com.cocido.nonna.ui.theme.TagFamiliarBackground
import com.cocido.nonna.ui.theme.TagFamiliarText
import com.cocido.nonna.ui.theme.TagNostalgicoBackground
import com.cocido.nonna.ui.theme.TagNostalgicoText
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.nonna.ui.theme.NonnaTheme

enum class MemoryType {
    Photo, Audio, Text
}

enum class EmotionalTag(
    val label: String,
    val backgroundColor: Color,
    val textColor: Color
) {
    Alegre("Alegre", TagAlegreBackground, TagAlegreText),
    Nostalgico("Nostálgico", TagNostalgicoBackground, TagNostalgicoText),
    Calmo("Calmo", TagCalmoBackground, TagCalmoText),
    Familiar("Familiar", TagFamiliarBackground, TagFamiliarText)
}

data class MemoryUiModel(
    val id: String,
    val type: MemoryType,
    val title: String,
    val description: String? = null,
    val date: String,
    val emotionalTag: EmotionalTag? = null,
    val thumbnailUrl: String? = null,
    val audioUrl: String? = null,
    val duration: String? = null // For audio
)

enum class MemoryCardViewMode {
    Grid, List
}

@Composable
fun MemoryCard(
    memory: MemoryUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewMode: MemoryCardViewMode = MemoryCardViewMode.Grid
) {
    if (viewMode == MemoryCardViewMode.List) {
        MemoryCardList(memory = memory, onClick = onClick, modifier = modifier)
    } else {
        MemoryCardGrid(memory = memory, onClick = onClick, modifier = modifier)
    }
}

@Composable
private fun MemoryCardGrid(
    memory: MemoryUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = NonnaCorners.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NonnaDimens.memoryCardHeight)
                    .clip(NonnaCorners.ImageMedium)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                PrimaryGradientStart.copy(alpha = 0.2f),
                                PrimaryGradientEnd.copy(alpha = 0.2f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (memory.thumbnailUrl != null) {
                    AsyncImage(
                        model = memory.thumbnailUrl,
                        contentDescription = memory.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(NonnaDimens.memoryCardHeight),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = memory.type.toIcon(),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
                
                // Type badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape = NonnaCorners.Full,
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Icon(
                        imageVector = memory.type.toIcon(),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Duration for audio
                if (memory.duration != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        shape = NonnaCorners.Small,
                        color = Color.Black.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = memory.duration,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            // Content
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = memory.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (memory.description != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = memory.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = memory.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (memory.emotionalTag != null) {
                        EmotionalTagBadge(tag = memory.emotionalTag)
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryCardList(
    memory: MemoryUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = NonnaCorners.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NonnaDimens.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(NonnaDimens.thumbnailSmall)
                    .clip(NonnaCorners.ImageSmall)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                PrimaryGradientStart.copy(alpha = 0.2f),
                                PrimaryGradientEnd.copy(alpha = 0.2f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (memory.thumbnailUrl != null) {
                    AsyncImage(
                        model = memory.thumbnailUrl,
                        contentDescription = memory.title,
                        modifier = Modifier.size(NonnaDimens.thumbnailSmall),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = memory.type.toIcon(),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = memory.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (memory.description != null) {
                    Text(
                        text = memory.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = memory.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (memory.duration != null) {
                        Text(
                            text = " • ${memory.duration}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (memory.emotionalTag != null) {
                Spacer(modifier = Modifier.width(8.dp))
                EmotionalTagBadge(tag = memory.emotionalTag)
            }
        }
    }
}

@Composable
fun EmotionalTagBadge(
    tag: EmotionalTag,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = NonnaCorners.Full,
        color = tag.backgroundColor
    ) {
        Text(
            text = tag.label,
            style = MaterialTheme.typography.labelSmall,
            color = tag.textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

fun MemoryType.toIcon(): ImageVector = when (this) {
    MemoryType.Photo -> Icons.Outlined.Image
    MemoryType.Audio -> Icons.Outlined.AudioFile
    MemoryType.Text -> Icons.Outlined.Description
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, backgroundColor = 0xFFFAF7F0)
@Composable
private fun MemoryCardGridPreview() {
    NonnaTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MemoryCard(
                memory = MemoryUiModel(
                    id = "1",
                    type = MemoryType.Photo,
                    title = "Navidad 1985",
                    description = "La abuela preparando el pan dulce casero",
                    date = "25 Dic 1985",
                    emotionalTag = EmotionalTag.Alegre
                ),
                onClick = {},
                viewMode = MemoryCardViewMode.Grid
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF7F0)
@Composable
private fun MemoryCardListPreview() {
    NonnaTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MemoryCard(
                memory = MemoryUiModel(
                    id = "2",
                    type = MemoryType.Audio,
                    title = "Historia del barrio",
                    description = "Abuelo contando cómo era el barrio",
                    date = "15 Ago 2023",
                    duration = "3:45",
                    emotionalTag = EmotionalTag.Nostalgico
                ),
                onClick = {},
                viewMode = MemoryCardViewMode.List
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF7F0)
@Composable
private fun EmotionalTagsPreview() {
    NonnaTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EmotionalTag.entries.forEach { tag ->
                EmotionalTagBadge(tag = tag)
            }
        }
    }
}
