package com.cocido.nonna.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.cocido.nonna.ui.theme.NonnaCorners

data class FilterChip(
    val id: String,
    val label: String
)

@Composable
fun FilterChipsRow(
    chips: List<FilterChip>,
    selectedChipId: String,
    onChipSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Row(
        modifier = modifier.horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chip ->
            FilterChipItem(
                chip = chip,
                isSelected = chip.id == selectedChipId,
                onClick = { onChipSelected(chip.id) }
            )
        }
    }
}

@Composable
fun FilterChipItem(
    chip: FilterChip,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Box(
        modifier = modifier
            .clip(NonnaCorners.Full)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = chip.label,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}

@Composable
fun EmotionalTagChip(
    label: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$emoji $label",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Predefined emotional tags matching Figma design
object EmotionalTags {
    val alegre = FilterChip("alegre", "😊 Alegre")
    val nostalgico = FilterChip("nostálgico", "🥹 Nostálgico")
    val calmo = FilterChip("calmo", "😌 Calmo")
    val familiar = FilterChip("familiar", "👨‍👩‍👧‍👦 Familiar")
    
    val all = listOf(alegre, nostalgico, calmo, familiar)
}

// Predefined cofre filters
object CofreFilters {
    val todos = FilterChip("todos", "Todos")
    val mios = FilterChip("mios", "Míos")
    val compartidos = FilterChip("compartidos", "Compartidos")
    
    val all = listOf(todos, mios, compartidos)
}

// Predefined memory type filters
object MemoryFilters {
    val todos = FilterChip("todos", "Todos")
    val fotos = FilterChip("fotos", "Fotos")
    val audios = FilterChip("audios", "Audios")
    val textos = FilterChip("textos", "Textos")
    
    val all = listOf(todos, fotos, audios, textos)
}
