package com.cocido.nonna.ui.screens.home

import android.content.SharedPreferences
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.nonna.R
import com.cocido.nonna.ui.components.AppShell
import com.cocido.nonna.ui.components.EmptyStateWithButton
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaStaggerItem
import com.cocido.nonna.ui.components.NonnaTab
import com.cocido.nonna.ui.components.relationValueLabel
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.NonnaTheme
import com.cocido.nonna.ui.theme.PrimaryGradientEnd
import com.cocido.nonna.ui.theme.PrimaryGradientStart
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cocido.nonna.ui.components.CofreInvitationUiModel
import com.cocido.nonna.ui.components.NonnaButtonStyle
import java.time.LocalDate
import java.util.Calendar

private const val PREF_INVITE_WELCOME_SUPPRESS_ID = "invite_welcome_suppress_invitation_id"
private const val PREF_INVITE_WELCOME_SUPPRESS_SIG = "invite_welcome_suppress_signature"

private fun SharedPreferences.isInviteWelcomeSuppressed(inv: CofreInvitationUiModel): Boolean {
    val sid = getString(PREF_INVITE_WELCOME_SUPPRESS_ID, null) ?: return false
    val ssig = getString(PREF_INVITE_WELCOME_SUPPRESS_SIG, null) ?: return false
    return sid == inv.id && ssig == inv.stateSignature
}

private fun SharedPreferences.suppressInviteWelcomeFor(inv: CofreInvitationUiModel) {
    edit()
        .putString(PREF_INVITE_WELCOME_SUPPRESS_ID, inv.id)
        .putString(PREF_INVITE_WELCOME_SUPPRESS_SIG, inv.stateSignature)
        .apply()
}

