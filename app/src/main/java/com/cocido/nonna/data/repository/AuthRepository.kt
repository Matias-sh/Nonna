package com.cocido.nonna.data.repository

import com.cocido.nonna.data.local.TokenManager
import com.cocido.nonna.data.remote.AuthApi
import com.cocido.nonna.data.remote.dto.LoginRequest
import com.cocido.nonna.data.remote.dto.RefreshTokenRequest
import com.cocido.nonna.data.remote.dto.RequestPasswordResetCodeDto
import com.cocido.nonna.data.remote.dto.ResetPasswordWithCodeDto
import com.cocido.nonna.data.remote.dto.UserDto
import com.cocido.nonna.data.remote.dto.VerifyEmailRequest
import com.cocido.nonna.data.remote.dto.VerifyPasswordResetCodeDto
import com.cocido.nonna.util.UserMessages
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.flow.map
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {
    val token: Flow<String?> = tokenManager.token
    val refreshToken: Flow<String?> = tokenManager.refreshToken
    val isLoggedIn: Flow<Boolean> = tokenManager.token.map { !it.isNullOrBlank() }

    suspend fun login(email: String, password: String): ApiResult<UserDto> {
        return try {
            val response = authApi.login(LoginRequest(email = email, password = password))
            if (response.isSuccessful) {
                val body = response.body()
                val tokenValue = body?.tokenValue
                if (!tokenValue.isNullOrBlank()) {
                    tokenManager.saveToken(tokenValue)
                    body?.refreshTokenValue?.takeIf { it.isNotBlank() }?.let { tokenManager.saveRefreshToken(it) }
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
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        } catch (e: Exception) {
            val detail = e.localizedMessage?.takeIf { it.isNotBlank() }
            ApiResult.Error(
                detail?.let { "No se pudo iniciar sesión. $it" }
                    ?: "No se pudo iniciar sesión. Probá de nuevo en unos minutos."
            )
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
                    body?.refreshTokenValue?.takeIf { it.isNotBlank() }?.let { tokenManager.saveRefreshToken(it) }
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
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        } catch (e: Exception) {
            ApiResult.Error("No se pudo completar el registro. Probá de nuevo en unos minutos.")
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
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        } catch (e: Exception) {
            ApiResult.Error("No se pudo cargar tu perfil. Probá de nuevo en unos minutos.")
        }
    }

    suspend fun logout() {
        tokenManager.clear()
    }

    suspend fun sendVerificationEmail(): ApiResult<String> {
        return try {
            val response = authApi.sendVerificationEmail()
            if (response.isSuccessful) {
                val message = response.body()?.message ?: UserMessages.GENERIC_REQUEST_ERROR
                ApiResult.Success(message)
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string())
                        ?: UserMessages.GENERIC_REQUEST_ERROR,
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(
                NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: UserMessages.GENERIC_REQUEST_ERROR,
                e.code()
            )
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(UserMessages.NO_INTERNET)
        } catch (e: Exception) {
            ApiResult.Error(UserMessages.GENERIC_REQUEST_ERROR)
        }
    }

    suspend fun verifyEmail(code: String): ApiResult<UserDto> {
        return try {
            val response = authApi.verifyEmail(VerifyEmailRequest(codigo = code.trim()))
            if (response.isSuccessful) {
                val body = response.body()
                val user = body?.userDtoOrNull()
                if (user != null) {
                    ApiResult.Success(user)
                } else {
                    when (val meResult = getMe()) {
                        is ApiResult.Success -> ApiResult.Success(meResult.data)
                        is ApiResult.Error -> meResult
                        ApiResult.Loading -> ApiResult.Error(UserMessages.GENERIC_REQUEST_ERROR)
                    }
                }
            } else {
                val rawError = response.errorBody()?.string()
                mapVerifyEmailError(response.code(), rawError)?.let { mapped ->
                    return ApiResult.Error(mapped, response.code())
                }
                ApiResult.Error(
                    NetworkErrorParser.parse(rawError)
                        ?: UserMessages.GENERIC_REQUEST_ERROR,
                    response.code()
                )
            }
        } catch (e: HttpException) {
            val rawError = e.response()?.errorBody()?.string()
            mapVerifyEmailError(e.code(), rawError)?.let { mapped ->
                return ApiResult.Error(mapped, e.code())
            }
            ApiResult.Error(
                NetworkErrorParser.parse(rawError) ?: UserMessages.GENERIC_REQUEST_ERROR,
                e.code()
            )
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(UserMessages.NO_INTERNET)
        } catch (e: Exception) {
            ApiResult.Error(UserMessages.GENERIC_REQUEST_ERROR)
        }
    }

    private fun mapVerifyEmailError(code: Int, rawError: String?): String? {
        if (code != 400 && code != 401 && code != 422) return null
        val lower = rawError?.lowercase().orEmpty()
        return when {
            "expir" in lower || "expired" in lower || "venc" in lower -> UserMessages.EXPIRED_VERIFICATION_CODE
            "invalido" in lower || "inválido" in lower || "incorrect" in lower || "invalid" in lower ->
                UserMessages.WRONG_VERIFICATION_CODE
            else -> UserMessages.WRONG_VERIFICATION_CODE
        }
    }

    suspend fun refreshSession(): ApiResult<UserDto> {
        return try {
            val refreshToken = tokenManager.refreshToken.firstOrNull()?.takeIf { it.isNotBlank() }
                ?: return ApiResult.Error(UserMessages.INVALID_CREDENTIALS)
            val response = authApi.refresh(RefreshTokenRequest(refreshToken))
            if (!response.isSuccessful) {
                if (response.code() == 401) tokenManager.clear()
                return ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string()) ?: UserMessages.GENERIC_REQUEST_ERROR,
                    response.code()
                )
            }
            val body = response.body()
            val newAccess = body?.tokenValue
            if (!newAccess.isNullOrBlank()) {
                tokenManager.saveToken(newAccess)
            }
            body?.refreshTokenValue?.takeIf { it.isNotBlank() }?.let { tokenManager.saveRefreshToken(it) }
            val user = body?.user ?: body?.usuario?.toUserDto() ?: run {
                val me = getMe()
                if (me is ApiResult.Success) me.data else null
            }
            if (user != null) {
                user.id.takeIf { it.isNotBlank() }?.let { tokenManager.saveUserId(it) }
                ApiResult.Success(user)
            } else {
                ApiResult.Error(UserMessages.GENERIC_REQUEST_ERROR)
            }
        } catch (e: HttpException) {
            if (e.code() == 401) tokenManager.clear()
            ApiResult.Error(
                NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: UserMessages.GENERIC_REQUEST_ERROR,
                e.code()
            )
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(UserMessages.NO_INTERNET)
        } catch (e: Exception) {
            ApiResult.Error(UserMessages.GENERIC_REQUEST_ERROR)
        }
    }

    suspend fun requestPasswordResetCode(email: String): ApiResult<String> {
        return try {
            val response = authApi.requestPasswordResetCode(RequestPasswordResetCodeDto(email.trim()))
            if (response.isSuccessful) {
                ApiResult.Success(response.body()?.message ?: "Código enviado")
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string())
                        ?: "No se pudo enviar el código",
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(
                NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(),
                e.code()
            )
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(UserMessages.NO_INTERNET)
        } catch (e: Exception) {
            ApiResult.Error(UserMessages.GENERIC_REQUEST_ERROR)
        }
    }

    suspend fun verifyPasswordResetCode(code: String): ApiResult<String> {
        return try {
            val response = authApi.verifyPasswordResetCode(VerifyPasswordResetCodeDto(codigo = code.trim()))
            if (response.isSuccessful) {
                val token = response.body()?.resetToken?.trim().orEmpty()
                if (token.isNotBlank()) {
                    ApiResult.Success(token)
                } else {
                    ApiResult.Error("No se recibió el token de recuperación")
                }
            } else {
                ApiResult.Error(
                    messageForPasswordResetVerifyFailure(
                        response.errorBody()?.string(),
                        response.code()
                    ),
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(
                messageForPasswordResetVerifyFailure(
                    e.response()?.errorBody()?.string(),
                    e.code()
                ),
                e.code()
            )
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(UserMessages.NO_INTERNET)
        } catch (e: Exception) {
            ApiResult.Error(UserMessages.GENERIC_REQUEST_ERROR)
        }
    }

    suspend fun confirmPasswordReset(resetToken: String, newPassword: String, confirmPassword: String): ApiResult<String> {
        return try {
            val bearer = "Bearer ${resetToken.trim()}"
            val response = authApi.confirmPasswordReset(
                authorization = bearer,
                body = ResetPasswordWithCodeDto(
                    contrasena = newPassword,
                    confirmarContrasena = confirmPassword
                )
            )
            if (response.isSuccessful) {
                ApiResult.Success(response.body()?.message ?: "Contraseña actualizada")
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string())
                        ?: "No se pudo restablecer la contraseña",
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(
                NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(),
                e.code()
            )
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(UserMessages.NO_INTERNET)
        } catch (e: Exception) {
            ApiResult.Error(UserMessages.GENERIC_REQUEST_ERROR)
        }
    }

    /**
     * El verify de recuperación suele devolver 4xx con cuerpo poco claro; evitamos mostrar error genérico
     * cuando en la práctica el código ingresado no coincide o expiró.
     */
    private fun messageForPasswordResetVerifyFailure(errorBody: String?, httpCode: Int): String {
        val parsed = NetworkErrorParser.parse(errorBody)
        if (parsed == null) {
            return if (httpCode in 400..499 && httpCode != 429) {
                UserMessages.WRONG_VERIFICATION_CODE
            } else {
                "Código incorrecto o expirado"
            }
        }
        val clientError = httpCode in 400..499 && httpCode != 429
        val isGeneric = parsed == UserMessages.GENERIC_REQUEST_ERROR ||
            parsed == UserMessages.GENERIC_ERROR ||
            parsed.contains("Revisá los datos ingresados", ignoreCase = true)
        return if (clientError && isGeneric) UserMessages.WRONG_VERIFICATION_CODE else parsed
    }
}
