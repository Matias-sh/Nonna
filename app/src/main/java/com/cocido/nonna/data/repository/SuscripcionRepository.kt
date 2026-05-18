package com.cocido.nonna.data.repository

import com.cocido.nonna.data.remote.SuscripcionApi
import com.cocido.nonna.data.remote.dto.BillingStateResponseDto
import com.cocido.nonna.data.remote.dto.CambiarPlanRequestDto
import com.cocido.nonna.data.remote.dto.SuscripcionActualDto
import com.cocido.nonna.util.NetworkFailureMessageResolver
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class SuscripcionRepository @Inject constructor(
    private val api: SuscripcionApi
) {
    suspend fun getBillingState(): ApiResult<BillingStateResponseDto> {
        return try {
            val response = api.getBillingState()
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it) }
                    ?: ApiResult.Error("Sin datos de facturación")
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parseOrGeneric(response.errorBody()?.string(), response.code()),
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(
                NetworkErrorParser.parseOrGeneric(e.response()?.errorBody()?.string(), e.code()),
                e.code()
            )
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(NetworkFailureMessageResolver.fromIOException(e))
        } catch (e: Exception) {
            ApiResult.Error("No se pudo cargar el estado de facturación.")
        }
    }

    suspend fun getMiSuscripcion(): ApiResult<SuscripcionActualDto> {
        return try {
            val response = api.getMiSuscripcion()
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it) }
                    ?: ApiResult.Error("Sin datos de suscripción")
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parseOrGeneric(response.errorBody()?.string(), response.code()),
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(
                NetworkErrorParser.parseOrGeneric(e.response()?.errorBody()?.string(), e.code()),
                e.code()
            )
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(NetworkFailureMessageResolver.fromIOException(e))
        } catch (e: Exception) {
            ApiResult.Error("No se pudo cargar tu plan.")
        }
    }

    suspend fun cambiarPlan(codigoPlan: String): ApiResult<SuscripcionActualDto> {
        return try {
            val response = api.cambiarPlan(CambiarPlanRequestDto(codigoPlan = codigoPlan.trim()))
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it) }
                    ?: ApiResult.Error("Sin datos de suscripción")
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parseOrGeneric(response.errorBody()?.string(), response.code()),
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(
                NetworkErrorParser.parseOrGeneric(e.response()?.errorBody()?.string(), e.code()),
                e.code()
            )
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(NetworkFailureMessageResolver.fromIOException(e))
        } catch (e: Exception) {
            ApiResult.Error("No se pudo cambiar el plan.")
        }
    }
}
