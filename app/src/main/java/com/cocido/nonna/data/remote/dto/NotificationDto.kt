package com.cocido.nonna.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NotificationDeviceTokenRequest(
    @SerializedName("fcmToken") val fcmToken: String,
    @SerializedName("platform") val platform: String,
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("deviceName") val deviceName: String
)

data class NotificationDto(
    @SerializedName(value = "id", alternate = ["notificationId"])
    val id: Long? = null,
    @SerializedName(value = "titulo", alternate = ["title", "asunto"])
    val title: String? = null,
    @SerializedName(value = "mensaje", alternate = ["message", "body", "descripcion", "cuerpo"])
    val message: String? = null,
    @SerializedName(value = "tipo", alternate = ["type", "categoria"])
    val type: String? = null,
    @SerializedName(value = "payloadJson", alternate = ["payload", "data"])
    val payloadJson: Map<String, String>? = null,
    @SerializedName(value = "leida", alternate = ["read", "isRead", "leido"])
    val read: Boolean? = null,
    @SerializedName(value = "fechaCreacion", alternate = ["createdAt", "created_at", "fecha"])
    val createdAt: String? = null
)

data class NotificationsListResponse(
    @SerializedName("data") val data: List<NotificationDto>? = null,
    @SerializedName("content") val content: List<NotificationDto>? = null,
    @SerializedName("items") val items: List<NotificationDto>? = null,
    @SerializedName("metadata") val metadata: PageMetadataDto? = null,
    @SerializedName("pageNumber") val pageNumber: Int? = null,
    @SerializedName("pageSize") val pageSize: Int? = null,
    @SerializedName("totalItems") val totalItems: Int? = null,
    @SerializedName("totalPages") val totalPages: Int? = null
) {
    fun list(): List<NotificationDto> = data ?: content ?: items ?: emptyList()
}

