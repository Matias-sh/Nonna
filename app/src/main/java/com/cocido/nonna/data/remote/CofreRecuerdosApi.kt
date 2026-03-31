package com.cocido.nonna.data.remote

import com.cocido.nonna.data.remote.dto.CofreCreateRequest
import com.cocido.nonna.data.remote.dto.CofreDto
import com.cocido.nonna.data.remote.dto.CofreInviteRequest
import com.cocido.nonna.data.remote.dto.InvitacionDto
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

    /**
     * Invitaciones recibidas por el usuario autenticado (estado PENDIENTE).
     *
     * ⚠️ CONFIRMAR CON BACKEND: el tipo de respuesta puede ser:
     *   - List<InvitacionDto> (array directo)
     *   - objeto con campo "invitaciones"
     * Si el backend devuelve objeto, cambiar el tipo de retorno a MisInvitacionesResponse.
     */
    @GET("cofre-recuerdos/mis-invitaciones-pendientes")
    suspend fun misInvitacionesPendientes(): Response<List<InvitacionDto>>

    /**
     * Invitaciones enviadas por el usuario autenticado.
     *
     * ⚠️ CONFIRMAR CON BACKEND: mismo aviso que misInvitacionesPendientes.
     */
    @GET("cofre-recuerdos/mis-invitaciones-enviadas")
    suspend fun misInvitacionesEnviadas(): Response<List<InvitacionDto>>

    /** Acepta la invitación con el ID dado. El usuario queda asociado al cofre. */
    @POST("cofre-recuerdos/invitaciones/{id}/aceptar")
    suspend fun aceptarInvitacion(@Path("id") id: String): Response<Unit>

    /** Rechaza la invitación con el ID dado. */
    @POST("cofre-recuerdos/invitaciones/{id}/rechazar")
    suspend fun rechazarInvitacion(@Path("id") id: String): Response<Unit>
}
