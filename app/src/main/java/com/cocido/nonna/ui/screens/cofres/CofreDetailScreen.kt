package com.cocido.nonna.ui.screens.cofres

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.res.stringResource
import com.cocido.nonna.R
import com.cocido.nonna.ui.components.EmptyStateWithButton
import com.cocido.nonna.ui.components.FilterChipsRow
import com.cocido.nonna.ui.components.HeaderAction
import com.cocido.nonna.ui.components.localizedMemoryFilters
import com.cocido.nonna.ui.components.MemoryCard
import com.cocido.nonna.ui.components.MemoryCardViewMode
import com.cocido.nonna.ui.components.MemoryFilters
import com.cocido.nonna.ui.components.MemoryType
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.NonnaBottomFeedbackBanner
import com.cocido.nonna.ui.components.NonnaFeedbackType
import com.cocido.nonna.ui.components.PageHeader
import com.cocido.nonna.ui.components.PermissionBadge
import com.cocido.nonna.ui.components.NonnaStaggerItem
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.PrimaryGradientEnd
import com.cocido.nonna.ui.theme.PrimaryGradientStart
import com.cocido.nonna.ui.components.InviteFamilyModal
import com.cocido.nonna.ui.components.relationValueLabel
import com.cocido.nonna.ui.permissions.canManageCofre
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay

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
    val scope = rememberCoroutineScope()
    var feedbackVisible by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackType by remember { mutableStateOf(NonnaFeedbackType.Success) }

    LaunchedEffect(Unit) { viewModel.load() }

    LaunchedEffect(Unit) {
        viewModel.deleteSuccess.collectLatest {
            feedbackMessage = context.getString(R.string.chest_deleted_success)
            feedbackType = NonnaFeedbackType.Success
            feedbackVisible = true
            delay(1200)
            feedbackVisible = false
            onBack()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.inviteSuccess.collectLatest {
            feedbackMessage = context.getString(R.string.chest_invite_sent)
            feedbackType = NonnaFeedbackType.Success
            feedbackVisible = true
            delay(1400)
            feedbackVisible = false
        }
    }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            feedbackMessage = msg
            feedbackType = NonnaFeedbackType.Error
            feedbackVisible = true
            scope.launch {
                delay(1800)
                feedbackVisible = false
            }
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
    val realPhotoCount = memoriesState.count { it.type == MemoryType.Photo }
    val realAudioCount = memoriesState.count { it.type == MemoryType.Audio }
    val realTextCount = memoriesState.count { it.type == MemoryType.Text }
    
    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        PageHeader(
            title = cofre?.name ?: "",
            subtitle = cofre?.relation?.let { relationValueLabel(it) } ?: "",
            onBack = onBack,
            action = HeaderAction(
                label = stringResource(R.string.common_add),
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
                        stringResource(R.string.chest_not_found),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
        // Cover image
        val coverUrl = cofre.coverImageUrl?.takeIf { it.isNotBlank() && it != "string" }
        AnimatedVisibility(visible = true, enter = fadeIn(animationSpec = spring())) {
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
                    text = relationValueLabel(cofre.relation),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            }
        }
        
        // Stats bar
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it / 4 }, animationSpec = spring()) + fadeIn()
        ) {
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
                    count = realPhotoCount,
                    label = if (realPhotoCount == 1) stringResource(R.string.stat_photo_singular) else stringResource(R.string.stat_photo_plural)
                )
                StatItem(
                    icon = Icons.Outlined.AudioFile,
                    count = realAudioCount,
                    label = if (realAudioCount == 1) stringResource(R.string.stat_audio_singular) else stringResource(R.string.stat_audio_plural)
                )
                StatItem(
                    icon = Icons.Outlined.Description,
                    count = realTextCount,
                    label = if (realTextCount == 1) stringResource(R.string.stat_text_singular) else stringResource(R.string.stat_text_plural)
                )
                StatItem(
                    icon = Icons.Outlined.People,
                    count = cofre.memberCount,
                    label = if (cofre.memberCount == 1) stringResource(R.string.stat_member_singular) else stringResource(R.string.stat_member_plural)
                )
            }
            }
        }
        
        // Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            listOf(
                stringResource(R.string.chest_tab_memories),
                stringResource(R.string.chest_tab_family),
                stringResource(R.string.chest_tab_details)
            ).forEachIndexed { index, title ->
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
                canInvite = canManageCofre(cofre),
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

    if (showInviteModal && canManageCofre(cofre) && cofre != null) {
        InviteFamilyModal(
            cofreName = cofre.name,
            onDismiss = { showInviteModal = false },
            onInvite = { data ->
                viewModel.invitar(data.emails)
                showInviteModal = false
            }
        )
    }

    if (showDeleteConfirm && canManageCofre(cofre)) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.common_delete_chest)) },
            text = { Text(stringResource(R.string.chest_delete_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteCofre()
                }) {
                    Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    NonnaBottomFeedbackBanner(
        visible = feedbackVisible,
        message = feedbackMessage,
        type = feedbackType,
        modifier = Modifier.align(Alignment.BottomCenter)
    )
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
            text = "$count $label", // i18n-ignore dynamic count + localized label
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
                    title = stringResource(R.string.memories_empty_title),
                    description = stringResource(R.string.memories_empty_description),
                    buttonText = stringResource(R.string.memories_add_first_button),
                    onButtonClick = onAddMemory
                )
            }
        } else {
            val controlsScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(controlsScrollState),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChipsRow(
                    chips = localizedMemoryFilters(),
                    selectedChipId = memoryFilter,
                    onChipSelected = onFilterChange,
                    scrollable = false
                )

                Surface(
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
                                contentDescription = stringResource(R.string.view_grid_cd),
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
                                contentDescription = stringResource(R.string.view_list_cd),
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
                        text = stringResource(R.string.memories_no_filter_results),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (viewMode == MemoryCardViewMode.Grid) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    itemsIndexed(filteredMemories, key = { _, memory -> memory.id }) { index, memory ->
                        NonnaStaggerItem(index = index, stepDelayMs = 50) {
                            MemoryCard(
                                memory = memory,
                                onClick = { onMemoryClick(memory.id) },
                                viewMode = MemoryCardViewMode.Grid
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    itemsIndexed(filteredMemories, key = { _, memory -> memory.id }) { index, memory ->
                        NonnaStaggerItem(index = index, stepDelayMs = 50) {
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
    val currentEmail = currentUser.email.trim().lowercase()
    val baseMembers = mutableListOf<CofreMemberDisplay>()
    val currentInvite = cofre.invited.firstOrNull { it.email.trim().lowercase() == currentEmail }

    val ownerEmailRaw = cofre.ownerEmail?.trim().orEmpty()
    val ownerLooksIncorrectForGuestView = !cofre.isOwner && ownerEmailRaw.equals(currentUser.email, ignoreCase = true)
    val ownerEmail = if (ownerLooksIncorrectForGuestView) "" else ownerEmailRaw
    if (ownerEmail.isNotBlank()) {
        baseMembers += CofreMemberDisplay(
            fullName = cofre.ownerName?.ifBlank { null } ?: ownerEmail,
            username = cofre.ownerUsername,
            email = ownerEmail,
            avatarUrl = cofre.ownerAvatarUrl
                ?: if (ownerEmail.equals(currentUser.email, ignoreCase = true)) currentUser.profileImageUrl() else null,
            role = com.cocido.nonna.ui.components.CofreRole.Creador
        )
    } else if (cofre.isOwner) {
        baseMembers += CofreMemberDisplay(
            fullName = currentUser.displayName(),
            username = currentUser.nombreUsuario,
            email = currentUser.email,
            avatarUrl = currentUser.profileImageUrl(),
            role = com.cocido.nonna.ui.components.CofreRole.Creador
        )
    }

    if (!cofre.isOwner && baseMembers.none { it.email.equals(currentUser.email, ignoreCase = true) }) {
        baseMembers += CofreMemberDisplay(
            fullName = currentUser.displayName(),
            username = currentUser.nombreUsuario,
            email = currentUser.email,
            avatarUrl = currentUser.profileImageUrl(),
            role = if (currentInvite == null || currentInvite.accepted) {
                com.cocido.nonna.ui.components.CofreRole.Colaborador
            } else {
                com.cocido.nonna.ui.components.CofreRole.Invitado
            }
        )
    }

    cofre.invited.forEach { invitee ->
        val inviteEmail = invitee.email.trim()
        if (inviteEmail.lowercase() == currentEmail) return@forEach
        val isOwnerByEmail = ownerEmail.isNotBlank() && inviteEmail.equals(ownerEmail, ignoreCase = true)
        baseMembers += CofreMemberDisplay(
            fullName = invitee.fullName ?: inviteEmail,
            username = null,
            email = inviteEmail,
            avatarUrl = if (isOwnerByEmail) cofre.ownerAvatarUrl else null,
            role = if (isOwnerByEmail) {
                com.cocido.nonna.ui.components.CofreRole.Creador
            } else if (invitee.accepted) {
                com.cocido.nonna.ui.components.CofreRole.Colaborador
            } else {
                com.cocido.nonna.ui.components.CofreRole.Invitado
            }
        )
    }
    return baseMembers.distinctBy { it.email.lowercase() }
}

@Composable
private fun FamiliaTab(
    members: List<CofreMemberDisplay>,
    canInvite: Boolean,
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
                    text = stringResource(R.string.chest_members_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.chest_members_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            NonnaButton(
                text = stringResource(R.string.common_invite),
                onClick = onInvite,
                enabled = canInvite,
                size = com.cocido.nonna.ui.components.NonnaButtonSize.Small
            )
        }
        if (!canInvite) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.chest_guest_permissions_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        text = stringResource(R.string.chest_members_empty_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.chest_members_empty_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(members, key = { _, member -> member.email }) { index, member ->
                    NonnaStaggerItem(index = index, stepDelayMs = 60) {
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
                                        text = "@${member.username}", // i18n-ignore username handle format
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
                    text = stringResource(R.string.chest_info_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailItem(label = stringResource(R.string.common_name), value = cofre.name)
                Spacer(modifier = Modifier.height(12.dp))
                DetailItem(label = stringResource(R.string.common_relation), value = relationValueLabel(cofre.relation))
                Spacer(modifier = Modifier.height(12.dp))
                DetailItem(label = stringResource(R.string.common_privacy), value = stringResource(R.string.chest_privacy_invited_only))
            }
        }
        
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
                    text = stringResource(R.string.chest_creator_actions),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                NonnaButton(
                    text = stringResource(R.string.chest_edit_info),
                    onClick = onEdit,
                    enabled = cofre.isOwner,
                    style = NonnaButtonStyle.Ghost,
                    fullWidth = true
                )

                NonnaButton(
                    text = stringResource(R.string.common_delete_chest),
                    onClick = onDelete,
                    enabled = cofre.isOwner,
                    style = NonnaButtonStyle.Destructive,
                    fullWidth = true
                )

                if (!cofre.isOwner) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.chest_guest_permissions_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
