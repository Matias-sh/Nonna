package com.cocido.nonna.data.repository

import android.util.Log
import com.cocido.nonna.data.mock.relationToApi
import com.cocido.nonna.data.remote.ArbolFamiliarApi
import com.cocido.nonna.data.remote.CofreRecuerdosApi
import com.cocido.nonna.data.remote.dto.PersonaArbolCreateRequest
import com.cocido.nonna.data.remote.dto.CofreCreateRequest
import com.cocido.nonna.data.remote.dto.CofreDto
import com.cocido.nonna.data.remote.dto.CofreInviteRequest
import com.cocido.nonna.ui.components.CofreUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.inject.Inject

class CofreRepository @Inject constructor(
    private val api: CofreRecuerdosApi,
    private val arbolFamiliarApi: ArbolFamiliarApi
) {
    companion object {
        private const val TAG = "CofreUpload"
    }

    fun misCofres(): Flow<ApiResult<List<CofreUiModel>>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = api.misCofres()
            if (response.isSuccessful) {
                val list = response.body()?.cofres?.map { it.toUiModel() } ?: emptyList()
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
            val trimmedDescription = description?.trim().orEmpty()
            if (trimmedDescription.isEmpty()) {
                return ApiResult.Error("La frase descriptiva es obligatoria.")
            }
            val validEmails = inviteEmails.map { it.trim() }.filter { it.contains("@") }
            val response = if (coverImageFile != null) {
                val nombre = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val parentesco = relationApi.toRequestBody("text/plain".toMediaTypeOrNull())
                val fraseDescripcion = trimmedDescription.toRequestBody("text/plain".toMediaTypeOrNull())
                val imageMime = detectImageMimeType(coverImageFile)
                Log.i(
                    TAG,
                    "createFull request file=${coverImageFile.name} mime=$imageMime size=${coverImageFile.length()}B relation=$relationApi invites=${validEmails.size}"
                )
                val fotoMimeForPart = "application/octet-stream"
                val fotoPart = MultipartBody.Part.createFormData(
                    "fotoPortada",
                    coverImageFile.name,
                    coverImageFile.asRequestBody(fotoMimeForPart.toMediaTypeOrNull())
                )
                Log.i(
                    TAG,
                    "fotoPortada part headers=${fotoPart.headers} contentType=${fotoPart.body.contentType()} contentLength=${fotoPart.body.contentLength()}"
                )
                val invitadosParts = if (validEmails.isEmpty()) {
                    // Swagger permite "Send empty value" para este campo opcional.
                    listOf(MultipartBody.Part.createFormData("invitadosEmails", ""))
                } else {
                    validEmails.map { email ->
                        MultipartBody.Part.createFormData("invitadosEmails", email)
                    }
                }
                api.createFull(
                    nombre = nombre,
                    parentesco = parentesco,
                    fraseDescripcion = fraseDescripcion,
                    fotoPortada = fotoPart,
                    invitadosEmails = invitadosParts
                )
            } else {
                api.create(
                    CofreCreateRequest(
                        nombre = name,
                        parentesco = relationApi,
                        fraseDescripcion = trimmedDescription
                    )
                )
            }
            if (response.isSuccessful) {
                val cofre = response.body()?.toUiModel() ?: return ApiResult.Error("Error al crear cofre")
                // Sincronización automática: cada cofre nuevo intenta crear su persona en el árbol.
                // No bloquea el alta del cofre si el árbol falla.
                runCatching {
                    arbolFamiliarApi.crearPersona(
                        PersonaArbolCreateRequest(
                            nombreCompleto = name,
                            unionPadresId = null,
                            parentescoConmigo = relationApi,
                            fechaNacimiento = null,
                            fechaFallecimiento = null,
                            notasPersonales = description,
                            crearCofre = false,
                            crearUnionRaiz = null
                        )
                    )
                }
                if (coverImageFile == null && validEmails.isNotEmpty()) {
                    val inviteResult = invitar(cofre.id, validEmails)
                    if (inviteResult is ApiResult.Error) {
                        return inviteResult
                    }
                }
                ApiResult.Success(cofre)
            } else {
                val rawError = response.errorBody()?.string()
                Log.w(
                    TAG,
                    "createCofre failed code=${response.code()} body=${rawError?.take(500)}"
                )
                val message = when (response.code()) {
                    413 -> "La imagen de portada es demasiado grande. Probá con una más chica."
                    400 -> normalizeUploadErrorMessage(rawError)
                    else -> NetworkErrorParser.parse(rawError) ?: rawError ?: "Error"
                }
                ApiResult.Error(message, response.code())
            }
        } catch (e: HttpException) {
            val rawError = e.response()?.errorBody()?.string()
            Log.e(
                TAG,
                "createCofre exception code=${e.code()} body=${rawError?.take(500)}",
                e
            )
            val message = when (e.code()) {
                413 -> "La imagen de portada es demasiado grande. Probá con una más chica."
                400 -> normalizeUploadErrorMessage(rawError)
                else -> NetworkErrorParser.parse(rawError) ?: rawError ?: e.message()
            }
            ApiResult.Error(message ?: "Error", e.code())
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
                val fraseDescripcion = description
                    ?.trim()
                    .orEmpty()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val imageMime = detectImageMimeType(coverImageFile)
                val fotoPart = MultipartBody.Part.createFormData(
                    "fotoPortada",
                    coverImageFile.name,
                    coverImageFile.asRequestBody(imageMime.toMediaTypeOrNull())
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
                ApiResult.Error(response.errorBody()?.string() ?: "Error", response.code())
            }
        } catch (e: HttpException) {
            ApiResult.Error(e.response()?.errorBody()?.string() ?: e.message(), e.code())
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
            else ApiResult.Error(response.errorBody()?.string() ?: "Error", response.code())
        } catch (e: HttpException) {
            ApiResult.Error(e.response()?.errorBody()?.string() ?: e.message(), e.code())
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }
}

private fun normalizeUploadErrorMessage(rawBody: String?): String {
    val parsed = NetworkErrorParser.parse(rawBody)
    val lower = parsed?.lowercase() ?: rawBody?.lowercase() ?: return "Error al crear cofre"
    return when {
        lower.contains("error al subir archivo") ->
            parsed ?: rawBody ?: "No pudimos procesar la imagen de portada."
        lower.contains("multipart") || lower.contains("part") || lower.contains("archivo") || lower.contains("imagen") ->
            parsed ?: rawBody ?: "No pudimos subir la imagen. Revisá formato y volvé a intentar."
        else -> parsed ?: rawBody ?: "Error al crear cofre"
    }
}

private fun detectImageMimeType(file: File): String {
    return when (file.extension.lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        else -> "application/octet-stream"
    }
}

private fun CofreDto.toUiModel(): CofreUiModel = CofreUiModel(
    id = idValue(),
    name = displayName(),
    relation = displayRelation(),
    photoCount = photoCount ?: 0,
    audioCount = audioCount ?: 0,
    textCount = textCount ?: 0,
    memberCount = memberCount ?: 1,
    lastUpdated = formatLastUpdated(updatedAt ?: updated_at),
    coverImageUrl = coverUrl(),
    isOwner = isOwnerValue()
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
