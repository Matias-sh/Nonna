package com.cocido.nonna.data.remote

import com.cocido.nonna.data.remote.dto.PagedResponse
import com.cocido.nonna.data.remote.dto.UserDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface UsuarioApi {
    @GET("usuario/search")
    suspend fun search(@Query("q") query: String? = null): Response<PagedResponse<UserDto>>

    @GET("usuario/{id}")
    suspend fun getById(@Path("id") id: String): Response<UserDto>

    /**
     * Actualizar datos de usuario, incluyendo foto de perfil opcional.
     *
     * Backend (Swagger) espera multipart/form-data con:
     * - nombre, apellido, nombreUsuario, contrasena, email, activo
     * - fotoPerfil (archivo binario)
     * - urlFotoPerfil (string) para mantener / borrar imagen existente
     *
     * Solo enviamos los campos que queremos modificar.
     */
    @Multipart
    @PATCH("usuario/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Part("nombre") nombre: RequestBody? = null,
        @Part("apellido") apellido: RequestBody? = null,
        @Part("nombreUsuario") nombreUsuario: RequestBody? = null,
        @Part("contrasena") contrasena: RequestBody? = null,
        @Part("email") email: RequestBody? = null,
        @Part("activo") activo: RequestBody? = null,
        @Part fotoPerfil: MultipartBody.Part? = null,
        @Part("urlFotoPerfil") urlFotoPerfil: RequestBody? = null
    ): Response<UserDto>

    @DELETE("usuario/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}
