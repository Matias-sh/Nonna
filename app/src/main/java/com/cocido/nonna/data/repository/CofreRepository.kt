package com.cocido.nonna.data.repository

import com.cocido.nonna.data.mock.relationToApi
import com.cocido.nonna.data.mock.relationDisplayToApi
import com.cocido.nonna.data.remote.CofreRecuerdosApi
import com.cocido.nonna.data.remote.UsuarioApi
import com.cocido.nonna.data.remote.dto.CofreCreateRequest
import com.cocido.nonna.data.remote.dto.CofreDto
import com.cocido.nonna.data.remote.dto.CofreInviteRequest
import com.cocido.nonna.data.remote.dto.CofreInvitationDto
import com.cocido.nonna.ui.components.CofreUiModel
import com.cocido.nonna.ui.components.CofreInvitationUiModel
import com.cocido.nonna.ui.components.CofreInviteeUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.cocido.nonna.util.NetworkFailureMessageResolver
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
    private val api: CofreRecuerdosApi,
    private val usuarioApi: UsuarioApi
) {
    private val inviteeAvatarCacheByEmail = mutableMapOf<String, String>()

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

    suspend fun getCofre(id: String): ApiResult<CofreUiModel> {
        return try {
            val response = api.getById(id)
            if (response.isSuccessful) {
                response.body()?.let {
                    ApiResult.Success(enrichInviteesAvatar(it.toUiModel()))
                }
                    ?: ApiResult.Error("Cofre no encontrado")
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

    private suspend fun enrichInviteesAvatar(cofre: CofreUiModel): CofreUiModel {
        val unresolvedEmails = cofre.invited
            .filter { it.avatarUrl.isNullOrBlank() && it.email.isNotBlank() }
            .map { it.email.trim().lowercase() }
            .distinct()
        if (unresolvedEmails.isEmpty()) return cofre

        val resolvedByEmail = mutableMapOf<String, String>()
        unresolvedEmails.forEach { email ->
            inviteeAvatarCacheByEmail[email]?.let {
                resolvedByEmail[email] = it
                return@forEach
            }
            runCatching {
                val search = usuarioApi.search(query = email)
                if (!search.isSuccessful) return@runCatching
                val user = search.body()?.list()?.firstOrNull { it.email.equals(email, ignoreCase = true) }
                    ?: return@runCatching
                user.profileImageUrl()?.let {
                    inviteeAvatarCacheByEmail[email] = it
                    resolvedByEmail[email] = it
                }
            }
        }
        if (resolvedByEmail.isEmpty()) return cofre
        return applyResolvedInviteeAvatars(cofre, resolvedByEmail)
    }

    suspend fun createCofre(
        name: String,
        relation: String,
        description: String? = null,
        coverImageFile: File? = null,
        inviteEmails: List<String> = emptyList()
    ): ApiResult<CofreUiModel> {
        return try {
            val relationInput = relation.trim()
            val relationApi = relationToApi(relationInput)
            val customRelation = relationInput.takeIf {
                relationApi == "OTRO" &&
                    !it.equals("OTRO", ignoreCase = true) &&
                    !it.equals("Otro", ignoreCase = true) &&
                    relationDisplayToApi[it] == null
            }
            val validEmails = inviteEmails.map { it.trim() }.filter { it.contains("@") }
            val response = if (coverImageFile != null) {
                val nombre = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val parentesco = relationApi.toRequestBody("text/plain".toMediaTypeOrNull())
                val parentescoPersonalizado = customRelation
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())
                val fraseDescripcion = (description ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
                val fotoPart = MultipartBody.Part.createFormData(
                    "fotoPortada",
                    coverImageFile.name,
                    coverImageFile.asRequestBody("image/*".toMediaTypeOrNull())
                )
                // Evitamos enviar invitados en multipart porque el backend valida ese campo
                // de forma estricta y puede rechazar valores válidos por formato de serialización.
                api.createFull(
                    nombre = nombre,
                    parentesco = parentesco,
                    parentescoPersonalizado = parentescoPersonalizado,
                    fraseDescripcion = fraseDescripcion,
                    fotoPortada = fotoPart,
                    invitadosEmails = null
                )
            } else {
                api.create(
                    CofreCreateRequest(
                        nombre = name,
                        parentesco = relationApi,
                        parentescoPersonalizado = customRelation,
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
                NetworkErrorParser.parseOrGeneric(e.response()?.errorBody()?.string(), e.code()),
                e.code()
            )
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(NetworkFailureMessageResolver.fromIOException(e))
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
            val relationInput = relation.trim()
            val relationApi = relationToApi(relationInput)
            val customRelation = relationInput.takeIf {
                relationApi == "OTRO" &&
                    !it.equals("OTRO", ignoreCase = true) &&
                    !it.equals("Otro", ignoreCase = true) &&
                    relationDisplayToApi[it] == null
            }
            val response = if (coverImageFile != null) {
                val nombre = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val parentesco = relationApi.toRequestBody("text/plain".toMediaTypeOrNull())
                val parentescoPersonalizado = customRelation
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())
                val fraseDescripcion = (description ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
                val fotoPart = MultipartBody.Part.createFormData(
                    "fotoPortada",
                    coverImageFile.name,
                    coverImageFile.asRequestBody("image/*".toMediaTypeOrNull())
                )
                api.updateFull(
                    id = id,
                    nombre = nombre,
                    parentesco = parentesco,
                    parentescoPersonalizado = parentescoPersonalizado,
                    fraseDescripcion = fraseDescripcion,
                    fotoPortada = fotoPart,
                    urlPortada = null,
                    invitadosEmails = null
                )
            } else {
                api.update(
                    id,
                    CofreCreateRequest(
                        nombre = name,
                        parentesco = relationApi,
                        parentescoPersonalizado = customRelation,
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
                NetworkErrorParser.parseOrGeneric(e.response()?.errorBody()?.string(), e.code()),
                e.code()
            )
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(NetworkFailureMessageResolver.fromIOException(e))
        }
    }

    suspend fun deleteCofre(id: String): ApiResult<Unit> {
        return try {
            val response = api.delete(id)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else {
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

    suspend fun abandonarCofreCompartido(cofreId: String): ApiResult<Unit> {
        return try {
            val response = api.abandonarCofreCompartido(cofreId)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string())
                        ?: "No se pudo abandonar el cofre",
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(
                NetworkErrorParser.parseOrGeneric(e.response()?.errorBody()?.string(), e.code()),
                e.code()
            )
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(NetworkFailureMessageResolver.fromIOException(e))
        }
    }

    suspend fun eliminarInvitadoAceptado(cofreId: String, invitadoUsuarioId: String): ApiResult<Unit> {
        if (invitadoUsuarioId.isBlank()) {
            return ApiResult.Error("No se puede quitar a este invitado desde la app (falta id de usuario).")
        }
        return try {
            val response = api.eliminarInvitadoAceptado(cofreId, invitadoUsuarioId)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else {
                ApiResult.Error(
                    NetworkErrorParser.parse(response.errorBody()?.string())
                        ?: "No se pudo quitar al invitado",
                    response.code()
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(
                NetworkErrorParser.parseOrGeneric(e.response()?.errorBody()?.string(), e.code()),
                e.code()
            )
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(NetworkFailureMessageResolver.fromIOException(e))
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
                NetworkErrorParser.parseOrGeneric(e.response()?.errorBody()?.string(), e.code()),
                e.code()
            )
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(NetworkFailureMessageResolver.fromIOException(e))
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
            ApiResult.Error(
                NetworkErrorParser.parseOrGeneric(e.response()?.errorBody()?.string(), e.code()),
                e.code()
            )
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(NetworkFailureMessageResolver.fromIOException(e))
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
            ApiResult.Error(
                NetworkErrorParser.parseOrGeneric(e.response()?.errorBody()?.string(), e.code()),
                e.code()
            )
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(NetworkFailureMessageResolver.fromIOException(e))
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
            ApiResult.Error(
                NetworkErrorParser.parseOrGeneric(e.response()?.errorBody()?.string(), e.code()),
                e.code()
            )
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(NetworkFailureMessageResolver.fromIOException(e))
        }
    }
}

internal fun applyResolvedInviteeAvatars(
    cofre: CofreUiModel,
    resolvedByEmail: Map<String, String>
): CofreUiModel {
    if (resolvedByEmail.isEmpty()) return cofre
    return cofre.copy(
        invited = cofre.invited.map { invitee ->
            val key = invitee.email.trim().lowercase()
            val avatar = invitee.avatarUrl ?: resolvedByEmail[key]
            if (avatar == null) invitee else invitee.copy(avatarUrl = avatar)
        }
    )
}

private fun CofreDto.toUiModel(forceNotOwner: Boolean = false): CofreUiModel = CofreUiModel(
    id = idValue(),
    name = displayName(),
    relation = displayRelation(),
    descriptionPhrase = listOfNotNull(
        fraseDescripcion?.trim()?.takeIf { it.isNotBlank() },
        descripcion?.trim()?.takeIf { it.isNotBlank() },
        description?.trim()?.takeIf { it.isNotBlank() }
    ).firstOrNull(),
    photoCount = photoCount ?: 0,
    audioCount = audioCount ?: 0,
    textCount = textCount ?: 0,
    memberCount = memberCount ?: run {
        val inviteEmails = invitedList().mapNotNull { inv ->
            inv.email?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
        }.distinct()
        (1 + inviteEmails.size).coerceAtLeast(1)
    },
    lastUpdated = formatLastUpdated(updatedAt ?: updated_at),
    updatedAtIso = updatedAt ?: updated_at,
    coverImageUrl = coverUrl(),
    isOwner = if (forceNotOwner) false else isOwnerValue(),
    ownerName = usuario?.displayNameValue(),
    ownerUsername = usuario?.nombreUsuario,
    ownerEmail = usuario?.email,
    ownerAvatarUrl = usuario?.profileImageUrlValue(),
    invited = invitedList().map { invite ->
        CofreInviteeUiModel(
            id = invite.idValue(),
            email = invite.email.orEmpty(),
            accepted = invite.invitacionAceptada == true,
            fullName = listOfNotNull(invite.persona?.nombre, invite.persona?.apellido)
                .joinToString(" ")
                .ifBlank { null },
            avatarUrl = invite.profileImageUrlValue(),
            invitedUserId = invite.invitadoUsuarioIdForApi()
        )
    }
)

private fun com.cocido.nonna.data.remote.dto.UsuarioDto.displayNameValue(): String {
    return listOfNotNull(persona?.nombre, persona?.apellido)
        .joinToString(" ")
        .trim()
        .ifBlank { nombreUsuario ?: email ?: "" }
}

private fun com.cocido.nonna.data.remote.dto.UsuarioDto.profileImageUrlValue(): String? {
    val raw = fotoPerfil?.takeIf { it.isNotBlank() && it != "string" } ?: return null
    return when {
        raw.startsWith("http://") || raw.startsWith("https://") -> raw
        raw.startsWith("/") -> "https://apinonna.pushsoftware.com.ar$raw"
        else -> "https://apinonna.pushsoftware.com.ar/$raw"
    }
}

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
