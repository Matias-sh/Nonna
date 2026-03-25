package com.cocido.nonna.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.CofreRepository
import com.cocido.nonna.ui.components.CofreUiModel
import com.cocido.nonna.util.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class CreateCofreViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cofreRepository: CofreRepository
) : ViewModel() {
    companion object {
        // Limite conservador para reducir rechazos 413 del backend.
        private const val MAX_COVER_UPLOAD_BYTES = 900 * 1024L
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _created = MutableSharedFlow<CofreUiModel>()
    val created: SharedFlow<CofreUiModel> = _created.asSharedFlow()

    fun create(
        name: String,
        relation: String,
        description: String?,
        coverImageUri: Uri?,
        inviteEmails: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            var coverFile: File? = null
            try {
                val trimmedDescription = description?.trim().orEmpty()
                if (trimmedDescription.isEmpty()) {
                    _errorMessage.emit("La frase descriptiva es obligatoria.")
                    return@launch
                }
                val hasCoverImage = coverImageUri != null
                if (coverImageUri != null) {
                    val detectedMime = context.contentResolver.getType(coverImageUri)
                    if (detectedMime != null && !isSupportedUploadMime(detectedMime)) {
                        _errorMessage.emit(
                            "Formato no soportado ($detectedMime). Usá JPG, PNG, GIF o WEBP."
                        )
                        return@launch
                    }
                    coverFile = ImageCompressor.compressForUpload(
                        context = context,
                        uri = coverImageUri,
                        maxBytes = MAX_COVER_UPLOAD_BYTES
                    ) ?: uriToTempFileOrNull(coverImageUri)
                    if (coverFile == null) {
                        _errorMessage.emit(
                            "No pudimos procesar esa imagen. Elegí otra JPG/PNG/GIF/WEBP e intentá nuevamente."
                        )
                        return@launch
                    }
                }

                when (val result = cofreRepository.createCofre(
                    name = name,
                    relation = relation,
                    description = trimmedDescription,
                    coverImageFile = coverFile,
                    inviteEmails = inviteEmails
                )) {
                    is ApiResult.Success -> _created.emit(result.data)
                    is ApiResult.Error -> {
                        val shouldRetryWithoutImage = hasCoverImage && (
                            result.code == 413
                        )
                        if (shouldRetryWithoutImage) {
                            when (val retryWithoutImage = cofreRepository.createCofre(
                                name = name,
                                relation = relation,
                                description = trimmedDescription,
                                coverImageFile = null,
                                inviteEmails = inviteEmails
                            )) {
                                is ApiResult.Success -> {
                                    _created.emit(retryWithoutImage.data)
                                    _errorMessage.emit("El cofre se creó sin imagen porque hubo un problema con la portada.")
                                }
                                is ApiResult.Error -> _errorMessage.emit(retryWithoutImage.message)
                                else -> Unit
                            }
                        } else {
                            _errorMessage.emit(result.message)
                        }
                    }
                    else -> { }
                }
            } finally {
                coverFile?.delete()
                _isLoading.value = false
            }
        }
    }

    private fun isSupportedUploadMime(mime: String): Boolean {
        return mime.equals("image/jpeg", ignoreCase = true) ||
            mime.equals("image/jpg", ignoreCase = true) ||
            mime.equals("image/png", ignoreCase = true) ||
            mime.equals("image/gif", ignoreCase = true) ||
            mime.equals("image/webp", ignoreCase = true)
    }

    private suspend fun uriToTempFileOrNull(uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val rawMime = context.contentResolver.getType(uri).orEmpty().lowercase()
            val ext = when {
                rawMime.contains("jpeg") || rawMime.contains("jpg") -> "jpg"
                rawMime.contains("png") -> "png"
                rawMime.contains("gif") -> "gif"
                rawMime.contains("webp") -> "webp"
                else -> "jpg"
            }
            val file = File.createTempFile("cofre_cover", ".$ext", context.cacheDir)
            val input = context.contentResolver.openInputStream(uri) ?: return@withContext null
            input.use { inStream ->
                file.outputStream().use { output -> inStream.copyTo(output) }
            }
            file
        } catch (_: FileNotFoundException) {
            null
        } catch (_: SecurityException) {
            null
        } catch (_: IOException) {
            null
        }
    }
}
