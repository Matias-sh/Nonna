package com.cocido.nonna.ui.screens.cofres

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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cocido.nonna.data.mock.mockCofres
import com.cocido.nonna.ui.components.AppShell
import com.cocido.nonna.ui.components.CofreCard
import com.cocido.nonna.ui.components.CofreFilters
import com.cocido.nonna.ui.components.EmptyStateWithButton
import com.cocido.nonna.ui.components.FilterChipsRow
import com.cocido.nonna.ui.components.NonnaTab
import com.cocido.nonna.ui.components.NonnaTextField
import com.cocido.nonna.ui.components.SimpleHeader
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CofresListScreen(
    onTabSelected: (NonnaTab) -> Unit,
    onCofreClick: (String) -> Unit,
    onCreateCofre: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(CofreFilters.todos.id) }
    
    val filteredCofres = mockCofres.filter { cofre ->
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
        Scaffold(
            floatingActionButton = {
                if (mockCofres.isNotEmpty()) {
                    FloatingActionButton(
                        onClick = onCreateCofre,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(bottom = 72.dp) // Above bottom nav
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nuevo cofre"
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header
                SimpleHeader(
                    title = "Cofres",
                    subtitle = "Tus espacios de memoria familiar"
                )
                
                if (mockCofres.isEmpty()) {
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
                    // Search
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
                            columns = GridCells.Adaptive(minSize = 280.dp),
                            contentPadding = PaddingValues(
                                start = NonnaDimens.screenPaddingHorizontal,
                                end = NonnaDimens.screenPaddingHorizontal,
                                bottom = 100.dp // Bottom nav + FAB padding
                            ),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredCofres) { cofre ->
                                CofreCard(
                                    cofre = cofre,
                                    onClick = { onCofreClick(cofre.id) }
                                )
                            }
                        }
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
