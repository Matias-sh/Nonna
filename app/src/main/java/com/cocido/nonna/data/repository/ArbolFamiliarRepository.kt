package com.cocido.nonna.data.repository

import com.cocido.nonna.data.mock.TreeNode
import com.cocido.nonna.data.remote.ArbolFamiliarApi
import com.cocido.nonna.data.remote.dto.ArbolFamiliarResponseDto
import com.cocido.nonna.data.remote.dto.PersonaArbolCreateRequest
import com.cocido.nonna.data.remote.dto.PersonaArbolDto
import com.cocido.nonna.data.remote.dto.UnionArbolCreateRequest
import com.cocido.nonna.data.remote.dto.UnionArbolDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ArbolFamiliarRepository @Inject constructor(
    private val api: ArbolFamiliarApi
) {
    /**
     * Obtiene el árbol familiar completo del usuario autenticado.
     */
    fun miArbol(): Flow<ApiResult<List<TreeNode>>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = api.miArbol()
            if (response.isSuccessful) {
                val body: ArbolFamiliarResponseDto? = response.body()
                val personaNodes = body?.personas?.map { it.toTreeNode() } ?: emptyList()
                val yoNode = body?.usuario?.persona?.toYoTreeNodeOrNull()
                val nodes = if (yoNode != null && personaNodes.none { it.id == yoNode.id }) {
                    personaNodes + yoNode
                } else {
                    personaNodes
                }
                emit(ApiResult.Success(nodes))
            } else {
                emit(ApiResult.Error(response.errorBody()?.string() ?: "Error", response.code()))
            }
        } catch (e: HttpException) {
            emit(ApiResult.Error(e.response()?.errorBody()?.string() ?: e.message(), e.code()))
        } catch (e: IOException) {
            emit(ApiResult.Error("Sin conexión. Revisá tu internet."))
        }
    }

    /**
     * Crear y añadir una persona al árbol familiar (el backend crea el árbol si no existe).
     */
    suspend fun crearPersona(
        nombreCompleto: String,
        unionPadresId: Int? = null,
        parentescoConmigo: String? = null,
        fechaNacimiento: String? = null,
        fechaFallecimiento: String? = null,
        notasPersonales: String? = null,
        crearCofre: Boolean = false,
        crearUnionRaiz: Boolean? = null
    ): ApiResult<TreeNode> {
        return try {
            val response = api.crearPersona(
                PersonaArbolCreateRequest(
                    nombreCompleto = nombreCompleto,
                    unionPadresId = unionPadresId,
                    parentescoConmigo = parentescoConmigo,
                    fechaNacimiento = fechaNacimiento,
                    fechaFallecimiento = fechaFallecimiento,
                    notasPersonales = notasPersonales,
                    crearCofre = crearCofre,
                    crearUnionRaiz = crearUnionRaiz
                )
            )
            if (response.isSuccessful) {
                response.body()?.toPersonaDtoOrNull()?.let { ApiResult.Success(it.toTreeNode()) }
                    ?: ApiResult.Error("Error al crear persona")
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Error", response.code())
            }
        } catch (e: HttpException) {
            ApiResult.Error(e.response()?.errorBody()?.string() ?: e.message(), e.code())
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    /**
     * Actualizar una persona del árbol familiar.
     */
    suspend fun actualizarPersona(
        id: String,
        nombreCompleto: String,
        unionPadresId: Int? = null,
        parentescoConmigo: String? = null,
        fechaNacimiento: String? = null,
        fechaFallecimiento: String? = null,
        notasPersonales: String? = null
    ): ApiResult<TreeNode> {
        return try {
            val response = api.actualizarPersona(
                id = id,
                request = PersonaArbolCreateRequest(
                    nombreCompleto = nombreCompleto,
                    unionPadresId = unionPadresId,
                    parentescoConmigo = parentescoConmigo,
                    fechaNacimiento = fechaNacimiento,
                    fechaFallecimiento = fechaFallecimiento,
                    notasPersonales = notasPersonales,
                    crearCofre = false,
                    crearUnionRaiz = null
                )
            )
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it.toTreeNode()) }
                    ?: ApiResult.Error("Error al actualizar persona")
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Error", response.code())
            }
        } catch (e: HttpException) {
            ApiResult.Error(e.response()?.errorBody()?.string() ?: e.message(), e.code())
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    /**
     * Eliminar una persona del árbol familiar.
     */
    suspend fun eliminarPersona(id: String): ApiResult<Unit> {
        return try {
            val response = api.eliminarPersona(id)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Error", response.code())
            }
        } catch (e: HttpException) {
            ApiResult.Error(e.response()?.errorBody()?.string() ?: e.message(), e.code())
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    /**
     * Eliminar todo el árbol familiar del usuario.
     */
    suspend fun eliminarArbol(): ApiResult<Unit> {
        return try {
            val response = api.eliminarArbol()
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Error", response.code())
            }
        } catch (e: HttpException) {
            ApiResult.Error(e.response()?.errorBody()?.string() ?: e.message(), e.code())
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun crearUnion(
        parent1Id: Int,
        parent2Id: Int? = null
    ): ApiResult<UnionArbolDto> {
        return try {
            val response = api.crearUnion(
                UnionArbolCreateRequest(
                    parent1Id = parent1Id,
                    parent2Id = parent2Id
                )
            )
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it) }
                    ?: ApiResult.Error("Error al crear unión")
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Error", response.code())
            }
        } catch (e: HttpException) {
            ApiResult.Error(e.response()?.errorBody()?.string() ?: e.message(), e.code())
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun listarUniones(): ApiResult<List<UnionArbolDto>> {
        return try {
            val response = api.listarUniones()
            if (response.isSuccessful) {
                ApiResult.Success(response.body()?.uniones.orEmpty())
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Error", response.code())
            }
        } catch (e: HttpException) {
            ApiResult.Error(e.response()?.errorBody()?.string() ?: e.message(), e.code())
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun obtenerUnion(id: String): ApiResult<UnionArbolDto> {
        return try {
            val response = api.obtenerUnion(id)
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it) }
                    ?: ApiResult.Error("Unión no encontrada")
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Error", response.code())
            }
        } catch (e: HttpException) {
            ApiResult.Error(e.response()?.errorBody()?.string() ?: e.message(), e.code())
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun eliminarUnion(id: String): ApiResult<Unit> {
        return try {
            val response = api.eliminarUnion(id)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Error", response.code())
            }
        } catch (e: HttpException) {
            ApiResult.Error(e.response()?.errorBody()?.string() ?: e.message(), e.code())
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }
}

private fun com.cocido.nonna.data.remote.dto.CrearPersonaResponseDto.toPersonaDtoOrNull(): PersonaArbolDto? {
    return persona
        ?: id?.let {
            PersonaArbolDto(
                id = it,
                nombreCompleto = nombreCompleto,
                fechaNacimiento = fechaNacimiento,
                fechaFallecimiento = fechaFallecimiento,
                parentescoConmigo = parentescoConmigo
            )
        }
}

private fun PersonaArbolDto.toTreeNode(): TreeNode = TreeNode(
    id = id,
    name = displayName(),
    relation = displayRelation(),
    cofreId = cofreIdOrNull(),
    avatarUrl = avatarUrl,
    birthDate = fechaNacimiento ?: birthDate,
    deathDate = fechaFallecimiento ?: deathDate,
    children = childrenList().map { it.toTreeNode() }
)

private fun com.cocido.nonna.data.remote.dto.PersonaBasicaDto.toYoTreeNodeOrNull(): TreeNode? {
    val displayName = listOfNotNull(nombre, apellido)
        .joinToString(" ")
        .trim()
    if (displayName.isBlank()) return null
    return TreeNode(
        id = id.toString(),
        name = displayName,
        relation = "YO",
        cofreId = null,
        avatarUrl = null,
        birthDate = null,
        deathDate = null,
        children = emptyList()
    )
}
