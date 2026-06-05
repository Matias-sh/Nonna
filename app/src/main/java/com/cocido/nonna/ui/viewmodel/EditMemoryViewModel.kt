package com.cocido.nonna.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.DataRefreshCoordinator
import com.cocido.nonna.data.repository.EmocionesRepository
import com.cocido.nonna.data.repository.RecuerdosRepository
import com.cocido.nonna.data.repository.SuscripcionRepository
import com.cocido.nonna.ui.components.EmotionalTag
import com.cocido.nonna.ui.components.MemoryType
import com.cocido.nonna.ui.components.MemoryUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditMemoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recuerdosRepository: RecuerdosRepository,
    private val emocionesRepository: EmocionesRepository,
    private val suscripcionRepository: SuscripcionRepository,
    private val refreshCoordinator: DataRefreshCoordinator
) : ViewModel() {

    private val memoryId: String = savedStateHandle.get<String>("memoryId") ?: ""

    private val _memory = MutableStateFlow<MemoryUiModel?>(null)
    val memory: StateFlow<MemoryUiModel?> = _memory.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _updated = MutableSharedFlow<Unit>()
    val updated: SharedFlow<Unit> = _updated.asSharedFlow()

    private val _emotionIdByName = MutableStateFlow<Map<String, String>>(emptyMap())

    private val _maxArchivosPorRecuerdo = MutableStateFlow(3)
    val maxArchivosPorRecuerdo: StateFlow<Int> = _maxArchivosPorRecuerdo.asStateFlow()

    init {
        loadEmotions()
        loadSubscriptionLimits()
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = recuerdosRepository.getRecuerdo(memoryId)) {
                is ApiResult.Success -> _memory.value = result.data
                is ApiResult.Error -> _errorMessage.emit(result.message)
                else -> Unit
            }
            _isLoading.value = false
        }
    }

    fun save(
        title: String,
        description: String?,
        date: String?,
        emotionalTag: EmotionalTag?,
        customEmotion: String?,
        replacementFile: java.io.File? = null,
        newPortadaAudio: java.io.File? = null,
        urlPortadaAudio: String? = null,
        galleryImages: List<java.io.File>? = null,
        limpiarImagenesGaleria: Boolean = false
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val customEmotionClean = customEmotion?.trim().takeUnless { it.isNullOrBlank() }
            val emotionId = if (customEmotionClean == null) {
                emotionalTag?.let { tag ->
                    _emotionIdByName.value[normalizeKey(tag.label)]
                }
            } else {
                null
            }
            val emotionCustomFallback = if (customEmotionClean == null && emotionId == null) {
                emotionalTag?.label
            } else {
                null
            }
            val current = _memory.value
            val urlArchivo = if (
                replacementFile == null &&
                current != null &&
                current.type != MemoryType.Text &&
                !current.mainMediaUrl.isNullOrBlank()
            ) {
                current.mainMediaUrl
            } else {
                null
            }
            val limpiar = limpiarImagenesGaleria && galleryImages.isNullOrEmpty()
            when (
                val result = recuerdosRepository.updateRecuerdo(
                    id = memoryId,
                    titulo = title.trim(),
                    descripcion = description?.trim().takeUnless { it.isNullOrBlank() },
                    fecha = date?.trim().takeUnless { it.isNullOrBlank() },
                    emocionId = emotionId,
                    emocionPersonalizada = customEmotionClean ?: emotionCustomFallback,
                    file = replacementFile,
                    urlArchivo = urlArchivo,
                    portadaAudio = newPortadaAudio,
                    urlPortadaAudio = urlPortadaAudio,
                    galleryImages = galleryImages,
                    limpiarImagenesGaleria = limpiar
                )
            ) {
                is ApiResult.Success -> {
                    refreshCoordinator.invalidateMemory(memoryId)
                    _updated.emit(Unit)
                }
                is ApiResult.Error -> _errorMessage.emit(result.message)
                else -> Unit
            }
            _isSaving.value = false
        }
    }

    private fun loadSubscriptionLimits() {
        viewModelScope.launch {
            when (val sub = suscripcionRepository.getMiSuscripcion()) {
                is ApiResult.Success -> {
                    val max = sub.data.limites?.maxArchivosPorRecuerdo
                        ?: sub.data.plan?.maxArchivosPorRecuerdo
                    if (max != null && max >= 1) {
                        _maxArchivosPorRecuerdo.value = max
                    }
                }
                else -> Unit
            }
        }
    }

    private fun loadEmotions() {
        viewModelScope.launch {
            emocionesRepository.search().collect { result ->
                if (result is ApiResult.Success) {
                    _emotionIdByName.value = result.data
                        .mapNotNull { emotion ->
                            val id = emotion.idValue().trim()
                            val name = emotion.displayName().trim()
                            if (id.isBlank() || name.isBlank()) null else normalizeKey(name) to id
                        }
                        .toMap()
                }
            }
        }
    }

    private fun normalizeKey(value: String): String {
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
            .trim()
    }
}

