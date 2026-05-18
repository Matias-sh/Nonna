package com.cocido.nonna.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkErrorParserTest {

    @Test
    fun `prioriza details sobre message generico`() {
        val raw = """
            {
              "code": "VA01",
              "message": "Datos de entrada no válidos.",
              "details": "Alcanzaste el límite de 2 cofre(s) para tu plan Gratis."
            }
        """.trimIndent()

        val parsed = NetworkErrorParser.parse(raw)

        assertEquals(
            "Alcanzaste el límite de 2 cofre(s) para tu plan Gratis.",
            parsed
        )
    }

    @Test
    fun `usa detail de errorDetails cuando existe`() {
        val raw = """
            {
              "message": "Bad Request Exception",
              "errorDetails": {
                "detail": "Alcanzaste el límite de 50 recuerdo(s) para tu plan Free."
              }
            }
        """.trimIndent()

        val parsed = NetworkErrorParser.parse(raw)

        assertEquals(
            "Alcanzaste el límite de 50 recuerdo(s) para tu plan Free.",
            parsed
        )
    }

    @Test
    fun `normaliza mensaje generico de bad request sin detalles`() {
        val raw = """
            {
              "message": "Datos de entrada no válidos."
            }
        """.trimIndent()

        val parsed = NetworkErrorParser.parse(raw)

        assertEquals(
            "No pudimos procesar la solicitud. Revisá los datos y, si es por límite del plan, actualizá tu suscripción.",
            parsed
        )
    }
}
