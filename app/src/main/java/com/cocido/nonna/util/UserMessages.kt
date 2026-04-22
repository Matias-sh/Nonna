package com.cocido.nonna.util

import com.cocido.nonna.R

object UserMessages {
    private fun tr(resId: Int, fallback: String): String {
        return AppContextProvider.get()?.getString(resId) ?: fallback
    }

    val FIELD_REVIEW_REQUIRED: String
        get() = tr(R.string.msg_field_review_required, "Revisá los campos obligatorios para continuar.")

    val FORM_REVIEW_REQUIRED: String
        get() = tr(R.string.msg_form_review_required, "Revisá los campos marcados para continuar.")

    val INVALID_EMAIL: String
        get() = tr(R.string.msg_invalid_email, "Ingresá un email válido.")

    val INVALID_INVITE_EMAIL: String
        get() = tr(R.string.msg_invalid_invite_email, "Ingresá un email válido para invitar.")

    val DUPLICATE_EMAIL: String
        get() = tr(R.string.msg_duplicate_email, "Ese email ya está agregado.")

    val DUPLICATE_ACCOUNT: String
        get() = tr(R.string.msg_duplicate_account, "Ese email o nombre de usuario ya está en uso.")

    val INVALID_NAME_MIN_2: String
        get() = tr(R.string.msg_invalid_name_min_2, "Ingresá un nombre válido (mínimo 2 caracteres).")

    val INVALID_LASTNAME_MIN_2: String
        get() = tr(R.string.msg_invalid_lastname_min_2, "Ingresá un apellido válido (mínimo 2 letras).")

    val INVALID_TITLE_MIN_2: String
        get() = tr(R.string.msg_invalid_title_min_2, "Ingresá un título válido (mínimo 2 caracteres).")

    val INVALID_RELATION: String
        get() = tr(R.string.msg_invalid_relation, "Seleccioná o escribí un parentesco válido.")

    val INVALID_SHORT_DESCRIPTION: String
        get() = tr(R.string.msg_invalid_short_description, "Escribí una descripción breve (mínimo 3 caracteres).")

    val INVALID_DATE: String
        get() = tr(R.string.msg_invalid_date, "Ingresá una fecha válida.")

    val PASSWORD_MISMATCH: String
        get() = tr(R.string.msg_password_mismatch, "Las contraseñas no coinciden")

    val INVALID_PASSWORD_RULES: String
        get() = tr(R.string.msg_invalid_password_rules, "La contraseña no cumple los requisitos.")

    val INVALID_CREDENTIALS: String
        get() = tr(R.string.msg_invalid_credentials, "Tus credenciales no son correctas.")

    val NO_INTERNET: String
        get() = tr(R.string.msg_no_internet, "Sin conexión. Revisá tu internet.")

    val GENERIC_ERROR: String
        get() = tr(R.string.msg_generic_error, "Ocurrió un error. Intentá nuevamente.")

    val GENERIC_REQUEST_ERROR: String
        get() = tr(
            R.string.msg_generic_request_error,
            "Ocurrió un error al procesar la solicitud. Intentá nuevamente."
        )

    val INVALID_VERIFICATION_CODE: String
        get() = tr(R.string.msg_invalid_verification_code, "Ingresá un código válido de 6 dígitos.")

    val WRONG_VERIFICATION_CODE: String
        get() = tr(R.string.msg_wrong_verification_code, "El código de verificación es incorrecto.")

    val EXPIRED_VERIFICATION_CODE: String
        get() = tr(R.string.msg_expired_verification_code, "El código de verificación venció. Pedí uno nuevo.")

    val VERIFICATION_CODE_SENT: String
        get() = tr(R.string.msg_verification_code_sent, "Te enviamos un nuevo código de verificación.")

    val INVITATION_ACCEPTED: String
        get() = tr(R.string.msg_invitation_accepted, "Invitación aceptada")

    val INVITATION_REJECTED: String
        get() = tr(R.string.msg_invitation_rejected, "Invitación rechazada")

    val INVITATION_CANCELLED: String
        get() = tr(R.string.msg_invitation_cancelled, "Invitación cancelada")
}
