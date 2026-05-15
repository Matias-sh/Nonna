package com.cocido.nonna.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PageDto<T>(
    @SerializedName("data") val data: List<T>? = null,
    @SerializedName("metadata") val metadata: PageMetadataDto? = null
)

data class PageMetadataDto(
    @SerializedName("pageNumber") val pageNumber: Int? = null,
    @SerializedName("pageSize") val pageSize: Int? = null,
    @SerializedName(value = "totalItems", alternate = ["count"]) val totalItems: Int? = null,
    @SerializedName("totalPages") val totalPages: Int? = null
)
