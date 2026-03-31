package com.cocido.nonna.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

/**
 * DTO de invitación recibida o enviada.
 *
 * ⚠️ CONFIRMAR CON BACKEND: los nombres de campo exactos pueden variar.
 * Ajustar @SerializedName según la respuesta real de:
 *   GET /cofre-recuerdos/mis-invitaciones-pendientes
 *   GET /cofre-recuerdos/mis-invitaciones-enviadas
 */
data class InvitacionDto(
    @SerializedName("id") private val idRaw: JsonElement? = null,
    @SerializedName("cofreRecuerdosId") val cofreRecuerdosId: JsonElement? = null,
    @SerializedName("nombreCofre") val nombreCofre: String? = null,
    @SerializedName("emailInvitado") val emailInvitado: String? = null,
    @SerializedName("emailInvitador") val emailInvitador: String? = null,
    @SerializedName("nombreInvitador") val nombreInvitador: String? = null,
    /** Estado: "PENDIENTE", "ACEPTADA", "RECHAZADA" */
    @SerializedName("estado") val estado: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
) {
    fun idValue(): String = when {
        idRaw == null -> ""
        idRaw.isJsonPrimitive -> {
            val p = idRaw.asJsonPrimitive
            if (p.isNumber) p.asInt.toString() else p.asString
        }
        else -> ""
    }

    fun cofreIdValue(): String = when {
        cofreRecuerdosId == null -> ""
        cofreRecuerdosId.isJsonPrimitive -> {
            val p = cofreRecuerdosId.asJsonPrimitive
            if (p.isNumber) p.asInt.toString() else p.asString
        }
        else -> ""
    }

    fun isPending(): Boolean = estado?.uppercase() == "PENDIENTE"
}

/** Modelo UI derivado de InvitacionDto para mostrarse en la pantalla. */
data class InvitacionUiModel(
    val id: String,
    val cofreId: String,
    val nombreCofre: String,
    val emailInvitador: String,
    val nombreInvitador: String
)

fun InvitacionDto.toUiModel(): InvitacionUiModel = InvitacionUiModel(
    id = idValue(),
    cofreId = cofreIdValue(),
    nombreCofre = nombreCofre ?: "Cofre sin nombre",
    emailInvitador = emailInvitador ?: "",
    nombreInvitador = nombreInvitador ?: emailInvitador ?: "Un familiar"
)

/**
 * Wrapper para la respuesta del backend. Soporta tanto array directo como objeto con campo.
 * ⚠️ Si el backend devuelve lista directa, se usa [InvitacionDto] como tipo de Retrofit.
 *    Si devuelve objeto, adaptar según la clave real.
 */
data class MisInvitacionesResponse(
    @SerializedName("invitaciones") val invitaciones: List<InvitacionDto>? = null,
    @SerializedName("data") val data: List<InvitacionDto>? = null,
    @SerializedName("content") val content: List<InvitacionDto>? = null
) {
    fun list(): List<InvitacionDto> = invitaciones ?: data ?: content ?: emptyList()
}
