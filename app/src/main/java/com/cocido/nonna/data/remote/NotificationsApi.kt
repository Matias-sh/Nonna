package com.cocido.nonna.data.remote

import com.cocido.nonna.data.remote.dto.NotificationDeviceTokenRequest
import com.cocido.nonna.data.remote.dto.NotificationsListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationsApi {

    @POST("notifications/device-token")
    suspend fun registerDeviceToken(
        @Body body: NotificationDeviceTokenRequest
    ): Response<Unit>

    @GET("notifications")
    suspend fun getMyNotifications(
        @Query("sortBy") sortBy: String? = null,
        @Query("pageSize") pageSize: Int? = null,
        @Query("pageNumber") pageNumber: Int? = null,
        @Query("q") query: String? = null,
        @Query("soloNoLeidas") onlyUnread: Boolean = false
    ): Response<NotificationsListResponse>

    @PATCH("notifications/{id}/read")
    suspend fun markNotificationAsRead(
        @Path("id") id: Long
    ): Response<Unit>
}

