package com.cocido.nonna.data.repository

import com.cocido.nonna.data.remote.EmocionesApi
import com.cocido.nonna.data.remote.dto.EmocionDto
import com.cocido.nonna.util.NetworkFailureMessageResolver
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
                val raw = response.errorBody()?.string()
                emit(ApiResult.Error(NetworkErrorParser.parse(raw) ?: "Error", response.code()))
            }
        } catch (e: HttpException) {
            val raw = e.response()?.errorBody()?.string()
            emit(ApiResult.Error(NetworkErrorParser.parseOrGeneric(raw, e.code()), e.code()))
        } catch (e: JsonParseException) {
            emit(ApiResult.Error(API_RESPONSE_PARSE_ERROR))
        } catch (e: IOException) {
            emit(ApiResult.Error(NetworkFailureMessageResolver.fromIOException(e)))
        }
    }
}
