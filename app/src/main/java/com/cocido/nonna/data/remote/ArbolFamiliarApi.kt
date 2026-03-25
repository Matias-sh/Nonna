package com.cocido.nonna.data.remote

import com.cocido.nonna.data.remote.dto.ArbolFamiliarResponseDto
import com.cocido.nonna.data.remote.dto.CrearPersonaResponseDto
import com.cocido.nonna.data.remote.dto.PersonaArbolCreateRequest
import com.cocido.nonna.data.remote.dto.PersonaArbolDto
import com.cocido.nonna.data.remote.dto.UnionArbolCreateRequest
import com.cocido.nonna.data.remote.dto.UnionArbolDto
import com.cocido.nonna.data.remote.dto.UnionesArbolResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ArbolFamiliarApi {

    /**
     * Obtiene el árbol familiar completo del usuario autenticado.
     *
     * GET /arbol-familiar/mi-arbol
     */
    @GET("arbol-familiar/mi-arbol")
    suspend fun miArbol(): Response<ArbolFamiliarResponseDto>

    /**
     * Crear un nuevo árbol familiar con la primera persona.
     *
     * POST /arbol-familiar/crear-arbol
     *
     * El backend permite crear solo un árbol por usuario.
     */
    @POST("arbol-familiar/crear-arbol")
    suspend fun crearArbol(
        @Body request: PersonaArbolCreateRequest
    ): Response<PersonaArbolDto>

    /**
     * Crear y añadir una persona al árbol familiar existente.
     * Si no existe un árbol, el backend crea uno nuevo.
     *
     * POST /arbol-familiar/persona
     */
    @POST("arbol-familiar/persona")
    suspend fun crearPersona(
        @Body request: PersonaArbolCreateRequest
    ): Response<CrearPersonaResponseDto>

    /**
     * Actualizar una persona del árbol familiar.
     *
     * PATCH /arbol-familiar/persona/{id}
     */
    @PATCH("arbol-familiar/persona/{id}")
    suspend fun actualizarPersona(
        @Path("id") id: String,
        @Body request: PersonaArbolCreateRequest
    ): Response<PersonaArbolDto>

    /**
     * Eliminar una persona del árbol familiar.
     *
     * DELETE /arbol-familiar/persona/{id}
     */
    @DELETE("arbol-familiar/persona/{id}")
    suspend fun eliminarPersona(
        @Path("id") id: String
    ): Response<Unit>

    /**
     * Eliminar el árbol familiar completo del usuario autenticado (soft delete).
     *
     * DELETE /arbol-familiar/arbol
     */
    @DELETE("arbol-familiar/arbol")
    suspend fun eliminarArbol(): Response<Unit>

    @GET("arbol-familiar/uniones")
    suspend fun listarUniones(): Response<UnionesArbolResponseDto>

    @GET("arbol-familiar/uniones/{id}")
    suspend fun obtenerUnion(@Path("id") id: String): Response<UnionArbolDto>

    @POST("arbol-familiar/union")
    suspend fun crearUnion(@Body request: UnionArbolCreateRequest): Response<UnionArbolDto>

    @DELETE("arbol-familiar/uniones/{id}")
    suspend fun eliminarUnion(@Path("id") id: String): Response<Unit>
}
