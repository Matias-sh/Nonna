package com.cocido.nonna.data.remote

import com.cocido.nonna.data.remote.dto.PagedResponse
import com.cocido.nonna.data.remote.dto.RecuerdoDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface RecuerdosApi {
    @GET("recuerdos/search")
    suspend fun search(
        @Query("q") query: String? = null,
        @Query("cofreRecuerdosId") cofreRecuerdosId: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PagedResponse<RecuerdoDto>>

    /** multipart: file principal, opcional portadaAudio (si file es audio), imagenesGaleria (si file es imagen). */
    @Multipart
    @POST("recuerdos/cofre/{cofreRecuerdosId}")
    suspend fun create(
        @Path("cofreRecuerdosId") cofreRecuerdosId: String,
        @Part file: MultipartBody.Part,
        @Part("titulo") titulo: RequestBody,
        @Part("descripcion") descripcion: RequestBody? = null,
        @Part("fecha") fecha: RequestBody? = null,
        @Part("emocionId") emocionId: RequestBody? = null,
        @Part("emocionPersonalizada") emocionPersonalizada: RequestBody? = null,
        @Part portadaAudio: MultipartBody.Part? = null,
        @Part imagenesGaleria: List<MultipartBody.Part>? = null
    ): Response<RecuerdoDto>

    @Multipart
    @PATCH("recuerdos/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Part file: MultipartBody.Part? = null,
        @Part("urlArchivo") urlArchivo: RequestBody? = null,
        @Part portadaAudio: MultipartBody.Part? = null,
        @Part("urlPortadaAudio") urlPortadaAudio: RequestBody? = null,
        @Part imagenesGaleria: List<MultipartBody.Part>? = null,
        @Part("limpiarImagenesGaleria") limpiarImagenesGaleria: RequestBody? = null,
        @Part("titulo") titulo: RequestBody? = null,
        @Part("descripcion") descripcion: RequestBody? = null,
        @Part("fecha") fecha: RequestBody? = null,
        @Part("emocionId") emocionId: RequestBody? = null,
        @Part("emocionPersonalizada") emocionPersonalizada: RequestBody? = null
    ): Response<RecuerdoDto>

    @GET("recuerdos/{id}")
    suspend fun getById(@Path("id") id: String): Response<RecuerdoDto>

    @DELETE("recuerdos/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}
