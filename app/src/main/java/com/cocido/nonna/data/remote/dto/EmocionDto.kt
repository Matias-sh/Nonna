package com.cocido.nonna.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EmocionDto(
    @SerializedName("id") val id: String,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("codigo") val codigo: String? = null,
    @SerializedName("emoji") val emoji: String? = null
) {
    fun displayName(): String = nombre ?: name ?: ""
}
