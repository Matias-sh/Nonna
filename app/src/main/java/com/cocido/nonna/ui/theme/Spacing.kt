package com.cocido.nonna.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Foundation spacing scale for all Compose UI surfaces.
 *
 * Use semantic aliases (`screenHorizontal`, `componentGap`) in components/pantallas
 * and keep raw scale usage for low-level DS building blocks.
 */
object NonnaSpacing {
    val none: Dp = 0.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp

    // Semantic aliases
    val screenHorizontal: Dp = lg
    val screenVertical: Dp = xl
    val componentGap: Dp = lg
    val contentInset: Dp = lg
    val sectionGap: Dp = xxl
}

