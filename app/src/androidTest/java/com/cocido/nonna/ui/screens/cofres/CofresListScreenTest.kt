package com.cocido.nonna.ui.screens.cofres

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cocido.nonna.R
import com.cocido.nonna.ui.components.CofreUiModel
import com.cocido.nonna.ui.theme.NonnaTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CofresListScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyState_showsTitleAndCta() {
        val context = composeRule.activity
        composeRule.setContent {
            NonnaTheme {
                CofresListScreen(
                    uiState = CofresListUiState(isLoading = false, cofres = emptyList()),
                    onEvent = {}
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.chests_empty_title)).assertIsDisplayed()
        composeRule.onNodeWithTag("cofres_empty_create_button").assertIsDisplayed()
    }

    @Test
    fun whenHasData_fabCreate_emitsEvent() {
        val context = composeRule.activity
        val emitted = mutableListOf<CofresListEvent>()
        composeRule.setContent {
            NonnaTheme {
                CofresListScreen(
                    uiState = CofresListUiState(
                        cofres = listOf(
                            CofreUiModel(
                                id = "1",
                                name = "Cofre de Ana",
                                relation = "madre"
                            )
                        )
                    ),
                    onEvent = { emitted += it }
                )
            }
        }

        composeRule.onNodeWithTag("cofres_fab_create").performClick()
        assertTrue(emitted.any { it is CofresListEvent.CreateCofre })
    }
}

