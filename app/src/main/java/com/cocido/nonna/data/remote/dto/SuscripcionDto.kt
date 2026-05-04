package com.cocido.nonna.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SuscripcionPlanDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("codigo") val codigo: String? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("precioMensual") val precioMensual: Double? = null,
    @SerializedName("precioAnual") val precioAnual: Double? = null,
    @SerializedName("moneda") val moneda: String? = null,
    @SerializedName("maxCofres") val maxCofres: Int? = null,
    @SerializedName("maxRecuerdos") val maxRecuerdos: Int? = null,
    @SerializedName("maxMiembrosPorCofre") val maxMiembrosPorCofre: Int? = null,
    @SerializedName("maxCofresInvitado") val maxCofresInvitado: Int? = null,
    @SerializedName("maxArchivosPorRecuerdo") val maxArchivosPorRecuerdo: Int? = null,
    @SerializedName("activo") val activo: Boolean? = null
)

data class LimitesPlanDto(
    @SerializedName("maxCofres") val maxCofres: Int? = null,
    @SerializedName("maxRecuerdos") val maxRecuerdos: Int? = null,
    @SerializedName("maxMiembrosPorCofre") val maxMiembrosPorCofre: Int? = null,
    @SerializedName("maxCofresInvitado") val maxCofresInvitado: Int? = null,
    @SerializedName("maxArchivosPorRecuerdo") val maxArchivosPorRecuerdo: Int? = null
)

data class UsoPlanDto(
    @SerializedName("cofresCreados") val cofresCreados: Int? = null,
    @SerializedName("recuerdosCreados") val recuerdosCreados: Int? = null
)

data class SuscripcionActualDto(
    @SerializedName("estado") val estado: String? = null,
    @SerializedName("plan") val plan: SuscripcionPlanDto? = null,
    @SerializedName("limites") val limites: LimitesPlanDto? = null,
    @SerializedName("uso") val uso: UsoPlanDto? = null,
    @SerializedName("fechaInicio") val fechaInicio: String? = null,
    @SerializedName("fechaFin") val fechaFin: String? = null,
    @SerializedName("periodicidad") val periodicidad: String? = null
)

data class CambiarPlanRequestDto(
    @SerializedName("codigoPlan") val codigoPlan: String
)
