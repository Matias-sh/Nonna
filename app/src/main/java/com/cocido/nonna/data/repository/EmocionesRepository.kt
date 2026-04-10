package com.cocido.nonna.data.repository

import com.cocido.nonna.data.remote.EmocionesApi
import com.cocido.nonna.data.remote.dto.EmocionDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class EmocionesRepository @Inject constructor(
    private val api: EmocionesApi
) {
    fun search(query: String? = null): Flow<ApiResult<List<EmocionDto>>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = api.search(query)
            if (response.isSuccessful) {
                val list = response.body()?.list() ?: emptyList()
                emit(ApiResult.Success(list))
            } else {
                emit(ApiResult.Error(response.errorBody()?.string() ?: "Error", response.code()))
            }
        } catch (e: HttpException) {
            emit(ApiResult.Error(e.response()?.errorBody()?.string() ?: e.message(), e.code()))
        } catch (e: JsonParseException) {
            emit(ApiResult.Error(API_RESPONSE_PARSE_ERROR))
        } catch (e: IOException) {
            emit(ApiResult.Error("Sin conexión. Revisá tu internet."))
        }
    }
}
