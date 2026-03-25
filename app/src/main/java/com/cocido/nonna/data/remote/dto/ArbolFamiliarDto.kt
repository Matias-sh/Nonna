package com.cocido.nonna.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para una persona del árbol familiar, compatible con las distintas formas
 * en que el backend puede nombrar los campos.
 */
data class PersonaArbolDto(
    @SerializedName("id") val id: String,
    @SerializedName("nombreCompleto") val nombreCompleto: String? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("parentesco") val parentesco: String? = null,
    @SerializedName("parentescoConmigo") val parentescoConmigo: String? = null,
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
    @SerializedName("hijos") val hijos: List<PersonaArbolDto>? = null,
    @SerializedName("conexion") val conexion: PersonaConexionDto? = null,
    @SerializedName("cofre") val cofre: CofreEnArbolDto? = null
) {
    fun displayName(): String = nombreCompleto ?: nombre ?: name ?: ""
    fun displayRelation(): String = parentescoConmigo ?: parentesco ?: relation ?: ""
    fun cofreIdOrNull(): String? = cofreRecuerdosId ?: cofreId ?: cofre?.id?.toString()
    fun childrenList(): List<PersonaArbolDto> = children ?: hijos ?: emptyList()
}

/**
 * Respuesta de GET /arbol-familiar/mi-arbol
 */
data class ArbolFamiliarResponseDto(
    @SerializedName("usuario") val usuario: UserInArbolDto,
    @SerializedName("personas") val personas: List<PersonaArbolDto>,
    @SerializedName("totalPersonas") val totalPersonas: Int
)

data class UserInArbolDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombreUsuario") val nombreUsuario: String?,
    @SerializedName("email") val email: String,
    @SerializedName("fotoPerfil") val fotoPerfil: String?,
    @SerializedName("persona") val persona: PersonaBasicaDto?
)

data class PersonaBasicaDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("apellido") val apellido: String?
)

data class PersonaConexionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombreCompleto") val nombreCompleto: String?,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String?,
    @SerializedName("fechaFallecimiento") val fechaFallecimiento: String?
)

data class CofreEnArbolDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("parentesco") val parentesco: String?,
    @SerializedName("fraseDescripcion") val fraseDescripcion: String?,
    @SerializedName("imagenPortada") val imagenPortada: String?
)

/**
 * Request para crear/actualizar personas del árbol familiar.
 */
data class PersonaArbolCreateRequest(
    @SerializedName("nombreCompleto") val nombreCompleto: String,
    @SerializedName("unionPadresId") val unionPadresId: Int? = null,
    @SerializedName("parentescoConmigo") val parentescoConmigo: String? = null,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String? = null,
    @SerializedName("fechaFallecimiento") val fechaFallecimiento: String? = null,
    @SerializedName("notasPersonales") val notasPersonales: String? = null,
    @SerializedName("crearCofre") val crearCofre: Boolean = false,
    @SerializedName("crearUnionRaiz") val crearUnionRaiz: Boolean? = null
)

data class UnionArbolDto(
    @SerializedName("id") val id: Int,
    @SerializedName("parent1") val parent1: PersonaArbolDto? = null,
    @SerializedName("parent2") val parent2: PersonaArbolDto? = null,
    @SerializedName("hijos") val hijos: List<PersonaArbolDto>? = null
)

data class UnionArbolCreateRequest(
    @SerializedName("parent1Id") val parent1Id: Int,
    @SerializedName("parent2Id") val parent2Id: Int? = null
)

data class UnionesArbolResponseDto(
    @SerializedName("personasRaiz") val personasRaiz: List<PersonaArbolDto> = emptyList(),
    @SerializedName("uniones") val uniones: List<UnionArbolDto> = emptyList()
)

data class CrearPersonaResponseDto(
    @SerializedName("persona") val persona: PersonaArbolDto? = null,
    @SerializedName("unionRaizCreada") val unionRaizCreada: UnionArbolDto? = null,
    // Compat por si el backend devuelve directamente la persona
    @SerializedName("id") val id: String? = null,
    @SerializedName("nombreCompleto") val nombreCompleto: String? = null,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String? = null,
    @SerializedName("fechaFallecimiento") val fechaFallecimiento: String? = null,
    @SerializedName("notasPersonales") val notasPersonales: String? = null,
    @SerializedName("parentescoConmigo") val parentescoConmigo: String? = null
)
