package com.cocido.nonna.ui.components

data class CofreInvitationUiModel(
    val id: String,
    val cofreId: String,
    val cofreName: String,
    val inviteeEmail: String,
    val inviterName: String? = null,
    val inviterEmail: String? = null,
    val cofreCoverImageUrl: String? = null,
    val cofreDescription: String? = null,
    val accepted: Boolean,
    val expired: Boolean,
    /** Firma del estado en servidor (p. ej. fechas + flags) para “no volver a mostrar” hasta que cambie. */
    val stateSignature: String
)
