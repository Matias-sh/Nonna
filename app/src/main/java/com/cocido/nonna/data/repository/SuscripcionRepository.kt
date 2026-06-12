package com.cocido.nonna.data.repository

import android.os.SystemClock
import com.cocido.nonna.data.remote.SuscripcionApi
import com.cocido.nonna.data.remote.dto.BillingStateResponseDto
import com.cocido.nonna.data.remote.dto.CambiarPlanRequestDto
import com.cocido.nonna.data.remote.dto.SuscripcionActualDto
import com.cocido.nonna.util.NetworkFailureMessageResolver
import com.google.gson.JsonParseException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuscripcionRepository @Inject constructor(
    private val api: SuscripcionApi
) {
    private val cacheMutex = Mutex()
    private var cachedSuscripcion: SuscripcionActualDto? = null
    private var cachedAtMs: Long = 0L
    private val cacheTtlMs: Long = 60_000L

    suspend fun getMiSuscripcion(forceRefresh: Boolean = false): ApiResult<SuscripcionActualDto> {
        if (!forceRefresh) {
            cacheMutex.withLock {
                val cached = cachedSuscripcion
                if (cached != null && (SystemClock.elapsedRealtime() - cachedAtMs) <= cacheTtlMs) {
                    return ApiResult.Success(cached)
                }
            }
        }
        return when (val result = fetchMiSuscripcionFromNetwork()) {
            is ApiResult.Success -> {
                cacheMutex.withLock {
                    cachedSuscripcion = result.data
                    cachedAtMs = SystemClock.elapsedRealtime()
                }
                result
            }
            else -> result
        }
    }

    fun invalidateCache() {
        cachedSuscripcion = null
        cachedAtMs = 0L
    }

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

    suspend fun cambiarPlan(codigoPlan: String): ApiResult<SuscripcionActualDto> {
        return try {
            val response = api.cambiarPlan(CambiarPlanRequestDto(codigoPlan = codigoPlan.trim()))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    cacheMutex.withLock {
                        cachedSuscripcion = body
                        cachedAtMs = SystemClock.elapsedRealtime()
                    }
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error("Sin datos de suscripción")
                }
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

    private suspend fun fetchMiSuscripcionFromNetwork(): ApiResult<SuscripcionActualDto> {
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
}
