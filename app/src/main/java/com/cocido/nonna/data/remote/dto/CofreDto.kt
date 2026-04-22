package com.cocido.nonna.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class CofreDto(
    @SerializedName("id") private val idRaw: JsonElement? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("parentesco") val parentesco: String? = null,
    @SerializedName("relation") val relation: String? = null,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("fraseDescripcion") val fraseDescripcion: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("imagenUrl") val imagenUrl: String? = null,
    @SerializedName("imagenPortada") val imagenPortada: String? = null,
    @SerializedName("coverImageUrl") val coverImageUrl: String? = null,
    @SerializedName("urlPortada") val urlPortada: String? = null,
    @SerializedName("photoCount") val photoCount: Int? = null,
    @SerializedName("audioCount") val audioCount: Int? = null,
    @SerializedName("textCount") val textCount: Int? = null,
    @SerializedName("memberCount") val memberCount: Int? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("updated_at") val updated_at: String? = null,
    @SerializedName("isOwner") val isOwner: Boolean? = null,
    @SerializedName("esPropietario") val esPropietario: Boolean? = null,
    @SerializedName("usuario") val usuario: UsuarioDto? = null,
    @SerializedName("invitadosEmails") val invitadosEmails: List<InvitadoCofreDto>? = null,
    @SerializedName("invitados") val invitados: List<InvitadoCofreDto>? = null
) {
    fun idValue(): String = idRaw.primitiveIdString()
    fun displayName(): String = nombre ?: name ?: ""
    fun displayRelation(): String = parentesco ?: relation ?: ""
    fun coverUrl(): String? = imagenPortada ?: imagenUrl ?: coverImageUrl ?: urlPortada
    fun isOwnerValue(): Boolean = isOwner ?: esPropietario ?: true
    fun invitedList(): List<InvitadoCofreDto> = invitadosEmails ?: invitados ?: emptyList()
}

data class InvitadoCofreDto(
    @SerializedName("id") private val idRaw: JsonElement? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("invitacionAceptada") val invitacionAceptada: Boolean? = null,
    @SerializedName("persona") val persona: PersonaDto? = null
) {
    fun idValue(): String = idRaw.primitiveIdString()
}

data class CofreInvitationDto(
    @SerializedName("id") private val idRaw: JsonElement? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("invitacionAceptada") val invitacionAceptada: Boolean? = null,
    @SerializedName("persona") val persona: PersonaDto? = null,
    @SerializedName("cofreRecuerdos") val cofreRecuerdos: CofreDto? = null,
    @SerializedName("cofre") val cofre: CofreDto? = null,
    @SerializedName("expirada") val expirada: Boolean? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    fun idValue(): String = idRaw.primitiveIdString()
    fun cofreDisplay(): CofreDto? = cofreRecuerdos ?: cofre
}

data class CofreCreateRequest(
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("parentesco") val parentesco: String? = null,
    @SerializedName("relation") val relation: String? = null,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("fraseDescripcion") val fraseDescripcion: String? = null,
    @SerializedName("imagenUrl") val imagenUrl: String? = null
)

/** Cuerpo para POST cofre-recuerdos/invitar: cofreRecuerdoId (number) y emailsUsuariosInvitados (array). */
data class CofreInviteRequest(
    @SerializedName("cofreRecuerdoId") val cofreRecuerdoId: Int,
    @SerializedName("emailsUsuariosInvitados") val emailsUsuariosInvitados: List<String>
)

/** Respuesta de GET cofre-recuerdos/mis-cofres: propios + aceptados como invitado (OpenAPI: cofresInvitado). */
data class MisCofresResponse(
    @SerializedName("usuario") val usuario: UsuarioDto? = null,
    @SerializedName("cofres") val cofres: List<CofreDto>? = null,
    @SerializedName("cofresInvitado") val cofresInvitado: List<CofreDto>? = null
)
