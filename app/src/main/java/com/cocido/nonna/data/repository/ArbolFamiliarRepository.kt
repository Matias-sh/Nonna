package com.cocido.nonna.data.repository

import com.cocido.nonna.data.mock.TreeNode
import com.cocido.nonna.data.remote.ArbolFamiliarApi
import com.cocido.nonna.data.remote.dto.ArbolFamiliarResponseDto
import com.cocido.nonna.data.remote.dto.PersonaArbolCreateRequest
import com.cocido.nonna.data.remote.dto.PersonaArbolDto
import com.cocido.nonna.util.NetworkFailureMessageResolver
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
                val raw = response.errorBody()?.string()
                emit(ApiResult.Error(NetworkErrorParser.parse(raw) ?: "Error", response.code()))
            }
        } catch (e: HttpException) {
            val raw = e.response()?.errorBody()?.string()
            emit(ApiResult.Error(NetworkErrorParser.parseOrGeneric(raw, e.code()), e.code()))
        } catch (e: JsonParseException) {
            emit(ApiResult.Error(API_RESPONSE_PARSE_ERROR))
        } catch (e: IOException) {
            emit(ApiResult.Error(NetworkFailureMessageResolver.fromIOException(e)))
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
                val raw = response.errorBody()?.string()
                ApiResult.Error(NetworkErrorParser.parse(raw) ?: "Error", response.code())
            }
        } catch (e: HttpException) {
            val raw = e.response()?.errorBody()?.string()
            ApiResult.Error(NetworkErrorParser.parseOrGeneric(raw, e.code()), e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(NetworkFailureMessageResolver.fromIOException(e))
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
                val raw = response.errorBody()?.string()
                ApiResult.Error(NetworkErrorParser.parse(raw) ?: "Error", response.code())
            }
        } catch (e: HttpException) {
            val raw = e.response()?.errorBody()?.string()
            ApiResult.Error(NetworkErrorParser.parseOrGeneric(raw, e.code()), e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(NetworkFailureMessageResolver.fromIOException(e))
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
                val raw = response.errorBody()?.string()
                ApiResult.Error(NetworkErrorParser.parse(raw) ?: "Error", response.code())
            }
        } catch (e: HttpException) {
            val raw = e.response()?.errorBody()?.string()
            ApiResult.Error(NetworkErrorParser.parseOrGeneric(raw, e.code()), e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(NetworkFailureMessageResolver.fromIOException(e))
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
                val raw = response.errorBody()?.string()
                ApiResult.Error(NetworkErrorParser.parse(raw) ?: "Error", response.code())
            }
        } catch (e: HttpException) {
            val raw = e.response()?.errorBody()?.string()
            ApiResult.Error(NetworkErrorParser.parseOrGeneric(raw, e.code()), e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(NetworkFailureMessageResolver.fromIOException(e))
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
