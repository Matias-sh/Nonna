package com.cocido.nonna.ui.screens.profile

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

class ProfileScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun profileRendersMainOptions() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            NonnaTheme {
                ProfileScreen(
                    uiState = ProfileUiState(),
                    onEvent = {},
                    onConsumeError = {}
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.profile_options)).assertIsDisplayed()
        composeRule.onNodeWithTag("profile_edit_option").assertIsDisplayed()
    }

    @Test
    fun logoutClick_emitsLogoutEvent() {
        val events = mutableListOf<ProfileEvent>()
        composeRule.setContent {
            NonnaTheme {
                ProfileScreen(
                    uiState = ProfileUiState(),
                    onEvent = { events += it },
                    onConsumeError = {}
                )
            }
        }

        composeRule.onNodeWithTag("profile_logout_action").performClick()
        assertTrue(events.any { it is ProfileEvent.Logout })
    }
}

