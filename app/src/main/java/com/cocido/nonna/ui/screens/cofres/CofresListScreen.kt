package com.cocido.nonna.ui.screens.cofres

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.cocido.nonna.R
import com.cocido.nonna.ui.viewmodel.CofresListViewModel
import com.cocido.nonna.ui.components.AppShell
import com.cocido.nonna.ui.components.RefreshOnResume
import com.cocido.nonna.ui.components.CofreCard
import com.cocido.nonna.ui.components.CofreFilters
import com.cocido.nonna.ui.components.EmptyStateWithButton
import com.cocido.nonna.ui.components.FilterChipsRow
import com.cocido.nonna.ui.components.localizedCofreFilters
import com.cocido.nonna.ui.components.NonnaTab
import com.cocido.nonna.ui.components.NonnaTextField
import com.cocido.nonna.ui.components.NonnaStaggerItem
import com.cocido.nonna.ui.components.NonnaMotion
import com.cocido.nonna.ui.components.SimpleHeader
import com.cocido.nonna.ui.components.rememberMotionInteractionSource
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CofresListRoute(
    onTabSelected: (NonnaTab) -> Unit,
    onCofreClick: (String) -> Unit,
    onCreateCofre: () -> Unit,
    viewModel: CofresListViewModel = hiltViewModel()
) {
    val cofres by viewModel.cofres.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val uiState = remember(cofres, isLoading, errorMessage) {
        CofresListUiState(
            isLoading = isLoading,
            cofres = cofres,
            errorMessage = errorMessage
        )
    }

    LaunchedEffect(Unit) { viewModel.load() }
    RefreshOnResume { viewModel.refreshOnResume() }

    CofresListScreen(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is CofresListEvent.OpenCofre -> onCofreClick(event.cofreId)
                CofresListEvent.CreateCofre -> onCreateCofre()
                is CofresListEvent.SelectTab -> onTabSelected(event.tab)
            }
        }
    )
}

@Composable
fun CofresListScreen(
    uiState: CofresListUiState,
    onEvent: (CofresListEvent) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var activeFilter by rememberSaveable { mutableStateOf(CofreFilters.todos.id) }
    val filteredCofres by remember(uiState.cofres, searchQuery, activeFilter) {
        derivedStateOf {
            uiState.cofres.filter { cofre ->
                val matchesSearch = cofre.name.contains(searchQuery, ignoreCase = true) ||
                    cofre.relation.contains(searchQuery, ignoreCase = true)

                if (!matchesSearch) return@filter false
                when (activeFilter) {
                    CofreFilters.mios.id -> cofre.isOwner
                    CofreFilters.compartidos.id -> !cofre.isOwner
                    else -> true
                }
            }
        }
    }
    
    AppShell(
        currentTab = NonnaTab.Cofres,
        onTabSelected = { onEvent(CofresListEvent.SelectTab(it)) }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                SimpleHeader(
                    title = stringResource(R.string.chests_title),
                    subtitle = stringResource(R.string.chests_subtitle)
                )
                
                if (uiState.isLoading && uiState.cofres.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(NonnaDimens.screenPaddingHorizontal),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                } else if (uiState.cofres.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(NonnaDimens.screenPaddingHorizontal),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateWithButton(
                            icon = Icons.Outlined.Inventory2,
                            title = stringResource(R.string.chests_empty_title),
                            description = stringResource(R.string.chests_empty_description),
                            buttonText = stringResource(R.string.select_chest_empty_button),
                            buttonTestTag = "cofres_empty_create_button",
                            onButtonClick = { onEvent(CofresListEvent.CreateCofre) }
                        )
                    }
                } else {
                    NonnaTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        testTag = "cofres_search_input",
                        placeholder = stringResource(R.string.chests_search_placeholder),
                        leadingIcon = Icons.Outlined.Search,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = NonnaDimens.screenPaddingHorizontal)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Filter chips
                    FilterChipsRow(
                        chips = localizedCofreFilters(),
                        selectedChipId = activeFilter,
                        onChipSelected = { activeFilter = it },
                        modifier = Modifier.padding(horizontal = NonnaDimens.screenPaddingHorizontal)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Cofres grid
                    if (filteredCofres.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(NonnaDimens.spacing24),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.chests_no_results),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            modifier = Modifier.weight(1f),
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(
                                start = NonnaDimens.screenPaddingHorizontal,
                                end = NonnaDimens.screenPaddingHorizontal,
                                bottom = 24.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(filteredCofres, key = { _, cofre -> cofre.id }) { index, cofre ->
                                NonnaStaggerItem(index = index, stepDelayMs = 0) {
                                    CofreCard(
                                        cofre = cofre,
                                        onClick = { onEvent(CofresListEvent.OpenCofre(cofre.id)) },
                                        modifier = Modifier
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.cofres.isNotEmpty()) {
                val fabInteraction = rememberMotionInteractionSource()
                val fabPressed by fabInteraction.collectIsPressedAsState()
                val fabScale by animateFloatAsState(
                    targetValue = when {
                        fabPressed -> 0.9f
                        else -> 1f
                    },
                    animationSpec = NonnaMotion.bounceSpring,
                    label = "cofres_fab_scale"
                )
                AnimatedVisibility(
                    visible = uiState.cofres.isNotEmpty(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(NonnaDimens.screenPaddingHorizontal)
                        .padding(bottom = 16.dp),
                    enter = scaleIn(
                        initialScale = 0f,
                        animationSpec = NonnaMotion.bounceSpring
                    )
                ) {
                    FloatingActionButton(
                        onClick = { onEvent(CofresListEvent.CreateCofre) },
                        interactionSource = fabInteraction,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .scale(fabScale)
                            .testTag("cofres_fab_create")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.chests_new_chest_cd)
                        )
                    }
                }
            }
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_5")
@Composable
private fun CofresListScreenPreview() {
    NonnaTheme {
        CofresListScreen(
            uiState = CofresListUiState(),
            onEvent = {}
        )
    }
}
