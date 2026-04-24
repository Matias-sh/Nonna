package com.cocido.nonna.data.repository

import com.cocido.nonna.ui.components.CofreInviteeUiModel
import com.cocido.nonna.ui.components.CofreUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CofreRepositoryAvatarFallbackTest {

    @Test
    fun `applyResolvedInviteeAvatars fills missing avatar by normalized email`() {
        val cofre = CofreUiModel(
            id = "1",
            name = "Nonna",
            relation = "Abuela",
            invited = listOf(
                CofreInviteeUiModel(
                    id = "a",
                    email = "INVITED@MAIL.COM ",
                    accepted = true,
                    fullName = "Invited User",
                    avatarUrl = null
                )
            )
        )

        val updated = applyResolvedInviteeAvatars(
            cofre = cofre,
            resolvedByEmail = mapOf("invited@mail.com" to "https://cdn.example.com/avatar.jpg")
        )

        assertEquals("https://cdn.example.com/avatar.jpg", updated.invited.first().avatarUrl)
    }

    @Test
    fun `applyResolvedInviteeAvatars keeps existing avatar over fallback`() {
        val cofre = CofreUiModel(
            id = "1",
            name = "Nonna",
            relation = "Abuela",
            invited = listOf(
                CofreInviteeUiModel(
                    id = "a",
                    email = "invited@mail.com",
                    accepted = true,
                    fullName = "Invited User",
                    avatarUrl = "https://cdn.example.com/current.jpg"
                )
            )
        )

        val updated = applyResolvedInviteeAvatars(
            cofre = cofre,
            resolvedByEmail = mapOf("invited@mail.com" to "https://cdn.example.com/fallback.jpg")
        )

        assertEquals("https://cdn.example.com/current.jpg", updated.invited.first().avatarUrl)
    }

    @Test
    fun `applyResolvedInviteeAvatars leaves avatar null when no email match`() {
        val cofre = CofreUiModel(
            id = "1",
            name = "Nonna",
            relation = "Abuela",
            invited = listOf(
                CofreInviteeUiModel(
                    id = "a",
                    email = "invited@mail.com",
                    accepted = true,
                    fullName = "Invited User",
                    avatarUrl = null
                )
            )
        )

        val updated = applyResolvedInviteeAvatars(
            cofre = cofre,
            resolvedByEmail = mapOf("another@mail.com" to "https://cdn.example.com/avatar.jpg")
        )

        assertNull(updated.invited.first().avatarUrl)
    }
}
