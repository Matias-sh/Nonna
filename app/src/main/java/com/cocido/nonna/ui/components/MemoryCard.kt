package com.cocido.nonna.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
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
import com.cocido.nonna.util.MemoryMediaUrlHeuristics
import com.cocido.nonna.R

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
    val emotionalCustomLabel: String? = null,
    val thumbnailUrl: String? = null,
    val audioUrl: String? = null,
    val duration: String? = null, // For audio
    /** Hasta 3 URLs (principal + galería) cuando el tipo es foto. */
    val carouselImageUrls: List<String> = emptyList(),
    /** Portada opcional para recuerdos de audio. */
    val audioCoverUrl: String? = null,
    /** URL del archivo principal (PATCH multipart `urlArchivo` si no se reemplaza el archivo). */
    val mainMediaUrl: String? = null
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
private fun MemoryUiModel.coverImageForCard(): String? {
    val ordered = when (type) {
        MemoryType.Audio -> listOfNotNull(audioCoverUrl, carouselImageUrls.firstOrNull(), thumbnailUrl)
        MemoryType.Photo -> buildList {
            addAll(carouselImageUrls)
            thumbnailUrl?.let { add(it) }
            mainMediaUrl?.let { add(it) }
        }
        MemoryType.Text -> emptyList()
    }
    return ordered
        .mapNotNull { it.trim().takeIf { s -> s.isNotBlank() } }
        .firstOrNull { MemoryMediaUrlHeuristics.isDisplayableImageUrl(it) }
}

