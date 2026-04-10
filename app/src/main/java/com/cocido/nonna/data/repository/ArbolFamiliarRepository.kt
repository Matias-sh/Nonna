package com.cocido.nonna.data.repository

import com.cocido.nonna.data.mock.TreeNode
import com.cocido.nonna.data.remote.ArbolFamiliarApi
import com.cocido.nonna.data.remote.dto.ArbolFamiliarResponseDto
import com.cocido.nonna.data.remote.dto.PersonaArbolCreateRequest
import com.cocido.nonna.data.remote.dto.PersonaArbolDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.google.gson.JsonParseException
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
                val nodes = body?.personas?.map { it.toTreeNode() } ?: emptyList()
                emit(ApiResult.Success(nodes))
            } else {
                emit(ApiResult.Error(response.errorBody()?.string() ?: "Error", response.code()))
            }
        } catch (e: HttpException) {
            emit(ApiResult.Error(e.response()?.errorBody()?.string() ?: e.message(), e.code()))
        } catch (e: JsonParseException) {
            emit(ApiResult.Error(API_RESPONSE_PARSE_ERROR))
        } catch (e: IOException) {
            emit(ApiResult.Error("Sin conexión. Revisá tu internet."))
        }
    }

    /**
     * Crear y añadir una persona al árbol familiar (el backend crea el árbol si no existe).
     */
    suspend fun crearPersona(
        nombreCompleto: String,
        conexionId: Int? = null,
        fechaNacimiento: String? = null,
        fechaFallecimiento: String? = null,
        notasPersonales: String? = null,
        crearCofre: Boolean = false
    ): ApiResult<TreeNode> {
        return try {
            val response = api.crearPersona(
                PersonaArbolCreateRequest(
                    nombreCompleto = nombreCompleto,
                    conexionId = conexionId,
                    fechaNacimiento = fechaNacimiento,
                    fechaFallecimiento = fechaFallecimiento,
                    notasPersonales = notasPersonales,
                    crearCofre = crearCofre
                )
            )
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it.toTreeNode()) }
                    ?: ApiResult.Error("Error al crear persona")
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Error", response.code())
            }
        } catch (e: HttpException) {
            ApiResult.Error(e.response()?.errorBody()?.string() ?: e.message(), e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
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
        conexionId: Int? = null,
        fechaNacimiento: String? = null,
        fechaFallecimiento: String? = null,
        notasPersonales: String? = null
    ): ApiResult<TreeNode> {
        return try {
            val response = api.actualizarPersona(
                id = id,
                request = PersonaArbolCreateRequest(
                    nombreCompleto = nombreCompleto,
                    conexionId = conexionId,
                    fechaNacimiento = fechaNacimiento,
                    fechaFallecimiento = fechaFallecimiento,
                    notasPersonales = notasPersonales,
                    crearCofre = false
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
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
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
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
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
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
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
