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
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.nonna.R
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.NonnaTheme
import com.cocido.nonna.ui.theme.PrimaryGradientEnd
import com.cocido.nonna.ui.theme.PrimaryGradientStart

data class CofreUiModel(
    val id: String,
    val name: String,
    val relation: String,
    val photoCount: Int = 0,
    val audioCount: Int = 0,
    val textCount: Int = 0,
    val memberCount: Int = 1,
    val lastUpdated: String = "",
    val updatedAtIso: String? = null,
    val coverImageUrl: String? = null,
    val isOwner: Boolean = true,
    val invited: List<CofreInviteeUiModel> = emptyList()
)

data class CofreInviteeUiModel(
    val id: String,
    val email: String,
    val accepted: Boolean,
    val fullName: String?
)

@Composable
fun CofreCard(
    cofre: CofreUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            hoveredElevation = 10.dp,
            pressedElevation = 10.dp
        )
    ) {
        Column {
            // Cover image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NonnaDimens.cofreCardCoverHeight)
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
                if (cofre.coverImageUrl != null) {
                    AsyncImage(
                        model = cofre.coverImageUrl,
                        contentDescription = cofre.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(NonnaDimens.cofreCardCoverHeight),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(NonnaDimens.cofreCardCoverHeight)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.2f)
                                    )
                                )
                            )
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                }
            }
            
            // Content
            Column(
                modifier = Modifier.padding(NonnaDimens.cardPadding)
            ) {
                Text(
                    text = cofre.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = relationValueLabel(cofre.relation),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (cofre.photoCount > 0) {
                        StatItem(
                            icon = Icons.Outlined.Image,
                            count = cofre.photoCount
                        )
                    }
                    if (cofre.audioCount > 0) {
                        StatItem(
                            icon = Icons.Outlined.AudioFile,
                            count = cofre.audioCount
                        )
                    }
                    if (cofre.textCount > 0) {
                        StatItem(
                            icon = Icons.Outlined.Description,
                            count = cofre.textCount
                        )
                    }
                    if (cofre.memberCount > 1) {
                        StatItem(
                            icon = Icons.Outlined.People,
                            count = cofre.memberCount
                        )
                    }
                }
                
                if (cofre.lastUpdated.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.cofre_updated_at, cofre.lastUpdated),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    count: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, backgroundColor = 0xFFFAF7F0)
@Composable
private fun CofreCardPreview() {
    NonnaTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CofreCard(
                cofre = CofreUiModel(
                    id = "1",
                    name = "Memorias de la Abuela Rosa",
                    relation = "Abuela materna",
                    photoCount = 45,
                    audioCount = 12,
                    textCount = 8,
                    memberCount = 3,
                    lastUpdated = "hace 2 días"
                ),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF7F0)
@Composable
private fun CofreCardEmptyPreview() {
    NonnaTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CofreCard(
                cofre = CofreUiModel(
                    id = "2",
                    name = "Nuevo Cofre",
                    relation = "Sin memorias aún"
                ),
                onClick = {}
            )
        }
    }
}