@Composable
private fun MemoryCardGrid(
    memory: MemoryUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coverImageUrl = memory.coverImageForCard()
    val interactionSource = rememberMotionInteractionSource()
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 238.dp)
            .nonnaInteractiveScale(interactionSource, pressed = 0.98f),
        interactionSource = interactionSource,
        shape = NonnaCorners.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, hoveredElevation = 10.dp, pressedElevation = 10.dp)
    ) {
        Column {
            // Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NonnaDimens.memoryCardHeight)
                    .clip(
                        RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
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
                if (coverImageUrl != null) {
                    AsyncImage(
                        model = coverImageUrl,
                        contentDescription = memory.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(NonnaDimens.memoryCardHeight),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    PlaceholderCover(
                        type = memory.type,
                        compact = false,
                        modifier = Modifier.fillMaxWidth()
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
            Column(
                modifier = Modifier
                    .padding(12.dp)
            ) {
                Text(
                    text = memory.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = memory.description.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                    } else if (!memory.emotionalCustomLabel.isNullOrBlank()) {
                        CustomEmotionBadge(text = memory.emotionalCustomLabel)
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
    val coverImageUrl = memory.coverImageForCard()
    val interactionSource = rememberMotionInteractionSource()
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .nonnaInteractiveScale(interactionSource, pressed = 0.98f),
        interactionSource = interactionSource,
        shape = NonnaCorners.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, hoveredElevation = 10.dp, pressedElevation = 10.dp)
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
                if (coverImageUrl != null) {
                    AsyncImage(
                        model = coverImageUrl,
                        contentDescription = memory.title,
                        modifier = Modifier.size(NonnaDimens.thumbnailSmall),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    PlaceholderCover(
                        type = memory.type,
                        compact = true,
                        modifier = Modifier.size(NonnaDimens.thumbnailSmall)
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
                            text = " • ${memory.duration}", // i18n-ignore dynamic separator
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (memory.emotionalTag != null) {
                Spacer(modifier = Modifier.width(8.dp))
                EmotionalTagBadge(tag = memory.emotionalTag)
            } else if (!memory.emotionalCustomLabel.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                CustomEmotionBadge(text = memory.emotionalCustomLabel)
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
            text = emotionalTagLabel(tag),
            style = MaterialTheme.typography.labelSmall,
            color = tag.textColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CustomEmotionBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = NonnaCorners.Full,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

fun MemoryType.toIcon(): ImageVector = when (this) {
    MemoryType.Photo -> Icons.Outlined.Image
    MemoryType.Audio -> Icons.Outlined.AudioFile
    MemoryType.Text -> Icons.Outlined.Description
}

@Composable
internal fun PlaceholderCover(
    type: MemoryType,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    when (type) {
        MemoryType.Audio -> AudioMemoryCover(compact = compact, modifier = modifier)
        MemoryType.Text -> TextMemoryCover(compact = compact, modifier = modifier)
        MemoryType.Photo -> Box(
            modifier = modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        PrimaryGradientStart.copy(alpha = 0.28f),
                        PrimaryGradientEnd.copy(alpha = 0.28f)
                    )
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = type.toIcon(),
                contentDescription = null,
                modifier = Modifier.size(if (compact) 22.dp else 34.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun AudioMemoryCover(
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "audioCoverAlpha"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.97f,
        animationSpec = tween(durationMillis = 320),
        label = "audioCoverScale"
    )

    val audioBgStart = Color(0xFFFFFBEB)
    val audioBgMid = Color(0xFFFED7AA)
    val audioBgEnd = Color(0xFFFEF3C7)
    val ringColor = Color(0x1A451A03)
    val centerGrad1 = Color(0x33451A03)
    val centerGrad2 = Color(0x4D9A3412)
    val iconColor = Color(0x99451A03)
    val waveColor = Color(0xFF451A03)
    val waveHeights = listOf(12.dp, 20.dp, 16.dp, 24.dp, 14.dp, 18.dp, 22.dp)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .alpha(animatedAlpha)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(audioBgStart, audioBgMid, audioBgEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!compact) {
            repeat(5) { index ->
                val fraction = 1f - (index * 0.2f)
                Box(
                    modifier = Modifier
                        .fillMaxSize(fraction)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .border(width = 1.dp, color = ringColor, shape = androidx.compose.foundation.shape.CircleShape)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(if (compact) 40.dp else 64.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(centerGrad1, centerGrad2)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Mic,
                contentDescription = null,
                modifier = Modifier.size(if (compact) 20.dp else 32.dp),
                tint = iconColor
            )
        }

        if (!compact) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                waveHeights.forEach { barHeight ->
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(barHeight)
                            .clip(NonnaCorners.Full)
                            .background(waveColor.copy(alpha = 0.2f))
                    )
                }
            }
        }
    }
}

@Composable
private fun TextMemoryCover(
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "textCoverAlpha"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.97f,
        animationSpec = tween(durationMillis = 320),
        label = "textCoverScale"
    )

    val textBgStart = Color(0xFFFFF1F2)
    val textBgMid = Color(0x80FFFBEB)
    val textBgEnd = Color(0xFFFED7AA)
    val lineAndBorder = Color(0x33881337)
    val centerCard1 = Color(0xCCFFF1F2)
    val centerCard2 = Color(0xCCFEF3C7)
    val docIconColor = Color(0x99881337)
    val stampOuter = Color(0x1A881337)
    val stampInner = Color(0x0D881337)
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .alpha(animatedAlpha)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(textBgStart, textBgMid, textBgEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!compact) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(NonnaCorners.Small),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                repeat(12) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(lineAndBorder.copy(alpha = 0.3f))
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .size(48.dp)
                    .drawBehind {
                        val stroke = with(density) { 2.dp.toPx() }
                        val radius = with(density) { 8.dp.toPx() }
                        drawLine(
                            color = lineAndBorder,
                            start = androidx.compose.ui.geometry.Offset(radius, 0f),
                            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = lineAndBorder,
                            start = androidx.compose.ui.geometry.Offset(0f, radius),
                            end = androidx.compose.ui.geometry.Offset(0f, size.height),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                    }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(48.dp)
                    .drawBehind {
                        val stroke = with(density) { 2.dp.toPx() }
                        val radius = with(density) { 8.dp.toPx() }
                        drawLine(
                            color = lineAndBorder,
                            start = androidx.compose.ui.geometry.Offset(0f, size.height),
                            end = androidx.compose.ui.geometry.Offset(size.width - radius, size.height),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = lineAndBorder,
                            start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height - radius),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                    }
            )
        }

        Box(
            modifier = Modifier
                .size(if (compact) 48.dp else 80.dp)
                .shadow(4.dp, NonnaCorners.Large)
                .clip(NonnaCorners.Large)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(centerCard1, centerCard2)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                modifier = Modifier.size(if (compact) 24.dp else 40.dp),
                tint = docIconColor
            )
        }

        if (!compact) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .border(2.dp, stampOuter, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(stampInner)
                )
            }
        }
    }
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

@Composable
fun emotionalTagLabel(tag: EmotionalTag): String {
    return when (tag) {
        EmotionalTag.Alegre -> stringResource(R.string.emotion_alegre)
        EmotionalTag.Nostalgico -> stringResource(R.string.emotion_nostalgico)
        EmotionalTag.Calmo -> stringResource(R.string.emotion_calmo)
        EmotionalTag.Familiar -> stringResource(R.string.emotion_familiar)
    }
}
