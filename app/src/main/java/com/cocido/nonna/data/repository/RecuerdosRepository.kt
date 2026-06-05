package com.cocido.nonna.data.repository

import android.util.Log
import com.cocido.nonna.data.remote.RecuerdosApi
import com.cocido.nonna.data.remote.dto.RecuerdoDto
import com.cocido.nonna.ui.components.EmotionalTag
import com.cocido.nonna.ui.components.MemoryType
import com.cocido.nonna.ui.components.MemoryUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import com.cocido.nonna.util.MemoryMediaUrlHeuristics
import com.cocido.nonna.util.NetworkFailureMessageResolver
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import okio.BufferedSink
import okio.source

class RecuerdosRepository @Inject constructor(
    private val api: RecuerdosApi
) {
    private companion object {
        const val TOMCAT_EMOTION_TAG = "TomcatEmotionCheck"
    }

    suspend fun recuerdosByCofreOnce(cofreId: String): ApiResult<List<MemoryUiModel>> {
        return try {
            val response = api.search(cofreRecuerdosId = cofreId)
            if (response.isSuccessful) {
                val body = response.body()
                val list = (body?.list() ?: emptyList()).map { it.toUiModel() }
                ApiResult.Success(list)
            } else {
                val raw = response.errorBody()?.string()
                ApiResult.Error(
                    NetworkErrorParser.parse(raw) ?: "No se pudieron cargar los recuerdos",
                    response.code()
                )
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

    fun recuerdosByCofre(cofreId: String): Flow<ApiResult<List<MemoryUiModel>>> = flow {
        emit(ApiResult.Loading)
        when (val result = recuerdosByCofreOnce(cofreId)) {
            is ApiResult.Success -> emit(result)
            is ApiResult.Error -> emit(result)
            else -> Unit
        }
    }

    suspend fun getRecuerdo(id: String): ApiResult<MemoryUiModel> {
        return try {
            val response = api.getById(id)
            if (response.isSuccessful) {
                response.body()?.let { ApiResult.Success(it.toUiModel()) }
                    ?: ApiResult.Error("Recuerdo no encontrado")
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

    suspend fun createRecuerdo(
        cofreRecuerdosId: String,
        titulo: String,
        file: File,
        descripcion: String? = null,
        fecha: String? = null,
        emocionId: String? = null,
        emocionPersonalizada: String? = null,
        portadaAudio: File? = null,
        galleryImages: List<File> = emptyList(),
        onUploadProgress: ((uploadedBytes: Long, totalBytes: Long) -> Unit)? = null
    ): ApiResult<MemoryUiModel> {
        return try {
            val mediaType = mediaTypeForFile(file)
            val progressFiles = buildList {
                add(file)
                if (portadaAudio != null) add(portadaAudio)
                addAll(galleryImages)
            }.filter { it.exists() && it.length() > 0L }
            val totalUploadBytes = progressFiles.sumOf { it.length() }
            val uploadedBytes = AtomicLong(0L)
            val filePart = createProgressPart(
                partName = "file",
                file = file,
                mediaType = mediaType,
                totalBytes = totalUploadBytes,
                uploadedBytes = uploadedBytes,
                onUploadProgress = onUploadProgress
            )
            val textPlain = "text/plain".toMediaTypeOrNull()
            val tituloBody = titulo.toRequestBody(contentType = textPlain)
            val descripcionBody = (descripcion ?: "").toRequestBody(contentType = textPlain)
            val fechaBody = (fecha ?: "").toRequestBody(contentType = textPlain)
            val emocionIdBody = emocionId?.trim()?.takeIf { it.isNotBlank() }?.toRequestBody(contentType = textPlain)
            val emocionPersonalizadaBody =
                emocionPersonalizada?.trim()?.takeIf { it.isNotBlank() }?.toRequestBody(contentType = textPlain)

            val isMainImage = file.extension.lowercase() in setOf("jpg", "jpeg", "png", "gif", "webp")
            val isMainAudio = file.extension.lowercase() in setOf("mp3", "m4a", "ogg", "wav")

            val portadaPart = if (isMainAudio && portadaAudio != null) {
                val coverType = mediaTypeForFile(portadaAudio)
                createProgressPart(
                    partName = "portadaAudio",
                    file = portadaAudio,
                    mediaType = coverType,
                    totalBytes = totalUploadBytes,
                    uploadedBytes = uploadedBytes,
                    onUploadProgress = onUploadProgress
                )
            } else {
                null
            }

            val galleryParts = if (isMainImage && galleryImages.isNotEmpty()) {
                galleryImages.map { g ->
                    val gt = mediaTypeForFile(g)
                    createProgressPart(
                        partName = "imagenesGaleria",
                        file = g,
                        mediaType = gt,
                        totalBytes = totalUploadBytes,
                        uploadedBytes = uploadedBytes,
                        onUploadProgress = onUploadProgress
                    )
                }
            } else {
                null
            }

            val response = api.create(
                cofreRecuerdosId = cofreRecuerdosId,
                file = filePart,
                titulo = tituloBody,
                descripcion = descripcionBody,
                fecha = fechaBody,
                emocionId = emocionIdBody,
                emocionPersonalizada = emocionPersonalizadaBody,
                portadaAudio = portadaPart,
                imagenesGaleria = galleryParts
            )
            if (response.isSuccessful) {
                if (totalUploadBytes > 0L) {
                    onUploadProgress?.invoke(totalUploadBytes, totalUploadBytes)
                }
                response.body()?.let { ApiResult.Success(it.toUiModel()) }
                    ?: ApiResult.Error("Error al crear recuerdo")
            } else {
                val raw = response.errorBody()?.string() ?: "Error"
                val message = mapRecuerdoBackendError(raw, response.code())
                ApiResult.Error(message, response.code())
            }
        } catch (e: HttpException) {
            val raw = e.response()?.errorBody()?.string() ?: e.message() ?: "Error"
            val message = mapRecuerdoBackendError(raw, e.code())
            ApiResult.Error(message ?: "Error", e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(NetworkFailureMessageResolver.fromIOException(e))
        }
    }

    suspend fun updateRecuerdo(
        id: String,
        titulo: String? = null,
        descripcion: String? = null,
        fecha: String? = null,
        emocionId: String? = null,
        emocionPersonalizada: String? = null,
        file: File? = null,
        urlArchivo: String? = null,
        portadaAudio: File? = null,
        urlPortadaAudio: String? = null,
        galleryImages: List<File>? = null,
        limpiarImagenesGaleria: Boolean = false
    ): ApiResult<MemoryUiModel> {
        return try {
            Log.d(
                TOMCAT_EMOTION_TAG,
                "PATCH recuerdos/$id multipart -> emocionId=$emocionId, emocionPersonalizada=$emocionPersonalizada, " +
                    "hasFile=${file != null}, urlArchivo=${urlArchivo != null}, limpiarGaleria=$limpiarImagenesGaleria"
            )
            val textPlain = "text/plain".toMediaTypeOrNull()
            val filePart = file?.let { f ->
                val mediaType = mediaTypeForFile(f)
                MultipartPartHelper.createFormDataFile("file", f, mediaType)
            }
            val urlArchivoBody = urlArchivo?.takeIf { it.isNotBlank() }?.toRequestBody(contentType = textPlain)
            val portadaPart = portadaAudio?.let { f ->
                val coverType = mediaTypeForFile(f)
                MultipartBody.Part.createFormData("portadaAudio", f.name, RequestBody.create(coverType, f))
            }
            val urlPortadaBody = urlPortadaAudio?.let { it.toRequestBody(contentType = textPlain) }
            val galleryParts = galleryImages
                ?.filter { it.exists() && it.length() > 0 }
                ?.takeIf { it.isNotEmpty() }
                ?.map { g ->
                    val gt = mediaTypeForFile(g)
                    MultipartBody.Part.createFormData("imagenesGaleria", g.name, RequestBody.create(gt, g))
                }
            val limpiarPart = if (limpiarImagenesGaleria) {
                "true".toRequestBody(contentType = textPlain)
            } else {
                null
            }
            val response = api.update(
                id = id,
                file = filePart,
                urlArchivo = urlArchivoBody,
                portadaAudio = portadaPart,
                urlPortadaAudio = urlPortadaBody,
                imagenesGaleria = galleryParts,
                limpiarImagenesGaleria = limpiarPart,
                titulo = titulo?.toRequestBody(contentType = textPlain),
                descripcion = descripcion?.toRequestBody(contentType = textPlain),
                fecha = fecha?.toRequestBody(contentType = textPlain),
                emocionId = emocionId?.trim()?.takeIf { it.isNotBlank() }?.toRequestBody(contentType = textPlain),
                emocionPersonalizada = emocionPersonalizada?.trim()?.takeIf { it.isNotBlank() }
                    ?.toRequestBody(contentType = textPlain)
            )
            if (response.isSuccessful) {
                response.body()?.let { updated ->
                    Log.d(
                        TOMCAT_EMOTION_TAG,
                        "PATCH recuerdos/$id response -> emocionId=${updated.emocionId}, emocionPersonalizada=${updated.emocionPersonalizada}"
                    )
                    ApiResult.Success(updated.toUiModel())
                } ?: ApiResult.Error("Error al actualizar")
            } else {
                val raw = response.errorBody()?.string() ?: "Error"
                val message = mapRecuerdoBackendError(raw, response.code())
                ApiResult.Error(message, response.code())
            }
        } catch (e: HttpException) {
            val raw = e.response()?.errorBody()?.string() ?: e.message() ?: "Error"
            val message = mapRecuerdoBackendError(raw, e.code())
            ApiResult.Error(message ?: "Error", e.code())
        } catch (e: JsonParseException) {
            ApiResult.Error(API_RESPONSE_PARSE_ERROR)
        } catch (e: IOException) {
            ApiResult.Error(NetworkFailureMessageResolver.fromIOException(e))
        }
    }

    suspend fun deleteRecuerdo(id: String): ApiResult<Unit> {
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
}

private fun createProgressPart(
    partName: String,
    file: File,
    mediaType: okhttp3.MediaType,
    totalBytes: Long,
    uploadedBytes: AtomicLong,
    onUploadProgress: ((uploadedBytes: Long, totalBytes: Long) -> Unit)?
): MultipartBody.Part {
    val delegate = file.asRequestBody(mediaType)
    val progressBody = object : RequestBody() {
        override fun contentType() = delegate.contentType()
        override fun contentLength(): Long = delegate.contentLength()
        override fun writeTo(sink: BufferedSink) {
            file.source().use { source ->
                val buffer = okio.Buffer()
                var read: Long
                while (source.read(buffer, 8 * 1024).also { read = it } != -1L) {
                    sink.write(buffer, read)
                    val uploaded = uploadedBytes.addAndGet(read)
                    if (totalBytes > 0L) {
                        onUploadProgress?.invoke(uploaded.coerceAtMost(totalBytes), totalBytes)
                    }
                }
            }
        }
    }
    return MultipartBody.Part.createFormData(partName, file.name, progressBody)
}

private fun mediaTypeForFile(file: File) = when (file.extension.lowercase()) {
    "jpg", "jpeg", "png", "gif", "webp" -> "image/*".toMediaTypeOrNull()
    "txt" -> "text/plain".toMediaTypeOrNull()
    "mp3", "m4a", "ogg", "wav" -> "audio/*".toMediaTypeOrNull()
    else -> "application/octet-stream".toMediaTypeOrNull()
} ?: "application/octet-stream".toMediaTypeOrNull()!!

private fun normalizeRecuerdoDate(raw: String): String {
    val t = raw.trim()
    if (t.length >= 10 && t[4] == '-' && t[7] == '-') return t.substring(0, 10)
    return t
}

private fun mapRecuerdoBackendError(raw: String, code: Int?): String {
    val normalized = raw.lowercase()
    val parsed = NetworkErrorParser.parse(raw, code)
    if (!parsed.isNullOrBlank() && !parsed.equals("Datos de entrada no válidos.", ignoreCase = true)) {
        return parsed
    }
    if (code == 413 || normalized.contains("too large") || normalized.contains("payload too large")) {
        val type = detectUploadType(normalized)
        val max = extractMaxSizeLabel(raw) ?: "No informado por servidor"
        return "Superaste el tamaño máximo de archivo para $type. El máximo es: $max."
    }
    if (normalized.contains("cofr03_descripcion") || normalized.contains("descripcion")) {
        return "La descripción es demasiado larga. Reducí el texto e intentá nuevamente."
    }
    return parsed ?: NetworkErrorParser.parseOrGeneric(null, code)
}

private fun detectUploadType(normalizedRaw: String): String {
    return when {
        normalizedRaw.contains("audio") || normalizedRaw.contains("m4a") || normalizedRaw.contains("mp3") -> "audio"
        normalizedRaw.contains("image") || normalizedRaw.contains("imagen") || normalizedRaw.contains("jpg") || normalizedRaw.contains("png") -> "imagen"
        normalizedRaw.contains("text") || normalizedRaw.contains("texto") || normalizedRaw.contains("txt") || normalizedRaw.contains("descripcion") -> "texto"
        else -> "archivo"
    }
}

private fun extractMaxSizeLabel(raw: String): String? {
    val regex = Regex("""(\d+(?:[.,]\d+)?)\s*(kb|mb|gb|bytes|byte|b)""", RegexOption.IGNORE_CASE)
    val match = regex.find(raw) ?: return null
    val value = match.groupValues[1].replace(',', '.')
    val unitRaw = match.groupValues[2].lowercase()
    val unit = when (unitRaw) {
        "kb" -> "KB"
        "mb" -> "MB"
        "gb" -> "GB"
        "byte", "bytes", "b" -> "B"
        else -> unitRaw.uppercase()
    }
    return "$value $unit"
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

private fun normalizeTipoMedia(raw: String?): MemoryType? {
    val v = raw?.trim()?.lowercase() ?: return null
    return when {
        v == "imagen" || v == "image" || v == "photo" || v == "foto" || v == "picture" -> MemoryType.Photo
        v == "audio" || v == "sonido" -> MemoryType.Audio
        v == "texto" || v == "text" || v == "txt" || v == "document" || v == "documento" -> MemoryType.Text
        v.contains("imag") || v.contains("photo") || v.contains("foto") || v.contains("img") -> MemoryType.Photo
        v.contains("audio") || v.contains("m4a") || v.contains("mp3") -> MemoryType.Audio
        v.contains("text") || v.contains("txt") || v.contains("doc") -> MemoryType.Text
        else -> null
    }
}

private fun inferMemoryTypeFromUrls(urls: List<String>): MemoryType {
    if (urls.isEmpty()) return MemoryType.Text
    if (urls.any { MemoryMediaUrlHeuristics.looksLikeAudioFileUrl(it) }) return MemoryType.Audio
    if (urls.any { MemoryMediaUrlHeuristics.looksLikePlainTextFileUrl(it) }) return MemoryType.Text
    return MemoryType.Photo
}

private fun RecuerdoDto.toUiModel(): MemoryUiModel {
    val sortedDetails = archivosDetalle?.sortedBy { it.orden ?: 0 }.orEmpty()
    val primary = sortedDetails.firstOrNull()

    val candidateHttpUrls = buildList {
        sortedDetails.forEach { d -> d.rutaArchivo?.let { add(it) } }
        rutaArchivo?.let { add(it) }
        thumbnailUrl?.let { add(it) }
        audioUrl?.let { add(it) }
        urlsCarruselImagenes?.forEach { add(it) }
    }
        .mapNotNull { it.trim().takeIf { s -> s.isNotBlank() && MemoryMediaUrlHeuristics.isHttpUrl(s) } }
        .distinct()

    var explicitType = normalizeTipoMedia(primary?.tipoArchivo)
        ?: normalizeTipoMedia(tipoArchivo)
        ?: normalizeTipoMedia(tipo)
        ?: normalizeTipoMedia(type)

    if (!urlsCarruselImagenes.isNullOrEmpty()) {
        explicitType = explicitType ?: MemoryType.Photo
    }

    val inferredFromUrls = inferMemoryTypeFromUrls(candidateHttpUrls)

    val memoryType = when {
        explicitType == MemoryType.Text &&
            candidateHttpUrls.isNotEmpty() &&
            inferredFromUrls != MemoryType.Text -> inferredFromUrls
        explicitType != null -> explicitType
        candidateHttpUrls.isNotEmpty() -> inferredFromUrls
        else -> MemoryType.Text
    }

    val mainUrl = primary?.rutaArchivo?.trim()?.takeIf { it.isNotBlank() }
        ?: rutaArchivo?.trim()?.takeIf { it.isNotBlank() }
        ?: thumbnailUrl?.trim()?.takeIf { it.isNotBlank() }

    val carouselFromApi = urlsCarruselImagenes
        ?.mapNotNull { it.trim().takeIf { u -> MemoryMediaUrlHeuristics.isHttpUrl(u) } }
        ?.distinct()
        ?.take(3)
        .orEmpty()

    val carouselFromDetails = sortedDetails
        .filter { it.tipoArchivo?.equals("imagen", ignoreCase = true) == true }
        .mapNotNull { it.rutaArchivo?.trim()?.takeIf { u -> MemoryMediaUrlHeuristics.isHttpUrl(u) } }
        .distinct()
        .take(3)

    val nonAudioNonTxt = candidateHttpUrls.filter {
        !MemoryMediaUrlHeuristics.looksLikeAudioFileUrl(it) &&
            !MemoryMediaUrlHeuristics.looksLikePlainTextFileUrl(it)
    }

    val carousel = when (memoryType) {
        MemoryType.Photo -> when {
            carouselFromApi.isNotEmpty() -> carouselFromApi
            carouselFromDetails.isNotEmpty() -> carouselFromDetails
            mainUrl != null && MemoryMediaUrlHeuristics.isHttpUrl(mainUrl) -> listOf(mainUrl)
            nonAudioNonTxt.isNotEmpty() -> nonAudioNonTxt.take(3)
            candidateHttpUrls.isNotEmpty() -> candidateHttpUrls.take(3)
            else -> emptyList()
        }
        else -> emptyList()
    }

    val thumbForPhoto = carousel.firstOrNull()
        ?: mainUrl?.takeIf { memoryType == MemoryType.Photo && MemoryMediaUrlHeuristics.isHttpUrl(it) }

    val audioStreamUrl = when (memoryType) {
        MemoryType.Audio -> primary?.rutaArchivo?.trim()?.takeIf { MemoryMediaUrlHeuristics.isHttpUrl(it) }
            ?: audioUrl?.trim()?.takeIf { MemoryMediaUrlHeuristics.isHttpUrl(it) }
            ?: rutaArchivo?.trim()?.takeIf { MemoryMediaUrlHeuristics.isHttpUrl(it) }
        MemoryType.Text -> primary?.rutaArchivo?.trim()?.takeIf { MemoryMediaUrlHeuristics.isHttpUrl(it) }
            ?: rutaArchivo?.trim()?.takeIf { MemoryMediaUrlHeuristics.isHttpUrl(it) }
            ?: audioUrl?.trim()?.takeIf { MemoryMediaUrlHeuristics.isHttpUrl(it) }
        else -> null
    }

    val coverAudio = if (memoryType == MemoryType.Audio) {
        primary?.portadaAudioUrl()?.takeIf { MemoryMediaUrlHeuristics.isHttpUrl(it) }
            ?: thumbnailUrl?.trim()?.takeIf { MemoryMediaUrlHeuristics.isDisplayableImageUrl(it) }
    } else {
        null
    }

    val emocionTag = resolveEmotionalTag(
        nombreFromApi = emocion?.displayName(),
        emocionPersonalizada = emocionPersonalizada
    )
    val emotionalCustomLabel = emocionPersonalizada?.trim()?.takeIf { it.isNotBlank() }
    return MemoryUiModel(
        id = idValue(),
        type = memoryType,
        title = displayTitle(),
        description = displayDescription(),
        date = normalizeRecuerdoDate(displayDate()),
        emotionalTag = emocionTag,
        emotionalCustomLabel = if (emocionTag == null) emotionalCustomLabel else null,
        thumbnailUrl = when (memoryType) {
            MemoryType.Photo -> thumbForPhoto
            MemoryType.Audio -> coverAudio
            else -> null
        },
        audioUrl = audioStreamUrl,
        duration = displayDuration(),
        carouselImageUrls = if (memoryType == MemoryType.Photo) carousel else emptyList(),
        audioCoverUrl = coverAudio,
        mainMediaUrl = mainUrl
    )
}
