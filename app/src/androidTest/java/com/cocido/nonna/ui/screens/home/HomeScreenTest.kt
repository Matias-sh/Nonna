package com.cocido.nonna.ui.screens.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.cocido.nonna.R
import com.cocido.nonna.ui.theme.NonnaTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyState_showsExpectedTexts() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            NonnaTheme {
                HomeScreen(
                    uiState = HomeUiState(isLoading = false, cofres = emptyList()),
                    onEvent = {}
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.home_empty_title)).assertIsDisplayed()
        composeRule.onNodeWithTag("home_empty_create_button").assertIsDisplayed()
    }

    @Test
    fun emptyStateCta_emitsCreateCofreEvent() {
        val events = mutableListOf<HomeEvent>()
        composeRule.setContent {
            NonnaTheme {
                HomeScreen(
                    uiState = HomeUiState(isLoading = false, cofres = emptyList()),
                    onEvent = { events += it }
                )
            }
        }

        composeRule.onNodeWithTag("home_empty_create_button").performClick()
        assertTrue(events.any { it is HomeEvent.CreateCofre })
    }
}

