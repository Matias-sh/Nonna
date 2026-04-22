package com.cocido.nonna.ui.screens.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.cocido.nonna.R
import com.cocido.nonna.ui.components.CofreCard
import com.cocido.nonna.ui.components.EmptyStateWithButton
import com.cocido.nonna.ui.components.PageHeader
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners

@Composable
fun SelectCofreScreen(
    onBack: () -> Unit,
    onCofreSelected: (String) -> Unit,
    onCreateCofre: () -> Unit,
    viewModel: com.cocido.nonna.ui.viewmodel.CofresListViewModel = hiltViewModel()
) {
    val cofres by viewModel.cofres.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        PageHeader(
            title = stringResource(R.string.select_chest_title),
            subtitle = stringResource(R.string.select_chest_subtitle),
            onBack = onBack
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
                    icon = Icons.Outlined.Favorite,
                    title = stringResource(R.string.select_chest_empty_title),
                    description = stringResource(R.string.select_chest_empty_description),
                    buttonText = stringResource(R.string.select_chest_empty_button),
                    onButtonClick = onCreateCofre
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(NonnaDimens.screenPaddingHorizontal),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cofres) { cofre ->
                    CofreCard(
                        cofre = cofre,
                        onClick = { onCofreSelected(cofre.id) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Info text
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                shape = NonnaCorners.Medium
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.select_chest_info_text),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                item {
                    // Create new cofre option
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onCreateCofre),
                        shape = NonnaCorners.Card,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(NonnaDimens.cardPaddingLarge),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = NonnaCorners.Full
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column {
                                Text(
                                    text = stringResource(R.string.select_chest_create_new_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.select_chest_create_new_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
