package com.cocido.nonna.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class RecuerdoDto(
    @SerializedName("id") private val idRaw: JsonElement? = null,
    @SerializedName("titulo") val titulo: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("contenidoTexto") val contenidoTexto: String? = null,
    @SerializedName("contenido") val contenido: String? = null,
    @SerializedName("texto") val texto: String? = null,
    @SerializedName("content") val content: String? = null,
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
    /** id como string (backend puede devolver número). */
    fun idValue(): String = idRaw.primitiveIdString()
    fun displayTitle(): String = titulo ?: title ?: ""
    fun displayDescription(): String? = firstNotBlank(
        descripcion,
        description,
        contenidoTexto,
        contenido,
        texto,
        content
    )
    fun displayDate(): String = fecha ?: date ?: ""
    fun displayDuration(): String? = duracion ?: duration

    private fun firstNotBlank(vararg values: String?): String? {
        return values.firstOrNull { !it.isNullOrBlank() }?.trim()
    }
}

data class RecuerdoCreateRequest(
    @SerializedName("titulo") val titulo: String? = null,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("fecha") val fecha: String? = null,
    @SerializedName("emocionId") val emocionId: String? = null,
    @SerializedName("emocionPersonalizada") val emocionPersonalizada: String? = null,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("audioUrl") val audioUrl: String? = null,
    @SerializedName("contenidoTexto") val contenidoTexto: String? = null
)
