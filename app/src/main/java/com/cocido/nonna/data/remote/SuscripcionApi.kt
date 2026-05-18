package com.cocido.nonna.data.remote

import com.cocido.nonna.data.remote.dto.CambiarPlanRequestDto
import com.cocido.nonna.data.remote.dto.BillingStateResponseDto
import com.cocido.nonna.data.remote.dto.SuscripcionActualDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface SuscripcionApi {
    @GET("suscripciones/me")
    suspend fun getMiSuscripcion(): Response<SuscripcionActualDto>

    @GET("suscripciones/me/billing-state")
    suspend fun getBillingState(): Response<BillingStateResponseDto>

    @PATCH("suscripciones/me/plan")
    suspend fun cambiarPlan(@Body body: CambiarPlanRequestDto): Response<SuscripcionActualDto>
}
