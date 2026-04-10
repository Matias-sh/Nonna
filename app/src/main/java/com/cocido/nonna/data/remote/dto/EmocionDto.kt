package com.cocido.nonna.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class EmocionDto(
    @SerializedName("id") private val idRaw: JsonElement? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("codigo") val codigo: String? = null,
    @SerializedName("emoji") val emoji: String? = null
) {
    /** id como string (backend puede devolver número). */
    fun idValue(): String = idRaw.primitiveIdString()
    fun displayName(): String = nombre ?: name ?: ""
}
