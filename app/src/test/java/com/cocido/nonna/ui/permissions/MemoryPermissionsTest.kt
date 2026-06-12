package com.cocido.nonna.ui.permissions

import com.cocido.nonna.data.remote.dto.UserDto
import com.cocido.nonna.ui.components.MemoryType
import com.cocido.nonna.ui.components.MemoryUiModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoryPermissionsTest {

    @Test
    fun `canModifyMemory returns true when user id matches creator`() {
        val memory = sampleMemory(addedByUserId = "42", addedByEmail = "creator@test.com")
        val user = UserDto(id = "42", email = "other@test.com")
        assertTrue(canModifyMemory(memory, user))
    }

    @Test
    fun `canModifyMemory returns true when email matches creator`() {
        val memory = sampleMemory(addedByUserId = null, addedByEmail = "creator@test.com")
        val user = UserDto(id = "99", email = "creator@test.com")
        assertTrue(canModifyMemory(memory, user))
    }

    @Test
    fun `canModifyMemory returns false for invited viewer`() {
        val memory = sampleMemory(addedByUserId = "1", addedByEmail = "owner@test.com")
        val user = UserDto(id = "2", email = "guest@test.com")
        assertFalse(canModifyMemory(memory, user))
    }

    private fun sampleMemory(
        addedByUserId: String?,
        addedByEmail: String?
    ) = MemoryUiModel(
        id = "1",
        type = MemoryType.Photo,
        title = "Test",
        date = "2026-06-12",
        addedByUserId = addedByUserId,
        addedByEmail = addedByEmail
    )
}
