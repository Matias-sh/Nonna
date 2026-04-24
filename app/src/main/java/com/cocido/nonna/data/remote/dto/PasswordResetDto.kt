package com.cocido.nonna.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RequestPasswordResetCodeDto(
    @SerializedName("email") val email: String
)

data class VerifyPasswordResetCodeDto(
    @SerializedName("codigo") val codigo: String
)

data class ResetPasswordWithCodeDto(
    @SerializedName("contrasena") val contrasena: String,
    @SerializedName("confirmarContrasena") val confirmarContrasena: String
)

data class PasswordResetVerifyResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("reset_token") val resetToken: String? = null,
    @SerializedName("expires_in") val expiresIn: String? = null
)
