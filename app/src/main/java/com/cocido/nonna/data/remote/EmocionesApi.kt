package com.cocido.nonna.data.remote

import com.cocido.nonna.data.remote.dto.EmocionDto
import com.cocido.nonna.data.remote.dto.PagedResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

data class EmocionCreateRequest(
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("codigo") val codigo: String? = null
)

interface EmocionesApi {
    @GET("emociones/search")
    suspend fun search(
        @Query("q") query: String? = null
    ): Response<PagedResponse<EmocionDto>>

    @POST("emociones")
    suspend fun create(@Body body: EmocionCreateRequest): Response<EmocionDto>

    @GET("emociones/{id}")
    suspend fun getById(@Path("id") id: String): Response<EmocionDto>

    @PATCH("emociones/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body body: EmocionCreateRequest
    ): Response<EmocionDto>

    @DELETE("emociones/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}
