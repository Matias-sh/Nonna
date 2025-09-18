package com.cocido.nonna.ui.memories

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.audio.AudioRecorder
import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.MemoryId
import com.cocido.nonna.domain.model.MemoryType
import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.domain.usecase.CreateMemoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * ViewModel para crear nuevos recuerdos sensoriales
 * Maneja la captura de foto, grabación de audio y guardado de metadatos
 */
@HiltViewModel
class CreateMemoryViewModel @Inject constructor(
    private val createMemoryUseCase: CreateMemoryUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<CreateMemoryUiState>(CreateMemoryUiState.Idle)
    val uiState: StateFlow<CreateMemoryUiState> = _uiState.asStateFlow()
    
    private var photoUri: Uri? = null
    private var audioPath: String? = null
    private var audioRecorder: AudioRecorder? = null
    private var recordingStartTime: Long = 0
    
    fun setPhotoUri(uri: Uri) {
        photoUri = uri
    }
    
    fun createPhotoFile(context: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = File(context.getExternalFilesDir(null), "Pictures")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        return Uri.fromFile(image)
    }
    
    fun startRecording(context: Context) {
        viewModelScope.launch {
            try {
                audioRecorder = AudioRecorder(context)
                val success = audioRecorder?.startRecording() ?: false
                
                if (success) {
                    recordingStartTime = System.currentTimeMillis()
                    
                    // Actualizar duración cada segundo
                    while (audioRecorder?.isRecording() == true) {
                        val duration = ((System.currentTimeMillis() - recordingStartTime) / 1000).toInt()
                        _uiState.value = CreateMemoryUiState.Recording(
                            duration = formatDuration(duration)
                        )
                        kotlinx.coroutines.delay(1000)
                    }
                } else {
                    _uiState.value = CreateMemoryUiState.Error(
                        message = "Error al iniciar la grabación"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = CreateMemoryUiState.Error(
                    message = "Error al iniciar la grabación: ${e.message}"
                )
            }
        }
    }
    
    fun stopRecording() {
        audioPath = audioRecorder?.stopRecording()
        audioRecorder?.release()
        audioRecorder = null
        
        _uiState.value = CreateMemoryUiState.Idle
    }
    
    fun saveMemory(title: String, date: String, tags: List<String>) {
        viewModelScope.launch {
            _uiState.value = CreateMemoryUiState.Loading
            
            try {
                // TODO: Obtener vaultId del usuario autenticado
                val currentVaultId = VaultId("vault_1") // Temporal
                
                val memory = Memory(
                    id = MemoryId(UUID.randomUUID().toString()),
                    vaultId = currentVaultId,
                    title = title,
                    type = determineMemoryType(),
                    photoLocalPath = photoUri?.path,
                    photoRemoteUrl = null,
                    audioLocalPath = audioPath,
                    audioRemoteUrl = null,
                    hasTranscript = false,
                    transcript = null,
                    people = emptyList(),
                    tags = tags,
                    dateTaken = parseDate(date),
                    location = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                val result = createMemoryUseCase(memory)
                result.fold(
                    onSuccess = { memoryId ->
                        _uiState.value = CreateMemoryUiState.Success
                    },
                    onFailure = { error ->
                        _uiState.value = CreateMemoryUiState.Error(
                            message = error.message ?: "Error al guardar el recuerdo"
                        )
                    }
                )
                
            } catch (e: Exception) {
                _uiState.value = CreateMemoryUiState.Error(
                    message = e.message ?: "Error al guardar el recuerdo"
                )
            }
        }
    }
    
    private fun determineMemoryType(): MemoryType {
        return when {
            photoUri != null && audioPath != null -> MemoryType.PHOTO_WITH_AUDIO
            photoUri != null -> MemoryType.PHOTO_ONLY
            audioPath != null -> MemoryType.AUDIO_ONLY
            else -> MemoryType.NOTE
        }
    }
    
    private fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
    
    private fun parseDate(dateString: String): Long? {
        return try {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formatter.parse(dateString)?.time
        } catch (e: Exception) {
            null
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        audioRecorder?.release()
    }
}

/**
 * Estados de la UI para crear recuerdos
 */
sealed class CreateMemoryUiState {
    object Idle : CreateMemoryUiState()
    object Loading : CreateMemoryUiState()
    object Success : CreateMemoryUiState()
    data class Error(val message: String) : CreateMemoryUiState()
    data class Recording(val duration: String) : CreateMemoryUiState()
}

