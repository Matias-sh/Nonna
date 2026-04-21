package com.cocido.nonna.data.repository

import com.cocido.nonna.data.remote.RecuerdosApi
import com.cocido.nonna.data.remote.dto.RecuerdoCreateRequest
import com.cocido.nonna.data.remote.dto.RecuerdoDto
import com.cocido.nonna.ui.components.EmotionalTag
import com.cocido.nonna.ui.components.MemoryType
import com.cocido.nonna.ui.components.MemoryUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.inject.Inject

class RecuerdosRepository @Inject constructor(
    private val api: RecuerdosApi
) {
    fun recuerdosByCofre(cofreId: String): Flow<ApiResult<List<MemoryUiModel>>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = api.search(cofreRecuerdosId = cofreId)
            val list = when {
                response.isSuccessful -> {
                    val body = response.body()
                    (body?.list() ?: emptyList()).map { it.toUiModel() }
                }
                else -> emptyList()
            }
            emit(ApiResult.Success(list))
        } catch (e: HttpException) {
            emit(ApiResult.Error(e.response()?.errorBody()?.string() ?: e.message(), e.code()))
        } catch (e: JsonParseException) {
            emit(ApiResult.Error(API_RESPONSE_PARSE_ERROR))
        } catch (e: IOException) {
            emit(ApiResult.Error("Sin conexión. Revisá tu internet."))
        }
    }

    suspend fun getRecuerdo(id: String): ApiResult<MemoryUiModel> {
        return try {
            val response = api.getById(id)
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it.toUiModel()) }
                    ?: ApiResult.Error("Recuerdo no encontrado")
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

    suspend fun createRecuerdo(
        cofreRecuerdosId: String,
        titulo: String,
        file: File,
        descripcion: String? = null,
        fecha: String? = null,
        emocionId: String? = null,
        emocionPersonalizada: String? = null
    ): ApiResult<MemoryUiModel> {
        return try {
            val mediaType = when (file.extension.lowercase()) {
                "jpg", "jpeg", "png", "gif", "webp" -> "image/*".toMediaTypeOrNull()
                "txt" -> "text/plain".toMediaTypeOrNull()
                "mp3", "m4a", "ogg", "wav" -> "audio/*".toMediaTypeOrNull()
                else -> "application/octet-stream".toMediaTypeOrNull()
            } ?: "application/octet-stream".toMediaTypeOrNull()!!
            val filePart = MultipartPartHelper.createFormDataFile("file", file, mediaType)
            val textPlain = "text/plain".toMediaTypeOrNull()
            val tituloBody = titulo.toRequestBody(contentType = textPlain)
            val descripcionBody = (descripcion ?: "").toRequestBody(contentType = textPlain)
            val fechaBody = (fecha ?: "").toRequestBody(contentType = textPlain)
            val emocionIdBody = emocionId?.let { it.toRequestBody(contentType = textPlain) }
            val emocionPersonalizadaBody = emocionPersonalizada?.let { it.toRequestBody(contentType = textPlain) }

            val response = api.create(
                cofreRecuerdosId = cofreRecuerdosId,
                file = filePart,
                titulo = tituloBody,
                descripcion = descripcionBody,
                fecha = fechaBody,
                emocionId = emocionIdBody,
                emocionPersonalizada = emocionPersonalizadaBody
            )
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it.toUiModel()) }
                    ?: ApiResult.Error("Error al crear recuerdo")
            } else {
                val message = when (response.code()) {
                    413 -> "La imagen es demasiado grande. Probá con otra más chica."
                    else -> response.errorBody()?.string() ?: "Error"
                }
                ApiResult.Error(message, response.code())
            }
        } catch (e: HttpException) {
            val message = when (e.code()) {
                413 -> "La imagen es demasiado grande. Probá con otra más chica."
                else -> e.response()?.errorBody()?.string() ?: e.message()
            }
            ApiResult.Error(message ?: "Error", e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun updateRecuerdo(
        id: String,
        titulo: String? = null,
        descripcion: String? = null,
        fecha: String? = null,
        emocionId: String? = null,
        emocionPersonalizada: String? = null,
        file: File? = null
    ): ApiResult<MemoryUiModel> {
        return try {
            val response = if (file != null) {
                val mediaType = when (file.extension.lowercase()) {
                    "jpg", "jpeg", "png", "gif", "webp" -> "image/*".toMediaTypeOrNull()
                    "txt" -> "text/plain".toMediaTypeOrNull()
                    "mp3", "m4a", "ogg", "wav" -> "audio/*".toMediaTypeOrNull()
                    else -> "application/octet-stream".toMediaTypeOrNull()
                } ?: "application/octet-stream".toMediaTypeOrNull()!!
                val filePart = MultipartPartHelper.createFormDataFile("file", file, mediaType)
                val textPlain = "text/plain".toMediaTypeOrNull()
                api.updateFull(
                    id = id,
                    file = filePart,
                    titulo = titulo?.toRequestBody(contentType = textPlain),
                    descripcion = descripcion?.toRequestBody(contentType = textPlain),
                    fecha = fecha?.toRequestBody(contentType = textPlain),
                    emocionId = emocionId?.toRequestBody(contentType = textPlain),
                    emocionPersonalizada = emocionPersonalizada?.toRequestBody(contentType = textPlain)
                )
            } else {
                api.update(
                    id,
                    RecuerdoCreateRequest(
                        titulo = titulo,
                        descripcion = descripcion,
                        fecha = fecha,
                        emocionId = emocionId,
                        emocionPersonalizada = emocionPersonalizada
                    )
                )
            }
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it.toUiModel()) }
                    ?: ApiResult.Error("Error al actualizar")
            } else {
                val message = when (response.code()) {
                    413 -> "El archivo es demasiado grande. Probá con uno más liviano."
                    else -> response.errorBody()?.string() ?: "Error"
                }
                ApiResult.Error(message, response.code())
            }
        } catch (e: HttpException) {
            val message = when (e.code()) {
                413 -> "El archivo es demasiado grande. Probá con uno más liviano."
                else -> e.response()?.errorBody()?.string() ?: e.message()
            }
            ApiResult.Error(message ?: "Error", e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error("Sin conexión. Revisá tu internet.")
        }
    }

    suspend fun deleteRecuerdo(id: String): ApiResult<Unit> {
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
}

