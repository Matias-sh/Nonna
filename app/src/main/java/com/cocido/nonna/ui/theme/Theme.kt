package com.cocido.nonna.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light Color Scheme - NONNA warm palette
private val NonnaLightColorScheme = lightColorScheme(
    // Primary colors
    primary = Caramel,
    onPrimary = Color.White,
    primaryContainer = Caramel.copy(alpha = 0.1f),
    onPrimaryContainer = Caramel,
    
    // Secondary colors
    secondary = Olive,
    onSecondary = Color.White,
    secondaryContainer = Color(0x1A8B9556),
    onSecondaryContainer = Olive,
    
    // Tertiary (accent)
    tertiary = Accent,
    onTertiary = Sepia,
    tertiaryContainer = Accent,
    onTertiaryContainer = Sepia,
    
    // Background
    background = Cream,
    onBackground = Caramel,
    
    // Surface
    surface = CardWhite,
    onSurface = Caramel,
    surfaceVariant = Muted,
    onSurfaceVariant = MutedForeground,
    surfaceTint = Caramel,
    
    // Inverse
    inverseSurface = Sepia,
    inverseOnSurface = Cream,
    inversePrimary = CaramelDark,
    
    // Error
    error = Destructive,
    onError = Color.White,
    errorContainer = Color(0x1AB84A3C),
    onErrorContainer = Destructive,
    
    // Outline
    outline = BorderLight,
    outlineVariant = Muted,
    
    // Scrim
    scrim = Color(0x80000000)
)

// Dark Color Scheme
private val NonnaDarkColorScheme = darkColorScheme(
    // Primary colors
    primary = CaramelDark,
    onPrimary = Color.White,
    primaryContainer = Color(0x33D88A71),
    onPrimaryContainer = CaramelDark,
    
    // Secondary colors
    secondary = OliveDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0x33A3B06C),
    onSecondaryContainer = OliveDark,
    
    // Tertiary (accent)
    tertiary = AccentDark,
    onTertiary = SepiaDark,
    tertiaryContainer = AccentDark,
    onTertiaryContainer = SepiaDark,
    
    // Background
    background = CreamDark,
    onBackground = SepiaDark,
    
    // Surface
    surface = CardDark,
    onSurface = SepiaDark,
    surfaceVariant = MutedDark,
    onSurfaceVariant = MutedForegroundDark,
    surfaceTint = CaramelDark,
    
    // Inverse
    inverseSurface = SepiaDark,
    inverseOnSurface = CreamDark,
    inversePrimary = Caramel,
    
    // Error
    error = DestructiveDark,
    onError = Color.White,
    errorContainer = Color(0x33C96B5D),
    onErrorContainer = DestructiveDark,
    
    // Outline
    outline = BorderDark,
    outlineVariant = MutedDark,
    
    // Scrim
    scrim = Color(0x80000000)
)

@Composable
fun NonnaTheme(
    darkTheme: Boolean = false, // Forzado a modo claro - deshabilitado modo oscuro
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NonnaDarkColorScheme else NonnaLightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = NonnaTypography,
        shapes = NonnaShapes,
        content = content
    )
}

/**
 * Extension to access custom NONNA colors not in Material3 scheme
 */
object NonnaColors {
    // Semantic text/feedback aliases
    val textPrimary = TextPrimary
    val textSecondary = TextSecondary
    val textDisabled = TextDisabled
    val success = Success
    val warning = Warning

    // Emotional tags
    val tagAlegre = TagAlegreBackground
    val tagAlegreText = TagAlegreText
    val tagNostalgico = TagNostalgicoBackground
    val tagNostalgicoText = TagNostalgicoText
    val tagCalmo = TagCalmoBackground
    val tagCalmoText = TagCalmoText
    val tagFamiliar = TagFamiliarBackground
    val tagFamiliarText = TagFamiliarText
    
    // Role badges
    val roleCreador = RoleCreadorBackground
    val roleCreadorText = RoleCreadorText
    val roleColaborador = RoleColaboradorBackground
    val roleColaboradorText = RoleColaboradorText
    val roleInvitado = RoleInvitadoBackground
    val roleInvitadoText = RoleInvitadoText
    val roleAbuelo = RoleAbueloBackground
    val roleAbueloText = RoleAbueloText
    
    // Input
    val inputBackground = InputBackground
}
