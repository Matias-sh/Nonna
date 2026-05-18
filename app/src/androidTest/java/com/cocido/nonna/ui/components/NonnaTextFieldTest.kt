package com.cocido.nonna.ui.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.cocido.nonna.ui.theme.NonnaTheme
import org.junit.Rule
import org.junit.Test

class NonnaTextFieldTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun textField_updatesValue_andShowsErrorMessage() {
        composeRule.setContent {
            NonnaTheme {
                val value = remember { mutableStateOf("") }
                NonnaTextField(
                    value = value.value,
                    onValueChange = { value.value = it },
                    testTag = "test_text_field",
                    label = "Nombre",
                    placeholder = "Ingresa nombre",
                    isError = true,
                    errorMessage = "Campo obligatorio"
                )
            }
        }

        composeRule.onNodeWithTag("test_text_field").performTextInput("Ana")
        composeRule.onNodeWithText("Ana").assertIsDisplayed()
        composeRule.onNodeWithText("Campo obligatorio").assertIsDisplayed()
    }
}

