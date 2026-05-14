package com.cocido.nonna.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cocido.nonna.ui.theme.NonnaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NonnaButtonTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun primaryButton_emitsClick_whenEnabled() {
        var clicked = 0
        composeRule.setContent {
            NonnaTheme {
                NonnaButton(
                    text = "Guardar",
                    onClick = { clicked++ }
                )
            }
        }

        composeRule.onNodeWithText("Guardar").performClick()
        assertEquals(1, clicked)
    }

    @Test
    fun button_isDisabled_whenEnabledFalse() {
        composeRule.setContent {
            NonnaTheme {
                NonnaButton(
                    text = "Deshabilitado",
                    onClick = {},
                    enabled = false
                )
            }
        }

        composeRule.onNodeWithText("Deshabilitado").assertIsNotEnabled()
    }
}

