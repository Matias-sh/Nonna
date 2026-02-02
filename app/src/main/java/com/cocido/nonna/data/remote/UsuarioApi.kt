package com.cocido.nonna.data.remote

import com.cocido.nonna.data.remote.dto.PagedResponse
import com.cocido.nonna.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

data class UsuarioUpdateRequest(
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null
)

interface UsuarioApi {
    @GET("usuario/search")
    suspend fun search(@Query("q") query: String? = null): Response<PagedResponse<UserDto>>

    @GET("usuario/{id}")
    suspend fun getById(@Path("id") id: String): Response<UserDto>

    @PATCH("usuario/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body body: UsuarioUpdateRequest
    ): Response<UserDto>

    @DELETE("usuario/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}
