package com.cocido.nonna.data.repository

import com.cocido.nonna.data.remote.PagosSuscripcionApi
import com.cocido.nonna.data.remote.dto.CheckoutSuscripcionResponseDto
import com.cocido.nonna.data.remote.dto.PagoSuscripcionResponseDto
import com.cocido.nonna.data.remote.dto.PeriodicidadPago
import com.cocido.nonna.data.remote.dto.CancelarAutoRenovacionRequestDto
import com.cocido.nonna.data.remote.dto.CrearCheckoutSuscripcionRequestDto
import com.cocido.nonna.data.remote.dto.SincronizarPagoRequestDto
import com.cocido.nonna.data.remote.dto.SincronizarPagoResponseDto
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class PagosSuscripcionRepository @Inject constructor(
    private val api: PagosSuscripcionApi
) {
    suspend fun crearCheckout(
        planId: Int,
        periodicidad: PeriodicidadPago,
        autoRenovar: Boolean
    ): ApiResult<CheckoutSuscripcionResponseDto> {
        return try {
            val response = api.crearCheckout(
                CrearCheckoutSuscripcionRequestDto(
                    planId = planId,
                    periodicidad = periodicidad.apiValue,
                    autoRenovar = autoRenovar
                )
            )
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it) }
                    ?: ApiResult.Error("No se pudo iniciar el checkout.")
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string()) ?: "No se pudo iniciar el checkout.",
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(), e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun cancelarAutoRenovacion(pagoId: Int): ApiResult<Unit> {
        return try {
            val response = api.cancelarAutoRenovacion(CancelarAutoRenovacionRequestDto(pagoId))
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string()) ?: "No se pudo cancelar la auto-renovación.",
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(), e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun reactivarAutoRenovacion(pagoId: Int): ApiResult<Unit> {
        return try {
            val response = api.reactivarAutoRenovacion(CancelarAutoRenovacionRequestDto(pagoId))
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string())
                        ?: "No se pudo reactivar la auto-renovación.",
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(), e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun cancelarPagoPendiente(pagoId: Int): ApiResult<Unit> {
        return try {
            val response = api.cancelarPagoPendiente(pagoId.toString())
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string()) ?: "No se pudo cancelar el pago pendiente.",
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(), e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun misPagos(): ApiResult<List<PagoSuscripcionResponseDto>> {
        return try {
            val response = api.misPagos()
            if (response.isSuccessful) {
                ApiResult.Success(response.body().orEmpty())
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string()) ?: "No se pudo cargar el historial de pagos.",
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(), e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun pagoPendiente(): ApiResult<PagoSuscripcionResponseDto?> {
        return try {
            val response = api.pagoPendiente()
            if (response.isSuccessful) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string()) ?: "No se pudo consultar el pago pendiente.",
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(), e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun sincronizarPago(pagoId: Int, paymentId: String? = null): ApiResult<SincronizarPagoResponseDto> {
        return try {
            val response = api.sincronizarPago(
                SincronizarPagoRequestDto(
                    pagoId = pagoId,
                    paymentId = paymentId?.trim()?.takeIf { it.isNotBlank() }
                )
            )
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it) }
                    ?: ApiResult.Error("No se pudo sincronizar el pago.")
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string()) ?: "No se pudo sincronizar el pago.",
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(), e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }
}
