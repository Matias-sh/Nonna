package com.cocido.nonna.ui.theme

import androidx.compose.ui.unit.dp

/**
 * NONNA Spacing System
 * Based on 4dp grid with common values: 4, 8, 12, 16, 24, 32
 */
object NonnaDimens {
    // Base spacing
    val spacing4 = NonnaSpacing.xs
    val spacing8 = NonnaSpacing.sm
    val spacing12 = NonnaSpacing.md
    val spacing16 = NonnaSpacing.lg
    val spacing20 = 20.dp
    val spacing24 = NonnaSpacing.xl
    val spacing32 = NonnaSpacing.xxl
    val spacing40 = 40.dp
    val spacing48 = 48.dp
    val spacing56 = 56.dp
    val spacing64 = 64.dp
    
    // Common aliases
    val paddingSmall = spacing8
    val paddingMedium = spacing16
    val paddingLarge = spacing24
    val paddingExtraLarge = spacing32
    
    // Screen padding
    val screenPaddingHorizontal = NonnaSpacing.screenHorizontal
    val screenPaddingVertical = NonnaSpacing.screenVertical
    
    // Card
    val cardPadding = spacing16
    val cardPaddingLarge = spacing24
    val cardSpacing = spacing16
    
    // Button
    val buttonHeight = 48.dp
    val buttonHeightSmall = 40.dp
    val buttonHeightLarge = 56.dp
    val buttonPaddingHorizontal = spacing24
    val buttonPaddingVertical = spacing12
    
    // Input
    val inputHeight = 48.dp
    val inputPadding = spacing16
    val inputIconSize = 20.dp
    
    // Icon
    val iconSizeSmall = 16.dp
    val iconSizeMedium = 20.dp
    val iconSizeLarge = 24.dp
    val iconSizeXLarge = 32.dp
    val iconSizeXXLarge = 48.dp
    
    // Avatar
    val avatarSizeSmall = 32.dp
    val avatarSizeMedium = 48.dp
    val avatarSizeLarge = 64.dp
    val avatarSizeXLarge = 80.dp
    val avatarSizeXXLarge = 96.dp
    
    // Cover image
    val coverHeightSmall = 160.dp
    val coverHeightMedium = 192.dp
    val coverHeightLarge = 256.dp
    
    // Bottom navigation
    val bottomNavHeight = 64.dp
    val bottomNavItemSize = 56.dp
    
    // FAB
    val fabSize = 56.dp
    val fabSizeSmall = 48.dp
    
    // Sidebar (desktop)
    val sidebarWidth = 264.dp
    
    // Divider
    val dividerThickness = 1.dp
    
    // Border
    val borderWidth = 1.dp
    val borderWidthThick = 2.dp
    
    // Progress
    val progressHeight = 6.dp
    
    // Thumbnail
    val thumbnailSmall = 64.dp
    val thumbnailMedium = 80.dp
    val thumbnailLarge = 160.dp
    
    // Memory card
    val memoryCardHeight = 160.dp
    val memoryCardHeightList = 80.dp
    
    // Cofre card
    val cofreCardCoverHeight = 160.dp
    
    // Logo
    val logoSizeSmall = 64.dp
    val logoSizeMedium = 96.dp
    val logoSizeLarge = 128.dp
}
