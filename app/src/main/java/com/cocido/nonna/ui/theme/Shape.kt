package com.cocido.nonna.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// NONNA Shape System - Soft, rounded corners
val NonnaShapes = Shapes(
    // Extra small for badges, chips
    extraSmall = RoundedCornerShape(4.dp),
    
    // Small - 8dp (radius-sm)
    small = RoundedCornerShape(8.dp),
    
    // Medium - 12dp (radius-md) - default
    medium = RoundedCornerShape(12.dp),
    
    // Large - 16dp (radius-lg)
    large = RoundedCornerShape(16.dp),
    
    // Extra large - 20dp (radius-xl)
    extraLarge = RoundedCornerShape(20.dp)
)

// Custom shapes for specific components
object NonnaCorners {
    val None = RoundedCornerShape(0.dp)
    val ExtraSmall = RoundedCornerShape(4.dp)
    val Small = RoundedCornerShape(8.dp)
    val Medium = RoundedCornerShape(12.dp)
    val Large = RoundedCornerShape(16.dp)
    val ExtraLarge = RoundedCornerShape(20.dp)
    val Full = RoundedCornerShape(50)
    
    // Card corners
    val Card = RoundedCornerShape(12.dp)
    
    // Button corners
    val Button = RoundedCornerShape(12.dp)
    val ButtonSmall = RoundedCornerShape(8.dp)
    val ButtonPill = RoundedCornerShape(50)
    
    // Input corners
    val Input = RoundedCornerShape(12.dp)
    
    // Image corners
    val ImageSmall = RoundedCornerShape(8.dp)
    val ImageMedium = RoundedCornerShape(12.dp)
    val ImageLarge = RoundedCornerShape(16.dp)
    
    // Avatar
    val Avatar = RoundedCornerShape(50)
    
    // Modal/Sheet
    val BottomSheet = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val Dialog = RoundedCornerShape(20.dp)
}
