package com.cocido.nonna.data.remote

import com.cocido.nonna.data.remote.dto.PageDto
import com.cocido.nonna.data.remote.dto.SuscripcionPlanDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PlanesApi {
    @GET("planes/search")
    suspend fun search(
        @Query("q") query: String? = null,
        @Query("pageNumber") pageNumber: Int? = null,
        @Query("pageSize") pageSize: Int? = null
    ): Response<PageDto<SuscripcionPlanDto>>
}
