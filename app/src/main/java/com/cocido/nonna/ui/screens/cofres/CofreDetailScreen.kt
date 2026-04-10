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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.cocido.nonna.ui.components.InviteFamilyModal
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun CofreDetailScreen(
    cofreId: String,
    onBack: () -> Unit,
    onAddMemory: () -> Unit,
    onMemoryClick: (String) -> Unit,
    onEditCofre: (String) -> Unit = {},
    viewModel: com.cocido.nonna.ui.viewmodel.CofreDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val cofreState by viewModel.cofre.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val memoriesState by viewModel.memories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val cofre = cofreState
    var showInviteModal by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.load() }

    LaunchedEffect(Unit) {
        viewModel.deleteSuccess.collectLatest { onBack() }
    }
    LaunchedEffect(Unit) {
        viewModel.inviteSuccess.collectLatest {
            scope.launch { snackbarHostState.showSnackbar("Invitación enviada") }
        }
    }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            scope.launch { snackbarHostState.showSnackbar(msg) }
            viewModel.clearError()
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var memoryFilter by remember { mutableStateOf(MemoryFilters.todos.id) }
    var viewMode by remember { mutableStateOf(MemoryCardViewMode.Grid) }
    
    val filteredMemories = memoriesState.filter { memory ->
        when (memoryFilter) {
            MemoryFilters.fotos.id -> memory.type == MemoryType.Photo
            MemoryFilters.audios.id -> memory.type == MemoryType.Audio
            MemoryFilters.textos.id -> memory.type == MemoryType.Text
            else -> true
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        PageHeader(
            title = cofre?.name ?: "",
            subtitle = cofre?.relation ?: "",
            onBack = onBack,
            action = HeaderAction(
                label = "Agregar",
                icon = Icons.Default.Add,
                onClick = onAddMemory
            )
        )
        
        when {
            isLoading && cofre == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
            cofre == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        "Cofre no encontrado",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
        // Cover image
        val coverUrl = cofre.coverImageUrl?.takeIf { it.isNotBlank() && it != "string" }
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
            if (coverUrl != null) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
            }
            
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
                allMemories = memoriesState,
                filteredMemories = filteredMemories,
                memoryFilter = memoryFilter,
                onFilterChange = { memoryFilter = it },
                viewMode = viewMode,
                onViewModeChange = { viewMode = it },
                onMemoryClick = onMemoryClick,
                onAddMemory = onAddMemory
            )
            1 -> FamiliaTab(
                members = buildFamiliaMembers(cofre, currentUser),
                onInvite = { showInviteModal = true }
            )
            2 -> DetallesTab(
                cofre = cofre,
                onEdit = { onEditCofre(viewModel.cofreId) },
                onDelete = { showDeleteConfirm = true }
            )
        }
        }
    }
    }

    if (showInviteModal && cofre != null) {
        InviteFamilyModal(
            cofreName = cofre.name,
            onDismiss = { showInviteModal = false },
            onInvite = { data ->
                viewModel.invitar(listOf(data.email))
                showInviteModal = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar cofre") },
            text = { Text("¿Estás seguro? Se eliminará el cofre y su información. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteCofre()
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
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
    allMemories: List<com.cocido.nonna.ui.components.MemoryUiModel>,
    filteredMemories: List<com.cocido.nonna.ui.components.MemoryUiModel>,
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
        if (allMemories.isEmpty()) {
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
            if (filteredMemories.isEmpty()) {
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
                    items(filteredMemories) { memory ->
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
                    items(filteredMemories) { memory ->
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

/** Miembro del cofre para la UI; datos reales (API o sesión). */
private data class CofreMemberDisplay(
    val fullName: String,
    val username: String?,
    val email: String,
    val avatarUrl: String?,
    val role: com.cocido.nonna.ui.components.CofreRole
)

private fun buildFamiliaMembers(
    cofre: com.cocido.nonna.ui.components.CofreUiModel?,
    currentUser: com.cocido.nonna.data.remote.dto.UserDto?
): List<CofreMemberDisplay> {
    if (cofre == null || currentUser == null) return emptyList()
    if (!cofre.isOwner) return emptyList()
    val fullName = currentUser.displayName()
    val username = currentUser.nombreUsuario
    val avatar = currentUser.fotoPerfil ?: currentUser.avatarUrl ?: currentUser.avatar_url
    return listOf(
        CofreMemberDisplay(
            fullName = fullName,
            username = username,
            email = currentUser.email,
            avatarUrl = avatar,
            role = com.cocido.nonna.ui.components.CofreRole.Creador
        )
    )
}

@Composable
private fun FamiliaTab(
    members: List<CofreMemberDisplay>,
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
        
        if (members.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Aún no hay miembros en este cofre",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Usá Invitar para sumar familiares. Ellos podrán ver y colaborar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(members) { member ->
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
                                val avatarModel = member.avatarUrl
                                if (avatarModel != null) {
                                    AsyncImage(
                                        model = avatarModel,
                                        contentDescription = member.fullName,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(NonnaCorners.Full),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = member.fullName.firstOrNull()?.toString() ?: "?",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = member.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (!member.username.isNullOrBlank()) {
                                    Text(
                                        text = "@${member.username}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
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
}

@Composable
private fun DetallesTab(
    cofre: com.cocido.nonna.ui.components.CofreUiModel,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
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
                DetailItem(label = "Privacidad", value = "Solo invitados")
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
                        onClick = onEdit,
                        style = NonnaButtonStyle.Ghost,
                        fullWidth = true
                    )
                    
                    NonnaButton(
                        text = "Eliminar cofre",
                        onClick = onDelete,
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
