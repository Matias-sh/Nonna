package com.cocido.nonna.data.repository

import com.cocido.nonna.data.mock.relationToApi
import com.cocido.nonna.data.remote.CofreRecuerdosApi
import com.cocido.nonna.data.remote.dto.CofreCreateRequest
import com.cocido.nonna.data.remote.dto.CofreDto
import com.cocido.nonna.data.remote.dto.CofreInviteRequest
import com.cocido.nonna.data.remote.dto.CofreInvitationDto
import com.cocido.nonna.ui.components.CofreUiModel
import com.cocido.nonna.ui.components.CofreInvitationUiModel
import com.cocido.nonna.ui.components.CofreInviteeUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.inject.Inject

class CofreRepository @Inject constructor(
    private val api: CofreRecuerdosApi
) {
    fun misCofres(): Flow<ApiResult<List<CofreUiModel>>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = api.misCofres()
            if (response.isSuccessful) {
                val body = response.body()
                val owned = body?.cofres.orEmpty().map { it.toUiModel() }
                val invited = body?.cofresInvitado.orEmpty().map { it.toUiModel(forceNotOwner = true) }
                val merged = LinkedHashMap<String, CofreUiModel>()
                owned.forEach { merged[it.id] = it }
                invited.forEach { if (!merged.containsKey(it.id)) merged[it.id] = it }
                emit(ApiResult.Success(merged.values.toList()))
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

    suspend fun getCofre(id: String): ApiResult<CofreUiModel> {
        return try {
            val response = api.getById(id)
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it.toUiModel()) }
                    ?: ApiResult.Error("Cofre no encontrado")
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

    suspend fun createCofre(
        name: String,
        relation: String,
        description: String? = null,
        coverImageFile: File? = null,
        inviteEmails: List<String> = emptyList()
    ): ApiResult<CofreUiModel> {
        return try {
            val relationApi = relationToApi(relation)
            val validEmails = inviteEmails.map { it.trim() }.filter { it.contains("@") }
            val response = if (coverImageFile != null) {
                val nombre = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val parentesco = relationApi.toRequestBody("text/plain".toMediaTypeOrNull())
                val fraseDescripcion = (description ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
                val fotoPart = MultipartBody.Part.createFormData(
                    "fotoPortada",
                    coverImageFile.name,
                    coverImageFile.asRequestBody("image/*".toMediaTypeOrNull())
                )
                // Evitamos enviar invitados en multipart porque el backend valida ese campo
                // de forma estricta y puede rechazar valores válidos por formato de serialización.
                api.createFull(nombre, parentesco, fraseDescripcion, fotoPart, null)
            } else {
                api.create(
                    CofreCreateRequest(
                        nombre = name,
                        parentesco = relationApi,
                        fraseDescripcion = description
                    )
                )
            }
            if (response.isSuccessful) {
                val cofre = response.body()?.toUiModel() ?: return ApiResult.Error("Error al crear cofre")
                if (validEmails.isNotEmpty()) {
                    val inviteResult = invitar(cofre.id, validEmails)
                    if (inviteResult is ApiResult.Error) {
                        return inviteResult
                    }
                }
                ApiResult.Success(cofre)
            } else {
                val raw = response.errorBody()?.string()
                ApiResult.Error(
                    NetworkErrorParser.parse(raw) ?: "No se pudo crear el cofre",
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(
                NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(),
                e.code()
            )
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun updateCofre(
        id: String,
        name: String,
        relation: String,
        description: String? = null,
        coverImageFile: File? = null,
        existingCoverUrl: String? = null
    ): ApiResult<CofreUiModel> {
        return try {
            val relationApi = relationToApi(relation)
            val response = if (coverImageFile != null) {
                val nombre = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val parentesco = relationApi.toRequestBody("text/plain".toMediaTypeOrNull())
                val fraseDescripcion = (description ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
                val fotoPart = MultipartBody.Part.createFormData(
                    "fotoPortada",
                    coverImageFile.name,
                    coverImageFile.asRequestBody("image/*".toMediaTypeOrNull())
                )
                api.updateFull(id, nombre, parentesco, fraseDescripcion, fotoPart, null, null)
            } else {
                api.update(
                    id,
                    CofreCreateRequest(
                        nombre = name,
                        parentesco = relationApi,
                        fraseDescripcion = description
                    )
                )
            }
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it.toUiModel()) }
                    ?: ApiResult.Error("Error al actualizar")
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string()) ?: "No se pudo actualizar el cofre",
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(
                NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(),
                e.code()
            )
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun deleteCofre(id: String): ApiResult<Unit> {
        return try {
            val response = api.delete(id)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(response.errorBody()?.string() ?: "Error", response.code())
        } catch (e: HttpException) {
            ApiResult.Error(e.response()?.errorBody()?.string() ?: e.message(), e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun invitar(cofreId: String, emails: List<String>): ApiResult<Unit> {
        if (emails.isEmpty()) return ApiResult.Error("Indicá al menos un email")
        return try {
            val cofreRecuerdoId = cofreId.toIntOrNull() ?: return ApiResult.Error("ID de cofre inválido")
            val response = api.invitar(
                CofreInviteRequest(
                    cofreRecuerdoId = cofreRecuerdoId,
                    emailsUsuariosInvitados = emails
                )
            )
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(
                NetworkErrorParser.parse(response.errorBody()?.string()) ?: "No se pudo enviar la invitación",
                response.code()
            )
        } catch (e: HttpException) {
            ApiResult.Error(
                NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(),
                e.code()
            )
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun getPendingInvitations(): ApiResult<List<CofreInvitationUiModel>> {
        return try {
            val response = api.misInvitacionesPendientes()
            if (response.isSuccessful) {
                ApiResult.Success(response.body().toInvitationsUi())
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string()) ?: "No se pudieron cargar tus invitaciones",
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(), e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun getSentInvitations(): ApiResult<List<CofreInvitationUiModel>> {
        return try {
            val response = api.misInvitacionesEnviadas()
            if (response.isSuccessful) {
                ApiResult.Success(response.body().toInvitationsUi())
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string()) ?: "No se pudieron cargar las invitaciones enviadas",
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(), e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun acceptInvitation(invitationId: String): ApiResult<Unit> = performInvitationAction {
        api.aceptarInvitacion(invitationId)
    }

    suspend fun rejectInvitation(invitationId: String): ApiResult<Unit> = performInvitationAction {
        api.rechazarInvitacion(invitationId)
    }

    suspend fun cancelInvitation(invitationId: String): ApiResult<Unit> = performInvitationAction {
        api.cancelarInvitacion(invitationId)
    }

    private suspend fun performInvitationAction(
        block: suspend () -> retrofit2.Response<Unit>
    ): ApiResult<Unit> {
        return try {
            val response = block()
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string()) ?: "No se pudo completar la acción",
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(NetworkErrorParser.parse(e.response()?.errorBody()?.string()) ?: e.message(), e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }
}

private fun CofreDto.toUiModel(forceNotOwner: Boolean = false): CofreUiModel = CofreUiModel(
    id = idValue(),
    name = displayName(),
    relation = displayRelation(),
    photoCount = photoCount ?: 0,
    audioCount = audioCount ?: 0,
    textCount = textCount ?: 0,
    memberCount = memberCount ?: 1,
    lastUpdated = formatLastUpdated(updatedAt ?: updated_at),
    updatedAtIso = updatedAt ?: updated_at,
    coverImageUrl = coverUrl(),
    isOwner = if (forceNotOwner) false else isOwnerValue(),
    invited = invitedList().map { invite ->
        CofreInviteeUiModel(
            id = invite.idValue(),
            email = invite.email.orEmpty(),
            accepted = invite.invitacionAceptada == true,
            fullName = listOfNotNull(invite.persona?.nombre, invite.persona?.apellido)
                .joinToString(" ")
                .ifBlank { null }
        )
    }
)

private fun formatLastUpdated(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        val date = java.time.Instant.parse(iso)
        val now = java.time.Instant.now()
        val diff = java.time.Duration.between(date, now)
        when {
            diff.toDays() > 0 -> "hace ${diff.toDays()} días"
            diff.toHours() > 0 -> "hace ${diff.toHours()} h"
            diff.toMinutes() > 0 -> "hace ${diff.toMinutes()} min"
            else -> "hace un momento"
        }
    } catch (_: Exception) {
        iso
    }
}

private fun com.cocido.nonna.data.remote.dto.PagedResponse<CofreInvitationDto>?.toInvitationsUi(): List<CofreInvitationUiModel> {
    return this?.list()?.map { it.toUiModel() } ?: emptyList()
}

private fun CofreInvitationDto.toUiModel(): CofreInvitationUiModel {
    val cofre = cofreDisplay()
    val inviter = cofre?.usuario
    val inviterDisplayName = listOfNotNull(inviter?.persona?.nombre, inviter?.persona?.apellido)
        .joinToString(" ")
        .ifBlank { inviter?.nombreUsuario ?: inviter?.email }
    val stateSignature = listOf(
        updatedAt.orEmpty(),
        createdAt.orEmpty(),
        (invitacionAceptada == true).toString(),
        (expirada == true).toString()
    ).joinToString("|")
    return CofreInvitationUiModel(
        id = idValue(),
        cofreId = cofre?.idValue().orEmpty(),
        cofreName = cofre?.displayName().orEmpty(),
        inviteeEmail = email.orEmpty(),
        inviterName = inviterDisplayName,
        inviterEmail = inviter?.email,
        cofreCoverImageUrl = cofre?.coverUrl(),
        cofreDescription = cofre?.fraseDescripcion ?: cofre?.descripcion ?: cofre?.description,
        accepted = invitacionAceptada == true,
        expired = expirada == true,
        stateSignature = stateSignature
    )
}
