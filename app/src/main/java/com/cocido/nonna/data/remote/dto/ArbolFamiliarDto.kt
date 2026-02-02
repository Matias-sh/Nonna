package com.cocido.nonna.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PersonaArbolDto(
    @SerializedName("id") val id: String,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("parentesco") val parentesco: String? = null,
    @SerializedName("relation") val relation: String? = null,
    @SerializedName("cofreRecuerdosId") val cofreRecuerdosId: String? = null,
    @SerializedName("cofreId") val cofreId: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String? = null,
    @SerializedName("birthDate") val birthDate: String? = null,
    @SerializedName("fechaFallecimiento") val fechaFallecimiento: String? = null,
    @SerializedName("deathDate") val deathDate: String? = null,
    @SerializedName("padreId") val padreId: String? = null,
    @SerializedName("madreId") val madreId: String? = null,
    @SerializedName("children") val children: List<PersonaArbolDto>? = null,
    @SerializedName("hijos") val hijos: List<PersonaArbolDto>? = null
) {
    fun displayName(): String = nombre ?: name ?: ""
    fun displayRelation(): String = parentesco ?: relation ?: ""
    fun cofreId(): String? = cofreRecuerdosId ?: cofreId
    fun childrenList(): List<PersonaArbolDto> = children ?: hijos ?: emptyList()
}

data class PersonaArbolCreateRequest(
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("parentesco") val parentesco: String? = null,
    @SerializedName("relation") val relation: String? = null,
    @SerializedName("cofreRecuerdosId") val cofreRecuerdosId: String? = null,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String? = null,
    @SerializedName("fechaFallecimiento") val fechaFallecimiento: String? = null,
    @SerializedName("padreId") val padreId: String? = null,
    @SerializedName("madreId") val madreId: String? = null
)
