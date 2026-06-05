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

    @Test
    fun `resuelve codigo api a chip predefinido en formulario de edicion`() {
        val (preset, custom) = resolveRelationForEditForm("PADRE")
        assertEquals("Padre", preset)
        assertEquals("", custom)
    }

    @Test
    fun `resuelve parentesco personalizado sin chip`() {
        val (preset, custom) = resolveRelationForEditForm("Vecino querido")
        assertEquals(null, preset)
        assertEquals("Vecino querido", custom)
    }
}
