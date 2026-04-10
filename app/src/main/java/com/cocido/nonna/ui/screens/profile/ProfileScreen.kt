package com.cocido.nonna.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cocido.nonna.BuildConfig
import com.cocido.nonna.ui.components.AppShell
import com.cocido.nonna.ui.components.NonnaTab
import com.cocido.nonna.ui.components.SimpleHeader
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.PrimaryGradientEnd
import com.cocido.nonna.ui.theme.PrimaryGradientStart
import com.cocido.nonna.ui.theme.NonnaTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ProfileScreen(
    onTabSelected: (NonnaTab) -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    viewModel: com.cocido.nonna.ui.viewmodel.ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    val displayUser = user
    // En perfil mostramos el nombre \"humano\" (persona/nombre) más que el username técnico.
    val userName = displayUser?.displayName() ?: ""
    val userEmail = displayUser?.email ?: ""
    val avatarUrl = displayUser?.profileImageUrl()
    val joinedDate = displayUser?.createdAt ?: displayUser?.created_at ?: ""

    AppShell(
        currentTab = NonnaTab.Perfil,
        onTabSelected = onTabSelected
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SimpleHeader(
                title = "Perfil",
                subtitle = "Tu cuenta y configuración"
            )
            
            Column(
                modifier = Modifier.padding(horizontal = NonnaDimens.screenPaddingHorizontal)
            ) {
                // User card
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
                            .padding(NonnaDimens.cardPaddingLarge),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            PrimaryGradientStart.copy(alpha = 0.2f),
                                            PrimaryGradientEnd.copy(alpha = 0.2f)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarUrl != null) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Info: nombre de usuario (o nombre completo) y email
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (userEmail.isNotBlank()) {
                                Text(
                                    text = userEmail,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isLoading) {
                                Spacer(modifier = Modifier.height(12.dp))
                                androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                        
                        IconButton(onClick = onOpenSettings) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Configuración"
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Options
                Text(
                    text = "Opciones",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OptionCard(
                    icon = Icons.Outlined.Settings,
                    title = "Configuración",
                    onClick = onOpenSettings
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Logout
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = NonnaCorners.Medium,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(NonnaDimens.cardPadding),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cerrar sesión",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Footer info
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (joinedDate.isNotBlank()) "Miembro desde $joinedDate" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Versión ${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(100.dp)) // Bottom nav padding
            }
        }
    }
}

@Composable
private fun OptionCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    badge: String? = null
) {
    Card(
        onClick = onClick,
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (badge != null) {
                Surface(
                    shape = NonnaCorners.Full,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_5")
@Composable
private fun ProfileScreenPreview() {
    NonnaTheme {
        ProfileScreen(
            onTabSelected = {},
            onOpenSettings = {},
            onLogout = {}
        )
    }
}
