package com.cocido.nonna.ui.screens.profile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cocido.nonna.R
import com.cocido.nonna.ui.theme.NonnaTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun profileRendersMainOptions() {
        val context = composeRule.activity
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
        val context = composeRule.activity
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

