package com.cocido.nonna.ui.viewmodel

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.DataRefreshCoordinator
import com.cocido.nonna.data.repository.EmocionesRepository
import com.cocido.nonna.data.repository.RecuerdosRepository
import com.cocido.nonna.data.repository.SuscripcionRepository
import com.cocido.nonna.ui.components.EmotionalTag
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
class AddMemoryViewModel @Inject constructor(
    private val recuerdosRepository: RecuerdosRepository,
    private val emocionesRepository: EmocionesRepository,
    private val suscripcionRepository: SuscripcionRepository,
    private val refreshCoordinator: DataRefreshCoordinator
) : ViewModel() {
    companion object {
        private const val PERF_TAG = "NonnaPerf"
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _saved = MutableSharedFlow<MemoryUiModel>()
    val saved: SharedFlow<MemoryUiModel> = _saved.asSharedFlow()
    private val _uploadProgress = MutableStateFlow<Float?>(null)
    val uploadProgress: StateFlow<Float?> = _uploadProgress.asStateFlow()

    private val _emotionIdByName = MutableStateFlow<Map<String, String>>(emptyMap())

    /** Límite del plan (archivos por recuerdo, incluye el principal). Mínimo 1; por defecto 3 (1 principal + 2 galería). */
    private val _maxArchivosPorRecuerdo = MutableStateFlow(3)
    val maxArchivosPorRecuerdo: StateFlow<Int> = _maxArchivosPorRecuerdo.asStateFlow()

    init {
        loadEmotions()
        loadSubscriptionLimits()
    }

    fun save(
        cofreRecuerdosId: String,
        titulo: String,
        file: java.io.File,
        descripcion: String? = null,
        fecha: String? = null,
        emocionId: String? = null,
        emocionPersonalizada: String? = null,
        portadaAudio: java.io.File? = null,
        galleryImages: List<java.io.File> = emptyList()
    ) {
        viewModelScope.launch {
            val startMs = SystemClock.elapsedRealtime()
            if (!canCreateAnotherMemory()) return@launch
            _isLoading.value = true
            _uploadProgress.value = 0f
            when (val result = recuerdosRepository.createRecuerdo(
                cofreRecuerdosId = cofreRecuerdosId,
                titulo = titulo,
                file = file,
                descripcion = descripcion,
                fecha = fecha,
                emocionId = emocionId,
                emocionPersonalizada = emocionPersonalizada,
                portadaAudio = portadaAudio,
                galleryImages = galleryImages,
                onUploadProgress = { uploadedBytes, totalBytes ->
                    if (totalBytes > 0L) {
                        _uploadProgress.value = (uploadedBytes.toFloat() / totalBytes.toFloat())
                            .coerceIn(0f, 1f)
                    }
                }
            )) {
                is ApiResult.Success -> {
                    Log.d(
                        PERF_TAG,
                        "save_recuerdo_success_ms=${SystemClock.elapsedRealtime() - startMs} " +
                            "gallery_count=${galleryImages.size}"
                    )
                    refreshCoordinator.invalidateCofre(cofreRecuerdosId)
                    _saved.emit(result.data)
                }
                is ApiResult.Error -> {
                    Log.d(
                        PERF_TAG,
                        "save_recuerdo_error_ms=${SystemClock.elapsedRealtime() - startMs} " +
                            "gallery_count=${galleryImages.size}"
                    )
                    _errorMessage.emit(result.message)
                }
                else -> { }
            }
            _isLoading.value = false
            _uploadProgress.value = null
        }
    }

    private suspend fun canCreateAnotherMemory(): Boolean {
        return when (val subResult = suscripcionRepository.getMiSuscripcion()) {
            is ApiResult.Success -> {
                val limites = subResult.data.limites
                val uso = subResult.data.uso
                val maxRecuerdos = (limites?.maxRecuerdos ?: subResult.data.plan?.maxRecuerdos)?.coerceAtLeast(0)
                val recuerdosCreados = (uso?.recuerdosCreados ?: 0).coerceAtLeast(0)
                if (maxRecuerdos != null && maxRecuerdos > 0 && recuerdosCreados >= maxRecuerdos) {
                    val planName = subResult.data.plan?.nombre?.trim().orEmpty().ifBlank { "actual" }
                    _errorMessage.emit(
                        "Alcanzaste el límite de $maxRecuerdos recuerdo(s) para tu plan $planName. " +
                            "Para seguir creando recuerdos, necesitás cambiar de plan."
                    )
                    false
                } else {
                    true
                }
            }
            else -> true
        }
    }

    fun resolveEmotionPayload(tag: EmotionalTag?): Pair<String?, String?> {
        if (tag == null) return null to null
        val normalizedTag = normalizeKey(tag.label)
        val emotionId = _emotionIdByName.value[normalizedTag]
        return if (!emotionId.isNullOrBlank()) {
            emotionId to null
        } else {
            null to tag.label
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
                    val map = result.data
                        .mapNotNull { emotion ->
                            val id = emotion.idValue().trim()
                            val name = emotion.displayName().trim()
                            if (id.isBlank() || name.isBlank()) null
                            else normalizeKey(name) to id
                        }
                        .toMap()
                    _emotionIdByName.value = map
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
