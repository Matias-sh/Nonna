package com.cocido.nonna.ui.screens.cofres

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cocido.nonna.data.mock.mockCofres
import com.cocido.nonna.data.mock.mockFamilyMembers
import com.cocido.nonna.data.mock.mockMemories
import com.cocido.nonna.ui.components.EmptyStateWithButton
import com.cocido.nonna.ui.components.FilterChipsRow
import com.cocido.nonna.ui.components.HeaderAction
import com.cocido.nonna.ui.components.MemoryCard
import com.cocido.nonna.ui.components.MemoryCardViewMode
import com.cocido.nonna.ui.components.MemoryFilters
import com.cocido.nonna.ui.components.MemoryType
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.PageHeader
import com.cocido.nonna.ui.components.PermissionBadge
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.PrimaryGradientEnd
import com.cocido.nonna.ui.theme.PrimaryGradientStart

@Composable
fun CofreDetailScreen(
    cofreId: String,
    onBack: () -> Unit,
    onAddMemory: () -> Unit,
    onMemoryClick: (String) -> Unit
) {
    val cofre = mockCofres.find { it.id == cofreId } ?: mockCofres.first()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var memoryFilter by remember { mutableStateOf(MemoryFilters.todos.id) }
    var viewMode by remember { mutableStateOf(MemoryCardViewMode.Grid) }
    
    val filteredMemories = mockMemories.filter { memory ->
        when (memoryFilter) {
            MemoryFilters.fotos.id -> memory.type == MemoryType.Photo
            MemoryFilters.audios.id -> memory.type == MemoryType.Audio
            MemoryFilters.textos.id -> memory.type == MemoryType.Text
            else -> true
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        PageHeader(
            title = cofre.name,
            subtitle = cofre.relation,
            onBack = onBack,
            action = HeaderAction(
                label = "Agregar",
                icon = Icons.Default.Add,
                onClick = onAddMemory
            )
        )
        
        // Cover image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(NonnaDimens.coverHeightMedium)
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
            Icon(
                imageVector = Icons.Outlined.Favorite,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))
                        )
                    )
            )
            
            // Info overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(NonnaDimens.spacing24)
            ) {
                Text(
                    text = cofre.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = cofre.relation,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
        
        // Stats bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NonnaDimens.spacing16),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                StatItem(
                    icon = Icons.Outlined.Image,
                    count = cofre.photoCount,
                    label = "fotos"
                )
                StatItem(
                    icon = Icons.Outlined.AudioFile,
                    count = cofre.audioCount,
                    label = "audios"
                )
                StatItem(
                    icon = Icons.Outlined.Description,
                    count = cofre.textCount,
                    label = "textos"
                )
                StatItem(
                    icon = Icons.Outlined.People,
                    count = cofre.memberCount,
                    label = if (cofre.memberCount == 1) "miembro" else "miembros"
                )
            }
        }
        
        // Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            listOf("Recuerdos", "Familia", "Detalles").forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Tab content
        when (selectedTabIndex) {
            0 -> RecuerdosTab(
                memories = filteredMemories,
                memoryFilter = memoryFilter,
                onFilterChange = { memoryFilter = it },
                viewMode = viewMode,
                onViewModeChange = { viewMode = it },
                onMemoryClick = onMemoryClick,
                onAddMemory = onAddMemory
            )
            1 -> FamiliaTab(
                onInvite = { /* TODO */ }
            )
            2 -> DetallesTab(
                cofre = cofre
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    count: Int,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RecuerdosTab(
    memories: List<com.cocido.nonna.ui.components.MemoryUiModel>,
    memoryFilter: String,
    onFilterChange: (String) -> Unit,
    viewMode: MemoryCardViewMode,
    onViewModeChange: (MemoryCardViewMode) -> Unit,
    onMemoryClick: (String) -> Unit,
    onAddMemory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(NonnaDimens.screenPaddingHorizontal)
    ) {
        if (mockMemories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateWithButton(
                    icon = Icons.Outlined.Favorite,
                    title = "Todavía no hay recuerdos",
                    description = "Empecemos con el primero. Puede ser una foto, un audio o una historia.",
                    buttonText = "Agregar primer recuerdo",
                    onButtonClick = onAddMemory
                )
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Filters and view mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChipsRow(
                    chips = MemoryFilters.all,
                    selectedChipId = memoryFilter,
                    onChipSelected = onFilterChange,
                    modifier = Modifier.weight(1f)
                )
                
                // View mode toggle
                Surface(
                    modifier = Modifier.padding(start = 8.dp),
                    shape = NonnaCorners.Medium,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        IconButton(
                            onClick = { onViewModeChange(MemoryCardViewMode.Grid) },
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (viewMode == MemoryCardViewMode.Grid) {
                                        MaterialTheme.colorScheme.surface
                                    } else {
                                        Color.Transparent
                                    },
                                    shape = NonnaCorners.Small
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.GridView,
                                contentDescription = "Vista grilla",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { onViewModeChange(MemoryCardViewMode.List) },
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (viewMode == MemoryCardViewMode.List) {
                                        MaterialTheme.colorScheme.surface
                                    } else {
                                        Color.Transparent
                                    },
                                    shape = NonnaCorners.Small
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.List,
                                contentDescription = "Vista lista",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Memories
            if (memories.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay recuerdos de este tipo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (viewMode == MemoryCardViewMode.Grid) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(memories) { memory ->
                        MemoryCard(
                            memory = memory,
                            onClick = { onMemoryClick(memory.id) },
                            viewMode = MemoryCardViewMode.Grid
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(memories) { memory ->
                        MemoryCard(
                            memory = memory,
                            onClick = { onMemoryClick(memory.id) },
                            viewMode = MemoryCardViewMode.List
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FamiliaTab(
    onInvite: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(NonnaDimens.screenPaddingHorizontal)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Miembros del cofre",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Personas que pueden ver y contribuir a este cofre",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            NonnaButton(
                text = "Invitar",
                onClick = onInvite,
                size = com.cocido.nonna.ui.components.NonnaButtonSize.Small
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mockFamilyMembers) { member ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            PrimaryGradientStart.copy(alpha = 0.2f),
                                            PrimaryGradientEnd.copy(alpha = 0.2f)
                                        )
                                    ),
                                    shape = NonnaCorners.Full
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = member.name.first().toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = member.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = member.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        PermissionBadge(role = member.role)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetallesTab(
    cofre: com.cocido.nonna.ui.components.CofreUiModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(NonnaDimens.screenPaddingHorizontal)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = NonnaCorners.Card,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(NonnaDimens.cardPaddingLarge)
            ) {
                Text(
                    text = "Información del cofre",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailItem(label = "Nombre", value = cofre.name)
                Spacer(modifier = Modifier.height(12.dp))
                DetailItem(label = "Parentesco", value = cofre.relation)
                Spacer(modifier = Modifier.height(12.dp))
                DetailItem(label = "Privacidad", value = "🔒 Solo invitados")
            }
        }
        
        if (cofre.isOwner) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = NonnaCorners.Card,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(NonnaDimens.cardPaddingLarge)
                ) {
                    Text(
                        text = "Acciones del creador",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    NonnaButton(
                        text = "Editar información del cofre",
                        onClick = { /* TODO */ },
                        style = NonnaButtonStyle.Ghost,
                        fullWidth = true
                    )
                    
                    NonnaButton(
                        text = "Gestionar permisos",
                        onClick = { /* TODO */ },
                        style = NonnaButtonStyle.Ghost,
                        fullWidth = true
                    )
                    
                    NonnaButton(
                        text = "Eliminar cofre",
                        onClick = { /* TODO */ },
                        style = NonnaButtonStyle.Destructive,
                        fullWidth = true
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
