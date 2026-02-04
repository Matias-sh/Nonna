package com.cocido.nonna.data.remote

import com.cocido.nonna.data.remote.dto.AuthResponse
import com.cocido.nonna.data.remote.dto.ChangePasswordRequest
import com.cocido.nonna.data.remote.dto.LoginRequest
import com.cocido.nonna.data.remote.dto.UserDto
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    /**
     * Registro de usuario.
     *
     * Backend actual espera multipart/form-data con:
     * - nombre
     * - apellido
     * - nombreUsuario
     * - contrasena
     * - email
     *
     * El campo `activo` es solo de respuesta (estado del usuario) y
     * ya no debe enviarse en el signup.
     */
    @Multipart
    @POST("auth/signup")
    suspend fun signup(
        @Part("nombre") nombre: RequestBody,
        @Part("apellido") apellido: RequestBody,
        @Part("nombreUsuario") nombreUsuario: RequestBody,
        @Part("contrasena") contrasena: RequestBody,
        @Part("email") email: RequestBody
    ): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<UserDto>

    @PATCH("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>
}
