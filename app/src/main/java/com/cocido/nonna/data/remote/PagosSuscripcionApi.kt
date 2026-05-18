package com.cocido.nonna.data.remote

import com.cocido.nonna.data.remote.dto.CancelarAutoRenovacionRequestDto
import com.cocido.nonna.data.remote.dto.AutoRenewMutationResponseDto
import com.cocido.nonna.data.remote.dto.CheckoutSuscripcionResponseDto
import com.cocido.nonna.data.remote.dto.CrearCheckoutSuscripcionRequestDto
import com.cocido.nonna.data.remote.dto.PagoSuscripcionResponseDto
import com.cocido.nonna.data.remote.dto.SincronizarPagoRequestDto
import com.cocido.nonna.data.remote.dto.SincronizarPagoResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PagosSuscripcionApi {
    @POST("pagos-suscripcion/checkout")
    suspend fun crearCheckout(
        @Body body: CrearCheckoutSuscripcionRequestDto
    ): Response<CheckoutSuscripcionResponseDto>

    @POST("pagos-suscripcion/auto-renovacion/cancelar")
    suspend fun cancelarAutoRenovacion(
        @Body body: CancelarAutoRenovacionRequestDto
    ): Response<AutoRenewMutationResponseDto>

    /** Habilita de nuevo la renovación automática (mismo body que cancelar: `pagoId`). */
    @POST("pagos-suscripcion/auto-renovacion/reactivar")
    suspend fun reactivarAutoRenovacion(
        @Body body: CancelarAutoRenovacionRequestDto
    ): Response<AutoRenewMutationResponseDto>

    @POST("pagos-suscripcion/{id}/cancelar")
    suspend fun cancelarPagoPendiente(
        @Path("id") id: String
    ): Response<Unit>

    @POST("pagos-suscripcion/sincronizar")
    suspend fun sincronizarPago(
        @Body body: SincronizarPagoRequestDto
    ): Response<SincronizarPagoResponseDto>

    @GET("pagos-suscripcion/mis-pagos")
    suspend fun misPagos(): Response<List<PagoSuscripcionResponseDto>>

    @GET("pagos-suscripcion/pendiente")
    suspend fun pagoPendiente(): Response<PagoSuscripcionResponseDto?>

    @GET("pagos-suscripcion/{id}")
    suspend fun pagoDetalle(
        @Path("id") id: String
    ): Response<PagoSuscripcionResponseDto>
}
