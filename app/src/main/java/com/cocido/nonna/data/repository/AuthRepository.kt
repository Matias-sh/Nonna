package com.cocido.nonna.data.repository

import com.cocido.nonna.data.local.TokenManager
import com.cocido.nonna.data.remote.AuthApi
import com.cocido.nonna.data.remote.dto.LoginRequest
import com.cocido.nonna.data.remote.dto.UserDto
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {
    val token: Flow<String?> = tokenManager.token
    val isLoggedIn: Flow<Boolean> = tokenManager.token.map { !it.isNullOrBlank() }

    suspend fun login(email: String, password: String): ApiResult<UserDto> {
        return try {
            val response = authApi.login(LoginRequest(email = email, password = password))
            if (response.isSuccessful) {
                val body = response.body()
                val tokenValue = body?.tokenValue
                if (!tokenValue.isNullOrBlank()) {
                    tokenManager.saveToken(tokenValue)
                    val meUser = runCatching { authApi.getMe().body() }.getOrNull()
                    val user = meUser ?: body?.user ?: body?.usuario?.toUserDto()
                    user?.id?.let { tokenManager.saveUserId(it) }
                    ApiResult.Success(user ?: UserDto("", email, email))
                } else {
                    ApiResult.Error("No se recibió token del servidor")
                }
            } else {
                ApiResult.Error(NetworkErrorParser.parse(response.errorBody()?.string()) ?: "Error al iniciar sesión", response.code())
            }
        } catch (e: HttpException) {
            if (e.code() == 401) ApiResult.Error("Email o contraseña incorrectos")
            else ApiResult.Error(NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(), e.code())
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun signup(
        email: String,
        password: String,
        nombre: String,
        apellido: String,
        nombreUsuario: String
    ): ApiResult<UserDto> {
        return try {
            val nombreVal = nombre.ifBlank { email.substringBefore("@").replace(".", " ").trim().ifEmpty { "Usuario" } }
            val apellidoVal = apellido.ifBlank { "Usuario" }
            val usuarioVal = nombreUsuario.ifBlank {
                email.substringBefore("@").replace(".", "").replace(" ", "").lowercase().ifEmpty { "user" }
            }
            val response = authApi.signup(
                nombre = nombreVal.toRequestBody("text/plain".toMediaTypeOrNull()),
                apellido = apellidoVal.toRequestBody("text/plain".toMediaTypeOrNull()),
                nombreUsuario = usuarioVal.toRequestBody("text/plain".toMediaTypeOrNull()),
                contrasena = password.toRequestBody("text/plain".toMediaTypeOrNull()),
                email = email.toRequestBody("text/plain".toMediaTypeOrNull())
            )
            if (response.isSuccessful) {
                val body = response.body()
                val tokenValue = body?.tokenValue
                if (!tokenValue.isNullOrBlank()) {
                    tokenManager.saveToken(tokenValue)
                    val meUser = runCatching { authApi.getMe().body() }.getOrNull()
                    val user = meUser ?: body?.user ?: body?.usuario?.toUserDto()
                    user?.id?.let { tokenManager.saveUserId(it) }
                    ApiResult.Success(user ?: UserDto("", email, nombreVal))
                } else {
                    ApiResult.Error("No se recibió token del servidor")
                }
            } else {
                ApiResult.Error(NetworkErrorParser.parse(response.errorBody()?.string()) ?: "Error al registrarse", response.code())
            }
        } catch (e: HttpException) {
            ApiResult.Error(NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(), e.code())
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun getMe(): ApiResult<UserDto> {
        return try {
            val response = authApi.getMe()
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it) }
                    ?: ApiResult.Error("Usuario no encontrado")
            } else {
                if (response.code() == 401) tokenManager.clear()
                ApiResult.Error(NetworkErrorParser.parse(response.errorBody()?.string()) ?: "Error", response.code())
            }
        } catch (e: HttpException) {
            if (e.code() == 401) {
                tokenManager.clear()
                ApiResult.Error("Sesión expirada")
            } else {
                ApiResult.Error(NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(), e.code())
            }
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun logout() {
        tokenManager.clear()
    }
}
