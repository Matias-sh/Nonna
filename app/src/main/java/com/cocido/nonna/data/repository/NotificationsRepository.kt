package com.cocido.nonna.data.repository

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.cocido.nonna.data.remote.NotificationsApi
import com.cocido.nonna.data.remote.dto.NotificationDeviceTokenRequest
import com.cocido.nonna.data.remote.dto.NotificationDto
import com.cocido.nonna.data.remote.dto.NotificationsListResponse
import com.google.gson.JsonParseException
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

data class NotificationUiModel(
    val id: Long,
    val title: String,
    val message: String,
    val type: String,
    val payloadType: String?,
    val paymentId: String?,
    val isRead: Boolean,
    val createdAt: String?
)

data class NotificationsPageUiModel(
    val items: List<NotificationUiModel>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int
)

class NotificationsRepository @Inject constructor(
    private val api: NotificationsApi,
    @ApplicationContext private val context: Context
) {

    suspend fun registerDeviceToken(fcmToken: String): ApiResult<Unit> {
        val cleanToken = fcmToken.trim()
        if (cleanToken.isBlank()) return ApiResult.Error("Token FCM vacío")
        return try {
            val body = NotificationDeviceTokenRequest(
                fcmToken = cleanToken,
                platform = "android",
                deviceId = deviceId(),
                deviceName = deviceName()
            )
            val response = api.registerDeviceToken(body)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string())
                        ?: "No se pudo registrar el token del dispositivo",
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

    suspend fun getMyNotifications(
        sortBy: String = "createdAt:desc",
        pageSize: Int = 20,
        pageNumber: Int = 1,
        query: String? = null,
        onlyUnread: Boolean = false
    ): ApiResult<NotificationsPageUiModel> {
        return try {
            val response = api.getMyNotifications(
                sortBy = sortBy,
                pageSize = pageSize,
                pageNumber = pageNumber,
                query = query,
                onlyUnread = onlyUnread
            )
            if (response.isSuccessful) {
                val body = response.body() ?: NotificationsListResponse()
                ApiResult.Success(
                    NotificationsPageUiModel(
                        items = body.list().map { it.toUiModel() },
                        pageNumber = body.metadata?.pageNumber ?: body.pageNumber ?: pageNumber,
                        pageSize = body.metadata?.pageSize ?: body.pageSize ?: pageSize,
                        totalItems = body.metadata?.totalItems ?: body.totalItems ?: body.list().size,
                        totalPages = body.metadata?.totalPages ?: body.totalPages ?: 1
                    )
                )
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string())
                        ?: "No se pudieron cargar las notificaciones",
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

    suspend fun markAsRead(id: Long): ApiResult<Unit> {
        return try {
            val response = api.markNotificationAsRead(id)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string())
                        ?: "No se pudo marcar la notificación como leída",
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

    private fun deviceId(): String {
        return runCatching {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: "unknown-android"
    }

    private fun deviceName(): String {
        val manufacturer = Build.MANUFACTURER?.trim().orEmpty()
        val model = Build.MODEL?.trim().orEmpty()
        return listOf(manufacturer, model).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Android device" }
    }
}

private fun NotificationDto.toUiModel(): NotificationUiModel {
    val payloadType = payloadJson?.get("type")?.trim()?.takeIf { it.isNotBlank() }
    val paymentId = payloadJson?.get("pagoId")?.trim()?.takeIf { it.isNotBlank() }
        ?: payloadJson?.get("paymentId")?.trim()?.takeIf { it.isNotBlank() }
    return NotificationUiModel(
        id = id ?: -1L,
        title = title?.takeIf { it.isNotBlank() } ?: "Notificación",
        message = message?.takeIf { it.isNotBlank() } ?: "",
        type = type?.takeIf { it.isNotBlank() } ?: "general",
        payloadType = payloadType,
        paymentId = paymentId,
        isRead = read == true,
        createdAt = createdAt
    )
}

