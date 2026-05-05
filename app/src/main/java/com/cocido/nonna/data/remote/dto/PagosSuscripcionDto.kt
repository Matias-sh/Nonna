package com.cocido.nonna.data.remote.dto

import com.google.gson.annotations.SerializedName

enum class PeriodicidadPago(val apiValue: String) {
    Mensual("MENSUAL"),
    Anual("ANUAL")
}

data class CrearCheckoutSuscripcionRequestDto(
    @SerializedName("planId") val planId: Int,
    @SerializedName("periodicidad") val periodicidad: String,
    @SerializedName("autoRenovar") val autoRenovar: Boolean
)

data class CheckoutSuscripcionResponseDto(
    @SerializedName("pagoId") val pagoId: Int,
    @SerializedName("preferenceId") val preferenceId: String,
    @SerializedName("initPoint") val initPoint: String
)

data class CancelarAutoRenovacionRequestDto(
    @SerializedName("pagoId") val pagoId: Int
)

data class SincronizarPagoRequestDto(
    @SerializedName("pagoId") val pagoId: Int,
    @SerializedName("paymentId") val paymentId: String? = null
)

data class SincronizarPagoResponseDto(
    @SerializedName("ok") val ok: Boolean? = null,
    @SerializedName("estado") val estado: String? = null
)

data class PagoSuscripcionResponseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("periodicidad") val periodicidad: String? = null,
    @SerializedName("monto") val monto: Double? = null,
    @SerializedName("moneda") val moneda: String? = null,
    @SerializedName("estadoInterno") val estadoInterno: String? = null,
    @SerializedName("autoRenovar") val autoRenovar: Boolean? = null,
    @SerializedName("planId") val planId: Int? = null,
    @SerializedName("planCodigo") val planCodigo: String? = null,
    @SerializedName("planNombre") val planNombre: String? = null,
    @SerializedName("suscripcionId") val suscripcionId: Int? = null,
    @SerializedName("mpPreferenceId") val mpPreferenceId: String? = null,
    @SerializedName("mpPaymentId") val mpPaymentId: Int? = null,
    @SerializedName("mpStatus") val mpStatus: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)
