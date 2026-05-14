package com.cocido.nonna.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.cocido.nonna.ui.theme.NonnaTheme
import org.junit.Rule
import org.junit.Test

class EmptyStateTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyState_rendersTitleAndDescription() {
        composeRule.setContent {
            NonnaTheme {
                EmptyState(
                    icon = Icons.Outlined.Inventory2,
                    title = "Sin cofres",
                    description = "Crea uno para comenzar"
                )
            }
        }

        composeRule.onNodeWithText("Sin cofres").assertIsDisplayed()
        composeRule.onNodeWithText("Crea uno para comenzar").assertIsDisplayed()
    }
}

