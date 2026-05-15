package com.cocido.nonna.data.mock

import org.junit.Assert.assertEquals
import org.junit.Test

class RelationMappingTest {

    @Test
    fun `mapea relacion de display conocida a valor de backend`() {
        assertEquals("PADRE", relationToApi("Padre"))
        assertEquals("TIA", relationToApi("Tía"))
    }

    @Test
    fun `acepta valor api ya normalizado`() {
        assertEquals("ABUELO", relationToApi("ABUELO"))
    }

    @Test
    fun `fallback de relacion personalizada envia OTRO`() {
        assertEquals("OTRO", relationToApi("Mascota"))
    }
}