/** Resuelve EmotionalTag desde el nombre que devuelve la API o emocionPersonalizada. */
private fun resolveEmotionalTag(
    nombreFromApi: String?,
    emocionPersonalizada: String?
): EmotionalTag? {
    val fromPersonalizada = emocionPersonalizada?.trim()?.takeIf { it.isNotBlank() }?.let { value ->
        EmotionalTag.entries.find { it.label.equals(value, ignoreCase = true) }
    }
    if (fromPersonalizada != null) return fromPersonalizada

    val name = nombreFromApi?.trim()?.takeIf { it.isNotBlank() } ?: return null
    val exact = EmotionalTag.entries.find { it.label.equals(name, ignoreCase = true) }
    if (exact != null) return exact

    return when {
        name.equals("Alegría", true) || name.equals("Alegria", true) || name.startsWith("Alegr", true) -> EmotionalTag.Alegre
        name.equals("Nostalgia", true) || name.equals("Nostalgico", true) || name.startsWith("Nostalg", true) -> EmotionalTag.Nostalgico
        name.equals("Calma", true) || name.startsWith("Calm", true) -> EmotionalTag.Calmo
        name.startsWith("Familiar", true) -> EmotionalTag.Familiar
        else -> null
    }
}

private fun RecuerdoDto.toUiModel(): MemoryUiModel {
    val tipoStr = (tipoArchivo ?: tipo ?: type)?.uppercase() ?: "TEXTO"
    val memoryType = when {
        tipoStr.contains("FOTO") || tipoStr.contains("PHOTO") || tipoStr.contains("IMAGE") || tipoStr == "IMAGEN" -> MemoryType.Photo
        tipoStr.contains("AUDIO") -> MemoryType.Audio
        tipoStr.contains("TEXTO") || tipoStr.contains("TEXT") || tipoStr.contains("TXT") || tipoStr.contains("DOCUMENT") -> MemoryType.Text
        else -> MemoryType.Text
    }
    val imageUrl = rutaArchivo ?: thumbnailUrl
    val emocionTag = resolveEmotionalTag(
        nombreFromApi = emocion?.displayName(),
        emocionPersonalizada = emocionPersonalizada
    )
    return MemoryUiModel(
        id = idValue(),
        type = memoryType,
        title = displayTitle(),
        description = displayDescription(),
        date = displayDate(),
        emotionalTag = emocionTag,
        thumbnailUrl = imageUrl,
        audioUrl = audioUrl ?: rutaArchivo,
        duration = displayDuration()
    )
}
