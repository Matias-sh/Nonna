package com.cocido.nonna.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cocido.nonna.ui.theme.NonnaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NonnaErrorStateTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun errorState_showsRetry_andEmitsCallback() {
        var retryCount = 0
        composeRule.setContent {
            NonnaTheme {
                NonnaErrorState(
                    title = "Error",
                    description = "No se pudo cargar",
                    retryLabel = "Reintentar",
                    onRetry = { retryCount++ }
                )
            }
        }

        composeRule.onNodeWithText("Error").assertIsDisplayed()
        composeRule.onNodeWithText("Reintentar").performClick()
        assertEquals(1, retryCount)
    }
}

