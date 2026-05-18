package com.cocido.nonna.data.repository

import com.cocido.nonna.data.remote.PagosSuscripcionApi
import com.cocido.nonna.data.remote.dto.AutoRenewMutationResponseDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class PagosSuscripcionRepositoryTest {

    private val api = mockk<PagosSuscripcionApi>()
    private val repository = PagosSuscripcionRepository(api)

    @Test
    fun cancelarAutoRenovacion_devuelveSuccessConMensajeBackend() = runTest {
        val dto = AutoRenewMutationResponseDto(ok = true, message = "Auto-renovación cancelada.")
        coEvery { api.cancelarAutoRenovacion(any()) } returns Response.success(dto)

        val result = repository.cancelarAutoRenovacion(10)

        assertTrue(result is ApiResult.Success)
        assertEquals("Auto-renovación cancelada.", (result as ApiResult.Success).data.message)
    }

    @Test
    fun reactivarAutoRenovacion_sinBodyRetornaMensajeSeguro() = runTest {
        coEvery { api.reactivarAutoRenovacion(any()) } returns Response.success(null)

        val result = repository.reactivarAutoRenovacion(9)

        assertTrue(result is ApiResult.Error)
        assertEquals(
            "No se pudo confirmar el estado de la auto-renovación.",
            (result as ApiResult.Error).message
        )
    }

    @Test
    fun crearCheckout_errorHttpUsaMensajeNormalizado() = runTest {
        val errorBody = """{"details":"Plan no disponible"}"""
            .toResponseBody("application/json".toMediaType())
        coEvery { api.crearCheckout(any()) } returns Response.error(400, errorBody)

        val result = repository.crearCheckout(
            planId = 1,
            periodicidad = com.cocido.nonna.data.remote.dto.PeriodicidadPago.Mensual,
            autoRenovar = true
        )

        assertTrue(result is ApiResult.Error)
        assertEquals("Plan no disponible", (result as ApiResult.Error).message)
    }
}
