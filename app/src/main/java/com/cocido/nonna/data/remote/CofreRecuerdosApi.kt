package com.cocido.nonna.data.remote

import com.cocido.nonna.data.remote.dto.CofreCreateRequest
import com.cocido.nonna.data.remote.dto.CofreDto
import com.cocido.nonna.data.remote.dto.CofreInviteRequest
import com.cocido.nonna.data.remote.dto.CofreInvitationDto
import com.cocido.nonna.data.remote.dto.MisCofresResponse
import com.cocido.nonna.data.remote.dto.PagedResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface CofreRecuerdosApi {
    @GET("cofre-recuerdos/search")
    suspend fun search(
        @Query("q") query: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PagedResponse<CofreDto>>

    @GET("cofre-recuerdos/mis-cofres")
    suspend fun misCofres(): Response<MisCofresResponse>

    @POST("cofre-recuerdos")
    suspend fun create(@Body request: CofreCreateRequest): Response<CofreDto>

    @Multipart
    @POST("cofre-recuerdos/full")
    suspend fun createFull(
        @Part("nombre") nombre: RequestBody,
        @Part("parentesco") parentesco: RequestBody,
        @Part("fraseDescripcion") fraseDescripcion: RequestBody,
        @Part fotoPortada: MultipartBody.Part? = null,
        @Part("invitadosEmails") invitadosEmails: RequestBody? = null
    ): Response<CofreDto>

    @PATCH("cofre-recuerdos/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body request: CofreCreateRequest
    ): Response<CofreDto>

    @Multipart
    @PATCH("cofre-recuerdos/{id}")
    suspend fun updateFull(
        @Path("id") id: String,
        @Part("nombre") nombre: RequestBody,
        @Part("parentesco") parentesco: RequestBody,
        @Part("fraseDescripcion") fraseDescripcion: RequestBody,
        @Part fotoPortada: MultipartBody.Part? = null,
        @Part("urlPortada") urlPortada: RequestBody? = null,
        @Part("invitadosEmails") invitadosEmails: RequestBody? = null
    ): Response<CofreDto>

    @GET("cofre-recuerdos/{id}")
    suspend fun getById(@Path("id") id: String): Response<CofreDto>

    @DELETE("cofre-recuerdos/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>

    @POST("cofre-recuerdos/invitar")
    suspend fun invitar(@Body request: CofreInviteRequest): Response<Unit>

    @GET("cofre-recuerdos/mis-invitaciones-pendientes")
    suspend fun misInvitacionesPendientes(): Response<PagedResponse<CofreInvitationDto>>

    @GET("cofre-recuerdos/mis-invitaciones-enviadas")
    suspend fun misInvitacionesEnviadas(): Response<PagedResponse<CofreInvitationDto>>

    @POST("cofre-recuerdos/invitaciones/{id}/aceptar")
    suspend fun aceptarInvitacion(@Path("id") invitationId: String): Response<Unit>

    @POST("cofre-recuerdos/invitaciones/{id}/rechazar")
    suspend fun rechazarInvitacion(@Path("id") invitationId: String): Response<Unit>

    @DELETE("cofre-recuerdos/invitaciones/{id}")
    suspend fun cancelarInvitacion(@Path("id") invitationId: String): Response<Unit>

    @DELETE("cofre-recuerdos/invitaciones/cofre/{cofreId}/abandonar")
    suspend fun abandonarCofreCompartido(@Path("cofreId") cofreId: String): Response<Unit>
}
