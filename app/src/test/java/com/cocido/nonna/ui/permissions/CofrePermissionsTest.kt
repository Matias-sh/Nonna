package com.cocido.nonna.ui.permissions

import com.cocido.nonna.data.remote.dto.UserDto
import com.cocido.nonna.ui.components.CofreInviteeUiModel
import com.cocido.nonna.ui.components.CofreUiModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CofrePermissionsTest {

    @Test
    fun `canManageCofre returns false for null`() {
        assertFalse(canManageCofre(null))
    }

    @Test
    fun `resolveOwnership marks invited user as non owner`() {
        val cofre = baseCofre(
            isOwner = true,
            ownerEmail = "owner@nonna.com",
            invited = listOf(CofreInviteeUiModel(id = "1", email = "guest@nonna.com", accepted = true, fullName = null))
        )
        val currentUser = UserDto(id = "u-1", email = "guest@nonna.com", name = "Guest")

        val resolved = resolveOwnership(cofre, currentUser)

        assertFalse(resolved.isOwner)
        assertFalse(canManageCofre(resolved))
    }

    @Test
    fun `resolveOwnership keeps owner as owner when email matches`() {
        val cofre = baseCofre(
            isOwner = false,
            ownerEmail = "owner@nonna.com"
        )
        val currentUser = UserDto(id = "u-2", email = "owner@nonna.com", name = "Owner")

        val resolved = resolveOwnership(cofre, currentUser)

        assertTrue(resolved.isOwner)
        assertTrue(canManageCofre(resolved))
    }

    @Test
    fun `resolveOwnership preserves backend isOwner when owner email missing`() {
        val cofre = baseCofre(isOwner = true, ownerEmail = null)
        val currentUser = UserDto(id = "u-3", email = "someone@nonna.com", name = "Someone")

        val resolved = resolveOwnership(cofre, currentUser)

        assertTrue(resolved.isOwner)
    }

    private fun baseCofre(
        isOwner: Boolean,
        ownerEmail: String?,
        invited: List<CofreInviteeUiModel> = emptyList()
    ): CofreUiModel = CofreUiModel(
        id = "cofre-1",
        name = "Nonna",
        relation = "Abuela",
        isOwner = isOwner,
        ownerEmail = ownerEmail,
        invited = invited
    )
}
