package com.cocido.nonna.ui.screens.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.cocido.nonna.R
import com.cocido.nonna.data.repository.NotificationUiModel
import com.cocido.nonna.ui.theme.NonnaTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NotificationsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyState_muestraMensaje() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            NonnaTheme {
                NotificationsScreen(
                    uiState = NotificationsUiState(
                        isLoading = false,
                        items = emptyList()
                    ),
                    onEvent = {}
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.notifications_empty_state)).assertIsDisplayed()
    }

    @Test
    fun botonCargarMas_emiteEvento() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val events = mutableListOf<NotificationsEvent>()
        composeRule.setContent {
            NonnaTheme {
                NotificationsScreen(
                    uiState = NotificationsUiState(
                        items = listOf(
                            NotificationUiModel(
                                id = 1L,
                                title = "Aviso",
                                message = "Mensaje de prueba",
                                type = "GENERAL",
                                payloadType = null,
                                paymentId = null,
                                isRead = false,
                                createdAt = null
                            )
                        )
                    ),
                    onEvent = { events += it }
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.notifications_load_more)).performClick()
        assertTrue(events.any { it is NotificationsEvent.LoadMore })
    }
}
