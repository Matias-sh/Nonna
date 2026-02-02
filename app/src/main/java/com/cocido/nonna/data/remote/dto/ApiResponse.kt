package com.cocido.nonna.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Respuesta paginada genérica (si el backend devuelve content/totalElements).
 */
data class PagedResponse<T>(
    @SerializedName("content") val content: List<T>? = null,
    @SerializedName("data") val data: List<T>? = null,
    @SerializedName("items") val items: List<T>? = null,
    @SerializedName("totalElements") val totalElements: Int? = null
) {
    fun list(): List<T> = content ?: data ?: items ?: emptyList()
}
