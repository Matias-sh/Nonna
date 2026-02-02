package com.cocido.nonna.data.remote

import com.cocido.nonna.data.remote.dto.PagedResponse
import com.cocido.nonna.data.remote.dto.PersonaArbolCreateRequest
import com.cocido.nonna.data.remote.dto.PersonaArbolDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ArbolFamiliarApi {
    @GET("arbol-familiar/search")
    suspend fun search(
        @Query("q") query: String? = null
    ): Response<PagedResponse<PersonaArbolDto>>

    @POST("arbol-familiar")
    suspend fun create(@Body request: PersonaArbolCreateRequest): Response<PersonaArbolDto>

    @GET("arbol-familiar/{id}")
    suspend fun getById(@Path("id") id: String): Response<PersonaArbolDto>

    @PATCH("arbol-familiar/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body request: PersonaArbolCreateRequest
    ): Response<PersonaArbolDto>

    @DELETE("arbol-familiar/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}
