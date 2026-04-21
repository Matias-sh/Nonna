package com.cocido.nonna.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocido.nonna.ui.theme.NonnaDimens

enum class NonnaTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    Inicio("inicio", "Inicio", Icons.Outlined.Home),
    Cofres("cofres", "Cofres", Icons.Outlined.Inventory2),
    Perfil("perfil", "Perfil", Icons.Outlined.AccountCircle)
}

@Composable
fun AppShell(
    currentTab: NonnaTab,
    onTabSelected: (NonnaTab) -> Unit,
    showBottomNav: Boolean = true,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Main content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            content()
        }
        
        // Bottom navigation
        if (showBottomNav) {
            NonnaBottomNavigation(
                currentTab = currentTab,
                onTabSelected = onTabSelected
            )
        }
    }
}

@Composable
fun NonnaBottomNavigation(
    currentTab: NonnaTab,
    onTabSelected: (NonnaTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Navigation items
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NonnaTab.entries.forEach { tab ->
                    NavItem(
                        tab = tab,
                        selected = currentTab == tab,
                        onClick = { onTabSelected(tab) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Space for system navigation bar
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            )
        }
    }
}

@Composable
private fun NavItem(
    tab: NonnaTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = rememberMotionInteractionSource()
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = NonnaMotion.bounceSpring,
        label = "bottom_nav_icon_scale"
    )
    val color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .nonnaInteractiveScale(interactionSource = interactionSource, pressed = 0.98f)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null
            )
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with indicator background when selected
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(width = 64.dp, height = 32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
            }
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                modifier = Modifier
                    .size(24.dp)
                    .scale(iconScale),
                tint = color
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Label
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = color,
            fontSize = 12.sp
        )
    }
}

@Composable
fun NonnaSidebar(
    currentTab: NonnaTab,
    onTabSelected: (NonnaTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(NonnaDimens.sidebarWidth)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(NonnaDimens.spacing24)
        ) {
            // Logo
            Text(
                text = "NONNA",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Navigation items
            NonnaTab.entries.forEach { tab ->
                SidebarNavItem(
                    tab = tab,
                    isSelected = currentTab == tab,
                    onClick = { onTabSelected(tab) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SidebarNavItem(
    tab: NonnaTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = rememberMotionInteractionSource()
    val offsetX by animateFloatAsState(
        targetValue = if (isSelected) 4f else 0f,
        animationSpec = NonnaMotion.navSpring,
        label = "sidebar_hover_offset"
    )
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .nonnaInteractiveScale(interactionSource, pressed = 0.98f)
            .padding(start = offsetX.dp),
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = null,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = tab.label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}
