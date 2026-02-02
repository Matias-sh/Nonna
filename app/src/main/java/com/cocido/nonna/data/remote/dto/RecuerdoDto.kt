package com.cocido.nonna.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RecuerdoDto(
    @SerializedName("id") val id: String,
    @SerializedName("titulo") val titulo: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("tipo") val tipo: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("tipoArchivo") val tipoArchivo: String? = null,
    @SerializedName("fecha") val fecha: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("rutaArchivo") val rutaArchivo: String? = null,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("audioUrl") val audioUrl: String? = null,
    @SerializedName("duracion") val duracion: String? = null,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("emocionId") val emocionId: String? = null,
    @SerializedName("emocion") val emocion: EmocionDto? = null,
    @SerializedName("emocionPersonalizada") val emocionPersonalizada: String? = null,
    @SerializedName("cofreRecuerdosId") val cofreRecuerdosId: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
) {
    fun displayTitle(): String = titulo ?: title ?: ""
    fun displayDescription(): String? = descripcion ?: description
    fun displayDate(): String = fecha ?: date ?: ""
    fun displayDuration(): String? = duracion ?: duration
}

data class RecuerdoCreateRequest(
    @SerializedName("titulo") val titulo: String? = null,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("fecha") val fecha: String? = null,
    @SerializedName("emocionId") val emocionId: String? = null,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("audioUrl") val audioUrl: String? = null,
    @SerializedName("contenidoTexto") val contenidoTexto: String? = null
)