@Composable
fun HomeRoute(
    onTabSelected: (NonnaTab) -> Unit,
    onCreateCofre: () -> Unit,
    onAddMemory: () -> Unit,
    onContinueCofre: (String) -> Unit,
    onOpenInvitations: () -> Unit = {},
    viewModel: com.cocido.nonna.ui.viewmodel.HomeViewModel = hiltViewModel()
) {
    val cofres by viewModel.cofres.collectAsStateWithLifecycle()
    val user by viewModel.user.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val featuredInvitation by viewModel.featuredInvitation.collectAsStateWithLifecycle()
    val invitationAcceptLoading by viewModel.invitationAcceptLoading.collectAsStateWithLifecycle()
    val invitationAcceptError by viewModel.invitationAcceptError.collectAsStateWithLifecycle()

    val uiState = remember(
        cofres,
        user,
        isLoading,
        errorMessage,
        featuredInvitation,
        invitationAcceptLoading,
        invitationAcceptError
    ) {
        HomeUiState(
            cofres = cofres,
            user = user,
            isLoading = isLoading,
            errorMessage = errorMessage,
            featuredInvitation = featuredInvitation,
            invitationAcceptLoading = invitationAcceptLoading,
            invitationAcceptError = invitationAcceptError
        )
    }

    LaunchedEffect(Unit) { viewModel.load() }

    HomeScreen(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is HomeEvent.SelectTab -> onTabSelected(event.tab)
                HomeEvent.CreateCofre -> onCreateCofre()
                HomeEvent.AddMemory -> onAddMemory()
                is HomeEvent.ContinueCofre -> onContinueCofre(event.cofreId)
                HomeEvent.OpenInvitations -> onOpenInvitations()
                is HomeEvent.AcceptInvitation -> viewModel.acceptInvitationFromHome(event.invitationId)
                HomeEvent.ClearInvitationAcceptError -> viewModel.clearInvitationAcceptError()
                HomeEvent.DismissFeaturedInvitation -> viewModel.dismissFeaturedInvitation()
            }
        }
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit
) {
    val context = LocalContext.current
    val homePrefs = remember(context) {
        context.getSharedPreferences("nonna_home_hints", android.content.Context.MODE_PRIVATE)
    }
    val todayKey = remember { LocalDate.now().toString() }

    val hasData = uiState.cofres.isNotEmpty()
    val lastViewedCofreId = remember(uiState.cofres, homePrefs) {
        homePrefs.getString("last_viewed_cofre_id", null)
    }
    val lastCofre = remember(uiState.cofres, lastViewedCofreId) {
        val lastViewed = uiState.cofres.firstOrNull { it.id == lastViewedCofreId }
        if (lastViewed != null) {
            lastViewed
        } else {
            uiState.cofres.maxByOrNull { parseCofreUpdatedAt(it.updatedAtIso) ?: java.time.Instant.EPOCH }
                ?: uiState.cofres.firstOrNull()
        }
    }
    val userName = uiState.user?.displayName()?.split(" ")?.firstOrNull() ?: ""
    var showDailyPrompt by rememberSaveable(todayKey) {
        mutableStateOf(
            !homePrefs.getBoolean("daily_prompt_dismissed_$todayKey", false)
        )
    }
    var dismissedInvitationId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.featuredInvitation?.id) {
        if (uiState.featuredInvitation?.id != dismissedInvitationId) return@LaunchedEffect
        dismissedInvitationId = null
    }

    AppShell(
        currentTab = NonnaTab.Inicio,
        onTabSelected = { onEvent(HomeEvent.SelectTab(it)) }
    ) {
        if (uiState.isLoading && uiState.cofres.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else if (!hasData) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateWithButton(
                    icon = Icons.Outlined.Inventory2,
                    title = stringResource(R.string.home_empty_title),
                    description = stringResource(R.string.home_empty_description),
                    buttonText = stringResource(R.string.home_empty_button),
                    buttonTestTag = "home_empty_create_button",
                    onButtonClick = { onEvent(HomeEvent.CreateCofre) }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(NonnaDimens.screenPaddingHorizontal)
            ) {
                Spacer(modifier = Modifier.height(NonnaDimens.spacing24))
                NonnaStaggerItem(index = 0, stepDelayMs = 100) {
                    GreetingSection(userName = userName)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Continue where you left off
                if (lastCofre != null) {
                    NonnaStaggerItem(index = 1, stepDelayMs = 100) {
                        ContinueSection(
                            cofreName = lastCofre.name,
                            cofreRelation = lastCofre.relation,
                            coverImageUrl = lastCofre.coverImageUrl?.takeIf { it.isNotBlank() && it != "string" },
                            onClick = {
                                homePrefs.edit().putString("last_viewed_cofre_id", lastCofre.id).apply()
                                onEvent(HomeEvent.ContinueCofre(lastCofre.id))
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
                
                // Daily prompt
                if (showDailyPrompt) {
                    NonnaStaggerItem(index = 2, stepDelayMs = 100) {
                        DailyPromptSection(
                            onAddMemory = { onEvent(HomeEvent.AddMemory) },
                            onDismiss = {
                                homePrefs.edit()
                                    .putBoolean("daily_prompt_dismissed_$todayKey", true)
                                    .apply()
                                showDailyPrompt = false
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
                
                // Quick actions
                NonnaStaggerItem(index = 3, stepDelayMs = 100) {
                    QuickActionsSection(
                        onCreateCofre = { onEvent(HomeEvent.CreateCofre) },
                        onAddMemory = { onEvent(HomeEvent.AddMemory) }
                    )
                }
                
                Spacer(modifier = Modifier.height(100.dp)) // Bottom nav padding
            }
        }
    }

    val invitationToShow = uiState.featuredInvitation?.takeIf {
        it.id != dismissedInvitationId && !homePrefs.isInviteWelcomeSuppressed(it)
    }
    if (invitationToShow != null) {
        InvitationWelcomeDialog(
            invitation = invitationToShow,
            acceptLoading = uiState.invitationAcceptLoading,
            acceptError = uiState.invitationAcceptError,
            onClearAcceptError = { onEvent(HomeEvent.ClearInvitationAcceptError) },
            onAcceptNow = { onEvent(HomeEvent.AcceptInvitation(invitationToShow.id)) },
            onOpenInvitations = { dontShowAgain ->
                if (dontShowAgain) homePrefs.suppressInviteWelcomeFor(invitationToShow)
                else dismissedInvitationId = invitationToShow.id
                onEvent(HomeEvent.DismissFeaturedInvitation)
                onEvent(HomeEvent.OpenInvitations)
            },
            onDismiss = { dontShowAgain ->
                if (dontShowAgain) homePrefs.suppressInviteWelcomeFor(invitationToShow)
                else dismissedInvitationId = invitationToShow.id
                onEvent(HomeEvent.DismissFeaturedInvitation)
            }
        )
    }
}

@Composable
private fun InvitationWelcomeDialog(
    invitation: CofreInvitationUiModel,
    acceptLoading: Boolean,
    acceptError: String?,
    onClearAcceptError: () -> Unit,
    onAcceptNow: () -> Unit,
    onOpenInvitations: (dontShowAgain: Boolean) -> Unit,
    onDismiss: (dontShowAgain: Boolean) -> Unit
) {
    var dontShowAgain by remember(invitation.id) { mutableStateOf(false) }
    val overlayVisible = remember(invitation.id) { MutableTransitionState(false) }

    LaunchedEffect(invitation.id) {
        dontShowAgain = false
        onClearAcceptError()
        overlayVisible.targetState = true
    }

    Dialog(
        onDismissRequest = { if (!acceptLoading) onDismiss(dontShowAgain) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visibleState = overlayVisible,
                enter = fadeIn(tween(220, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(160)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.52f))
                        .clickable(enabled = !acceptLoading) { onDismiss(dontShowAgain) }
                )
            }
            AnimatedVisibility(
                visibleState = overlayVisible,
                enter = fadeIn(
                    animationSpec = tween(280, delayMillis = 36, easing = FastOutSlowInEasing)
                ) + scaleIn(
                    initialScale = 0.88f,
                    animationSpec = tween(300, delayMillis = 36, easing = FastOutSlowInEasing)
                ) + slideInVertically(
                    animationSpec = tween(300, delayMillis = 36, easing = FastOutSlowInEasing)
                ) { it / 12 },
                exit = fadeOut(tween(160)) + scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(160)
                ) + slideOutVertically(animationSpec = tween(160)) { it / 12 },
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
            Card(
                shape = NonnaCorners.Card,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(NonnaDimens.cardPadding)) {
                    Text(
                        text = stringResource(R.string.invite_welcome_modal_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = invitation.inviterName?.let {
                            stringResource(R.string.invite_welcome_modal_message_with_name, it, invitation.cofreName)
                        } ?: stringResource(R.string.invite_welcome_modal_message, invitation.cofreName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    invitation.cofreDescription
                        ?.takeIf { it.isNotBlank() }
                        ?.let { description ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(NonnaCorners.Large)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        PrimaryGradientStart.copy(alpha = 0.25f),
                                        PrimaryGradientEnd.copy(alpha = 0.25f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val cover = invitation.cofreCoverImageUrl?.takeIf { it.isNotBlank() && it != "string" }
                        if (cover != null) {
                            AsyncImage(
                                model = cover,
                                contentDescription = invitation.cofreName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Inventory2,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    if (acceptError != null) {
                        Text(
                            text = acceptError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    NonnaButton(
                        text = if (acceptLoading) {
                            stringResource(R.string.common_loading)
                        } else {
                            stringResource(R.string.invite_welcome_modal_accept_now)
                        },
                        onClick = onAcceptNow,
                        testTag = "home_invite_accept_button",
                        enabled = !acceptLoading,
                        fullWidth = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    NonnaButton(
                        text = stringResource(R.string.invite_welcome_modal_open_button),
                        onClick = { onOpenInvitations(dontShowAgain) },
                        testTag = "home_invite_open_invitations_button",
                        enabled = !acceptLoading,
                        style = NonnaButtonStyle.Outline,
                        fullWidth = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !acceptLoading) { dontShowAgain = !dontShowAgain },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = dontShowAgain,
                            onCheckedChange = { if (!acceptLoading) dontShowAgain = it },
                            enabled = !acceptLoading
                        )
                        Text(
                            text = stringResource(R.string.invite_welcome_modal_dont_show_again),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    NonnaButton(
                        text = stringResource(R.string.common_close),
                        onClick = { onDismiss(dontShowAgain) },
                        testTag = "home_invite_close_button",
                        enabled = !acceptLoading,
                        style = NonnaButtonStyle.Outline,
                        fullWidth = true
                    )
                }
            }
            }
        }
    }
}

@Composable
private fun GreetingSection(userName: String) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> stringResource(R.string.home_greeting_morning)
        hour < 19 -> stringResource(R.string.home_greeting_afternoon)
        else -> stringResource(R.string.home_greeting_night)
    }
    val message = when {
        hour < 12 -> stringResource(R.string.home_message_morning)
        hour < 19 -> stringResource(R.string.home_message_afternoon)
        else -> stringResource(R.string.home_message_night)
    }
    
    Column {
        Text(
            text = stringResource(R.string.home_hello_user, userName),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.home_greeting_with_message, greeting, message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ContinueSection(
    cofreName: String,
    cofreRelation: String,
    coverImageUrl: String? = null,
    onClick: () -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.home_continue_where_left),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = NonnaCorners.Card,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 5.dp,
                pressedElevation = 8.dp
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
                        .size(80.dp)
                        .clip(NonnaCorners.Medium)
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
                            contentDescription = cofreName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cofreName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = relationValueLabel(cofreRelation),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    imageVector = Icons.Outlined.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun DailyPromptSection(
    onAddMemory: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = NonnaCorners.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFFDED6CB)
                )
                .padding(NonnaDimens.cardPaddingLarge)
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.home_close_hint),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.padding(end = 20.dp, top = 2.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.home_daily_prompt_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = stringResource(R.string.home_daily_prompt_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    NonnaButton(
                        text = stringResource(R.string.home_add_memory),
                        onClick = onAddMemory
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onCreateCofre: () -> Unit,
    onAddMemory: () -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.home_quick_actions),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = Icons.Outlined.Add,
                title = stringResource(R.string.home_new_chest),
                description = stringResource(R.string.home_new_chest_desc),
                onClick = onCreateCofre,
                iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            
            QuickActionCard(
                icon = Icons.Outlined.Image,
                title = stringResource(R.string.home_add_memory_title),
                description = stringResource(R.string.home_add_memory_desc),
                onClick = onAddMemory,
                iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                iconTint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
        
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    iconBackgroundColor: androidx.compose.ui.graphics.Color,
    iconTint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = NonnaCorners.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 10.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(NonnaDimens.cardPadding)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = iconBackgroundColor,
                        shape = NonnaCorners.Medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = iconTint
                )
            }
            
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun parseCofreUpdatedAt(iso: String?): java.time.Instant? {
    if (iso.isNullOrBlank()) return null
    return try {
        java.time.Instant.parse(iso)
    } catch (_: Exception) {
        null
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_5")
@Composable
private fun HomeScreenPreview() {
    NonnaTheme {
        HomeScreen(
            uiState = HomeUiState(),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF7F0)
@Composable
private fun GreetingSectionPreview() {
    NonnaTheme {
        GreetingSection(userName = "María")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF7F0)
@Composable
private fun DailyPromptSectionPreview() {
    NonnaTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DailyPromptSection(
                onAddMemory = {},
                onDismiss = {}
            )
        }
    }
}
