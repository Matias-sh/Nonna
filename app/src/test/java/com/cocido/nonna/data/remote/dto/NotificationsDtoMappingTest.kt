package com.cocido.nonna.data.remote.dto

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class NotificationsDtoMappingTest {

    private val gson = Gson()

    @Test
    fun `debe mapear cuerpo y leido del contrato backend`() {
        val json = """
            {
              "id": 10,
              "titulo": "Invitación a un cofre",
              "cuerpo": "Silvana te invitó al cofre \"Abuela\".",
              "tipo": "INVITACION_COFRE",
              "leido": false,
              "createdAt": "2025-02-10T15:30:00.000Z"
            }
        """.trimIndent()

        val dto = gson.fromJson(json, NotificationDto::class.java)

        assertEquals(10L, dto.id)
        assertEquals("Invitación a un cofre", dto.title)
        assertEquals("Silvana te invitó al cofre \"Abuela\".", dto.message)
        assertEquals("INVITACION_COFRE", dto.type)
        assertNotNull(dto.read)
        assertFalse(dto.read!!)
    }

    @Test
    fun `debe mapear metadata count como totalItems`() {
        val json = """
            {
              "data": [],
              "metadata": {
                "count": 37,
                "pageNumber": 1,
                "pageSize": 20,
                "totalPages": 2
              }
            }
        """.trimIndent()

        val response = gson.fromJson(json, NotificationsListResponse::class.java)

        assertEquals(37, response.metadata?.totalItems)
        assertEquals(1, response.metadata?.pageNumber)
        assertEquals(20, response.metadata?.pageSize)
        assertEquals(2, response.metadata?.totalPages)
    }
}
