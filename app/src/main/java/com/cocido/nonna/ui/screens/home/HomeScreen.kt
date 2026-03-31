package com.cocido.nonna.ui.screens.home

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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.cocido.nonna.ui.components.AppShell
import com.cocido.nonna.ui.components.EmptyStateWithButton
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaTab
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.NonnaTheme
import com.cocido.nonna.ui.theme.PrimaryGradientEnd
import com.cocido.nonna.ui.theme.PrimaryGradientStart
import androidx.compose.ui.tooling.preview.Preview
import java.util.Calendar

@Composable
fun HomeScreen(
    onTabSelected: (NonnaTab) -> Unit,
    onCreateCofre: () -> Unit,
    onAddMemory: () -> Unit,
    onContinueCofre: (String) -> Unit,
    onVerInvitaciones: () -> Unit = {},
    viewModel: com.cocido.nonna.ui.viewmodel.HomeViewModel = hiltViewModel()
) {
    val cofres by viewModel.cofres.collectAsState()
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val invitacionesCount by viewModel.invitacionesPendientesCount.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    val hasData = cofres.isNotEmpty()
    val lastCofre = cofres.firstOrNull()
    val userName = user?.displayName()?.split(" ")?.firstOrNull() ?: ""

    AppShell(
        currentTab = NonnaTab.Inicio,
        onTabSelected = onTabSelected
    ) {
        if (isLoading && cofres.isEmpty()) {
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
                    title = "Bienvenido a NONNA",
                    description = "Creá tu primer cofre para empezar a preservar las memorias que importan",
                    buttonText = "Crear mi primer cofre",
                    onButtonClick = onCreateCofre
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

                // Banner de invitaciones pendientes
                if (invitacionesCount > 0) {
                    InvitacionesBanner(
                        count = invitacionesCount,
                        onClick = onVerInvitaciones
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Greeting
                GreetingSection(userName = userName)

                Spacer(modifier = Modifier.height(32.dp))
                
                // Continue where you left off
                if (lastCofre != null) {
                    ContinueSection(
                        cofreName = lastCofre.name,
                        cofreRelation = lastCofre.relation,
                        coverImageUrl = lastCofre.coverImageUrl?.takeIf { it.isNotBlank() && it != "string" },
                        onClick = { onContinueCofre(lastCofre.id) }
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
                
                // Daily prompt
                DailyPromptSection(onAddMemory = onAddMemory)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Quick actions
                QuickActionsSection(
                    onCreateCofre = onCreateCofre,
                    onAddMemory = onAddMemory,
                    onInviteFamily = onVerInvitaciones
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Upcoming dates
                UpcomingDatesSection()
                
                Spacer(modifier = Modifier.height(100.dp)) // Bottom nav padding
            }
        }
    }
}

@Composable
private fun GreetingSection(userName: String) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Buenos días"
        hour < 19 -> "Buenas tardes"
        else -> "Buenas noches"
    }
    val message = when {
        hour < 12 -> "¿Qué recuerdo vamos a guardar hoy?"
        hour < 19 -> "Un buen momento para recordar."
        else -> "Las memorias viven para siempre aquí."
    }
    
    Column {
        Text(
            text = "Hola, $userName",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "$greeting. $message",
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
            text = "Continuar donde quedaste",
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
                        text = cofreRelation,
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
private fun DailyPromptSection(onAddMemory: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = NonnaCorners.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PrimaryGradientStart.copy(alpha = 0.1f),
                            PrimaryGradientEnd.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(NonnaDimens.cardPaddingLarge)
        ) {
            Row(verticalAlignment = Alignment.Top) {
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
                        text = "Hoy es un buen día para guardar algo",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Una foto, una anécdota, la voz de alguien querido... Cada recuerdo cuenta.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    NonnaButton(
                        text = "Agregar recuerdo",
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
    onAddMemory: () -> Unit,
    onInviteFamily: () -> Unit
) {
    Column {
        Text(
            text = "Acciones rápidas",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = Icons.Outlined.Add,
                title = "Nuevo Cofre",
                description = "Creá un espacio para más memorias",
                onClick = onCreateCofre,
                iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            
            QuickActionCard(
                icon = Icons.Outlined.Image,
                title = "Agregar Recuerdo",
                description = "Foto, audio o texto",
                onClick = onAddMemory,
                iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                iconTint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        QuickActionCard(
            icon = Icons.Outlined.Favorite,
            title = "Invitar Familia",
            description = "Construyan juntos este legado",
            onClick = onInviteFamily,
            iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.fillMaxWidth()
        )
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

@Composable
private fun UpcomingDatesSection() {
    Column {
        Text(
            text = "Próximas fechas significativas",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "No hay fechas registradas. Agregá cumpleaños o aniversarios en los detalles del cofre.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InvitacionesBanner(
    count: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = NonnaCorners.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Mail,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (count == 1) "Tenés 1 invitación pendiente"
                    else "Tenés $count invitaciones pendientes",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Alguien te invitó a un cofre de recuerdos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Ver invitaciones",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_5")
@Composable
private fun HomeScreenPreview() {
    NonnaTheme {
        HomeScreen(
            onTabSelected = {},
            onCreateCofre = {},
            onAddMemory = {},
            onContinueCofre = {}
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
            DailyPromptSection(onAddMemory = {})
        }
    }
}
