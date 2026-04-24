package com.cocido.nonna.data.repository

import com.cocido.nonna.data.remote.SuscripcionApi
import com.cocido.nonna.data.remote.dto.CambiarPlanRequestDto
import com.cocido.nonna.data.remote.dto.SuscripcionActualDto
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class SuscripcionRepository @Inject constructor(
    private val api: SuscripcionApi
) {
    suspend fun getMiSuscripcion(): ApiResult<SuscripcionActualDto> {
        return try {
            val response = api.getMiSuscripcion()
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it) }
                    ?: ApiResult.Error("Sin datos de suscripción")
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string()) ?: "Error",
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(), e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
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
                    NetworkErrorParser.parse(response.errorBody()?.string()) ?: "No se pudo cambiar el plan",
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(), e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        } catch (e: Exception) {
            ApiResult.Error("No se pudo cambiar el plan.")
        }
    }
}
