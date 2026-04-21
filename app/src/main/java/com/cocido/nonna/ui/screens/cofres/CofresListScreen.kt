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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.cocido.nonna.ui.viewmodel.CofresListViewModel
import com.cocido.nonna.ui.components.AppShell
import com.cocido.nonna.ui.components.CofreCard
import com.cocido.nonna.ui.components.CofreFilters
import com.cocido.nonna.ui.components.EmptyStateWithButton
import com.cocido.nonna.ui.components.FilterChipsRow
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
fun CofresListScreen(
    onTabSelected: (NonnaTab) -> Unit,
    onCofreClick: (String) -> Unit,
    onCreateCofre: () -> Unit,
    viewModel: CofresListViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(CofreFilters.todos.id) }
    val cofres by viewModel.cofres.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var fabVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(cofres.isNotEmpty()) {
        if (cofres.isNotEmpty()) {
            kotlinx.coroutines.delay(300)
            fabVisible = true
        } else {
            fabVisible = false
        }
    }

    val filteredCofres = cofres.filter { cofre ->
        // Apply search
        val matchesSearch = cofre.name.contains(searchQuery, ignoreCase = true) ||
                cofre.relation.contains(searchQuery, ignoreCase = true)
        
        if (!matchesSearch) return@filter false
        
        // Apply filter
        when (activeFilter) {
            CofreFilters.mios.id -> cofre.isOwner
            CofreFilters.compartidos.id -> !cofre.isOwner
            else -> true
        }
    }
    
    AppShell(
        currentTab = NonnaTab.Cofres,
        onTabSelected = onTabSelected
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                SimpleHeader(
                    title = "Cofres",
                    subtitle = "Tus espacios de memoria familiar"
                )
                
                if (isLoading && cofres.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(NonnaDimens.screenPaddingHorizontal),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                } else if (cofres.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(NonnaDimens.screenPaddingHorizontal),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateWithButton(
                            icon = Icons.Outlined.Inventory2,
                            title = "Todavía no tenés cofres",
                            description = "Creá tu primer cofre para empezar a guardar las memorias que importan",
                            buttonText = "Crear primer cofre",
                            onButtonClick = onCreateCofre
                        )
                    }
                } else {
                    NonnaTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "Buscar cofres...",
                        leadingIcon = Icons.Outlined.Search,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = NonnaDimens.screenPaddingHorizontal)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Filter chips
                    FilterChipsRow(
                        chips = CofreFilters.all,
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
                                text = "No se encontraron cofres con ese criterio",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyVerticalGrid(
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
                                NonnaStaggerItem(index = index, stepDelayMs = NonnaMotion.StaggerStepMs) {
                                    CofreCard(
                                        cofre = cofre,
                                        onClick = { onCofreClick(cofre.id) },
                                        modifier = Modifier
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (cofres.isNotEmpty()) {
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
                    visible = fabVisible,
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
                        onClick = onCreateCofre,
                        interactionSource = fabInteraction,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .scale(fabScale)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nuevo cofre"
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
            onTabSelected = {},
            onCofreClick = {},
            onCreateCofre = {}
        )
    }
}
