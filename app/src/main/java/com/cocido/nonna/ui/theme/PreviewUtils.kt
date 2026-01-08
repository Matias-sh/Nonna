package com.cocido.nonna.ui.theme

import androidx.compose.ui.tooling.preview.Preview

/**
 * Preview con tema claro
 */
@Preview(
    name = "Light Mode",
    showBackground = true,
    backgroundColor = 0xFFFAF7F0
)
annotation class LightPreview

/**
 * Preview con tema oscuro
 */
@Preview(
    name = "Dark Mode",
    showBackground = true,
    backgroundColor = 0xFF2B2520,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
annotation class DarkPreview

/**
 * Preview combinada (light + dark)
 */
@LightPreview
@DarkPreview
annotation class ThemePreview

/**
 * Preview de pantalla completa
 */
@Preview(
    name = "Full Screen",
    showBackground = true,
    showSystemUi = true,
    device = "id:pixel_5"
)
annotation class FullScreenPreview
