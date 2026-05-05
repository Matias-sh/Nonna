package com.cocido.nonna.data.repository

import com.cocido.nonna.data.remote.PlanesApi
import com.cocido.nonna.data.remote.dto.SuscripcionPlanDto
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class PlanesRepository @Inject constructor(
    private val api: PlanesApi
) {
    suspend fun planesActivos(): ApiResult<List<SuscripcionPlanDto>> {
        return try {
            val response = api.search(pageNumber = 1, pageSize = 20)
            if (response.isSuccessful) {
                val planes = response.body()?.data.orEmpty()
                    .filter { it.activo != false }
                    .sortedBy { it.precioMensual ?: Double.MAX_VALUE }
                ApiResult.Success(planes)
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string()) ?: "No se pudieron cargar los planes.",
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
