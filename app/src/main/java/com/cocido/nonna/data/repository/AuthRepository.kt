package com.cocido.nonna.data.repository

import com.cocido.nonna.data.local.TokenManager
import com.cocido.nonna.data.remote.AuthApi
import com.cocido.nonna.data.remote.dto.LoginRequest
import com.cocido.nonna.data.remote.dto.UserDto
import com.cocido.nonna.data.remote.dto.VerifyEmailRequest
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
    val emailVerificado: Flow<Boolean?> = tokenManager.emailVerificado

    suspend fun login(email: String, password: String): ApiResult<UserDto> {
        return try {
            val response = authApi.login(LoginRequest(email = email, password = password))
            if (response.isSuccessful) {
                val body = response.body()
                val tokenValue = body?.tokenValue
                if (!tokenValue.isNullOrBlank()) {
                    tokenManager.saveToken(tokenValue)
                    val user = body?.user ?: body?.usuario?.toUserDto() ?: authApi.getMe().body()
                    user?.id?.let { tokenManager.saveUserId(it) }
                    // Guardar estado de emailVerificado si el backend lo devuelve
                    val verificado = user?.emailVerificado
                    if (verificado != null) {
                        tokenManager.saveEmailVerificado(verificado)
                    }
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
                    val user = body?.user ?: body?.usuario?.toUserDto() ?: authApi.getMe().body()
                    user?.id?.let { tokenManager.saveUserId(it) }
                    // Guardar estado de emailVerificado
                    val verificado = user?.emailVerificado
                    if (verificado != null) {
                        tokenManager.saveEmailVerificado(verificado)
                    } else {
                        // Si el backend no devuelve el campo en signup, asumimos NO verificado
                        // porque se acaba de registrar y el backend envía el código por email.
                        // ⚠️ CONFIRMAR CON BACKEND: si signup implica email no verificado siempre.
                        tokenManager.saveEmailVerificado(false)
                    }
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
                val user = response.body()
                // Sincronizar emailVerificado con la última info del servidor
                user?.emailVerificado?.let { tokenManager.saveEmailVerificado(it) }
                user?.let { ApiResult.Success(it) }
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

    /**
     * Verifica el email del usuario con el código de 6 dígitos recibido por correo.
     * En caso de éxito actualiza el estado local a verificado.
     */
    suspend fun verifyEmail(code: String): ApiResult<Unit> {
        return try {
            val response = authApi.verifyEmail(VerifyEmailRequest(codigo = code))
            if (response.isSuccessful) {
                tokenManager.saveEmailVerificado(true)
                ApiResult.Success(Unit)
            } else {
                val msg = NetworkErrorParser.parse(response.errorBody()?.string())
                ApiResult.Error(msg ?: when (response.code()) {
                    400 -> "Código inválido. Verificá que hayas ingresado los 6 dígitos correctamente."
                    404 -> "Código no encontrado o ya utilizado."
                    410 -> "El código expiró. Solicitá uno nuevo."
                    else -> "Error al verificar el código (${response.code()})"
                }, response.code())
            }
        } catch (e: HttpException) {
            ApiResult.Error(NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(), e.code())
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    /**
     * Reenvía el código de verificación de email al usuario autenticado.
     */
    suspend fun sendVerificationEmail(): ApiResult<Unit> {
        return try {
            val response = authApi.sendVerificationEmail()
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                val msg = NetworkErrorParser.parse(response.errorBody()?.string())
                ApiResult.Error(msg ?: "No se pudo reenviar el código. Intentá más tarde.", response.code())
            }
        } catch (e: HttpException) {
            ApiResult.Error(NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(), e.code())
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun logout() {
        tokenManager.clear()
    }
}
