package com.cocido.nonna.data.repository

import com.cocido.nonna.data.mock.TreeNode
import com.cocido.nonna.data.remote.ArbolFamiliarApi
import com.cocido.nonna.data.remote.dto.PersonaArbolCreateRequest
import com.cocido.nonna.data.remote.dto.PersonaArbolDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ArbolFamiliarRepository @Inject constructor(
    private val api: ArbolFamiliarApi
) {
    fun search(query: String? = null): Flow<ApiResult<List<TreeNode>>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = api.search(query)
            if (response.isSuccessful) {
                val list = response.body()?.list()?.map { it.toTreeNode() } ?: emptyList()
                emit(ApiResult.Success(list))
            } else {
                emit(ApiResult.Error(response.errorBody()?.string() ?: "Error", response.code()))
            }
        } catch (e: HttpException) {
            emit(ApiResult.Error(e.response()?.errorBody()?.string() ?: e.message(), e.code()))
        } catch (e: IOException) {
            emit(ApiResult.Error("Sin conexión. Revisá tu internet."))
        }
    }

    suspend fun createPerson(
        nombre: String,
        parentesco: String? = null,
        cofreRecuerdosId: String? = null,
        fechaNacimiento: String? = null,
        fechaFallecimiento: String? = null
    ): ApiResult<TreeNode> {
        return try {
            val response = api.create(
                PersonaArbolCreateRequest(
                    nombre = nombre,
                    name = nombre,
                    parentesco = parentesco,
                    relation = parentesco,
                    cofreRecuerdosId = cofreRecuerdosId,
                    fechaNacimiento = fechaNacimiento,
                    fechaFallecimiento = fechaFallecimiento
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
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun getById(id: String): ApiResult<TreeNode> {
        return try {
            val response = api.getById(id)
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it.toTreeNode()) }
                    ?: ApiResult.Error("No encontrado")
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Error", response.code())
            }
        } catch (e: HttpException) {
            ApiResult.Error(e.response()?.errorBody()?.string() ?: e.message(), e.code())
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun delete(id: String): ApiResult<Unit> {
        return try {
            val response = api.delete(id)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(response.errorBody()?.string() ?: "Error", response.code())
        } catch (e: HttpException) {
            ApiResult.Error(e.response()?.errorBody()?.string() ?: e.message(), e.code())
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }
}

private fun PersonaArbolDto.toTreeNode(): TreeNode = TreeNode(
    id = id,
    name = displayName(),
    relation = displayRelation(),
    cofreId = cofreId(),
    avatarUrl = avatarUrl,
    birthDate = fechaNacimiento ?: birthDate,
    deathDate = fechaFallecimiento ?: deathDate,
    children = childrenList().map { it.toTreeNode() }
)
