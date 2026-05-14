package com.cocido.nonna.ui.theme

import androidx.compose.ui.graphics.Color

// NONNA Warm Color Palette - Light Mode
val Cream = Color(0xFFFAF7F0)          // Background
val Caramel = Color(0xFFAE7C4B)        // Primary
val Olive = Color(0xFF8B9556)          // Secondary
val Sepia = Color(0xFF4A4238)          // Foreground/Text
val Muted = Color(0xFFE8E3DA)          // Muted background
val MutedForeground = Color(0xFF756F66) // Muted text
val Accent = Color(0xFFD4CFC4)         // Accent background
val CardWhite = Color(0xFFFFFFFF)      // Card background
val InputBackground = Color(0xFFF5F1E8) // Input background
val Destructive = Color(0xFFB84A3C)    // Error/Delete

// Border colors
val BorderLight = Color(0x264A4238)    // 15% opacity of Sepia

// Dark Mode Colors
val CreamDark = Color(0xFF2B2520)
val CaramelDark = Color(0xFFC99766)
val OliveDark = Color(0xFFA3B06C)
val SepiaDark = Color(0xFFF5F1E8)
val MutedDark = Color(0xFF3D3731)
val MutedForegroundDark = Color(0xFFB5AFA6)
val AccentDark = Color(0xFF4A433D)
val CardDark = Color(0xFF342F2A)
val BorderDark = Color(0x26F5F1E8)
val DestructiveDark = Color(0xFFC96B5D)

// Emotional Tag Colors
val TagAlegreBackground = Color(0xFFFEF9C3)
val TagAlegreText = Color(0xFFA16207)
val TagAlepreBorder = Color(0xFFFEF08A)

val TagNostalgicoBackground = Color(0xFFFEF3C7)
val TagNostalgicoText = Color(0xFFB45309)
val TagNostalgicoBorder = Color(0xFFFDE68A)

val TagCalmoBackground = Color(0xFFDBEAFE)
val TagCalmoText = Color(0xFF1D4ED8)
val TagCalmoBorder = Color(0xFFBFDBFE)

val TagFamiliarBackground = Color(0xFFFFE4E6)
val TagFamiliarText = Color(0xFFBE123C)
val TagFamiliarBorder = Color(0xFFFECDD3)

// Role Badge Colors
val RoleCreadorBackground = Caramel.copy(alpha = 0.1f)
val RoleCreadorText = Caramel
val RoleColaboradorBackground = Color(0x1A8B9556)
val RoleColaboradorText = Olive
val RoleInvitadoBackground = Muted
val RoleInvitadoText = MutedForeground
val RoleAbueloBackground = Color(0x1AF59E0B)
val RoleAbueloText = Color(0xFFD97706)

// Gradients (for use with Brush)
val PrimaryGradientStart = Caramel
val PrimaryGradientEnd = Olive

// Semantic aliases for DS consumption
val TextPrimary = Sepia
val TextSecondary = MutedForeground
val TextDisabled = MutedForeground.copy(alpha = 0.5f)
val Success = Olive
val Warning = Color(0xFFD97706)
