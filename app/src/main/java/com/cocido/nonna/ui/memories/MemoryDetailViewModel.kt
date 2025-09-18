package com.cocido.nonna.ui.memories

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.audio.AudioPlayer
import com.cocido.nonna.data.repository.MemoryRepositoryImpl
import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.MemoryId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para el detalle de un recuerdo
 * Maneja la carga del recuerdo y la reproducción de audio
 */
@HiltViewModel
class MemoryDetailViewModel @Inject constructor(
    private val memoryRepository: MemoryRepositoryImpl
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<MemoryDetailUiState>(MemoryDetailUiState.Loading)
    val uiState: StateFlow<MemoryDetailUiState> = _uiState.asStateFlow()
    
    private var currentMemory: Memory? = null
    private var audioPlayer: AudioPlayer? = null
    
    fun loadMemory(memoryId: String) {
        viewModelScope.launch {
            _uiState.value = MemoryDetailUiState.Loading
            
            try {
                val memory = memoryRepository.getMemoryById(MemoryId(memoryId))
                if (memory != null) {
                    currentMemory = memory
                    _uiState.value = MemoryDetailUiState.Success(memory = memory)
                } else {
                    _uiState.value = MemoryDetailUiState.Error(
                        message = "Recuerdo no encontrado"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = MemoryDetailUiState.Error(
                    message = e.message ?: "Error al cargar el recuerdo"
                )
            }
        }
    }
    
    fun initializeAudioPlayer(context: Context) {
        audioPlayer = AudioPlayer(context)
        observeAudioPlayer()
    }
    
    fun togglePlayback() {
        val memory = currentMemory ?: return
        val audioPath = memory.audioLocalPath ?: memory.audioRemoteUrl ?: return
        
        audioPlayer?.let { player ->
            if (player.isPlaying.value) {
                player.pause()
            } else {
                // Siempre cargar el audio antes de reproducir
                player.loadAudio(audioPath)
                player.play()
            }
        }
    }
    
    fun seekTo(progress: Int) {
        audioPlayer?.seekToProgress(progress)
    }
    
    private fun observeAudioPlayer() {
        viewModelScope.launch {
            audioPlayer?.let { player ->
                // Observar estado de reproducción
                player.isPlaying.collect { isPlaying ->
                    updatePlaybackState(isPlaying)
                }
            }
        }
        
        viewModelScope.launch {
            audioPlayer?.let { player ->
                // Observar posición actual
                player.currentPosition.collect { position ->
                    updateCurrentPosition(position)
                }
            }
        }
    }
    
    private fun updatePlaybackState(isPlaying: Boolean) {
        val currentState = _uiState.value
        if (currentState is MemoryDetailUiState.Playing) {
            _uiState.value = currentState.copy(isPlaying = isPlaying)
        }
    }
    
    private fun updateCurrentPosition(position: Long) {
        audioPlayer?.let { player ->
            val duration = player.duration.value
            val progress = player.getCurrentProgress()
            val currentTimeFormatted = player.formatTime(position)
            val totalTimeFormatted = player.formatTime(duration)
            
            _uiState.value = MemoryDetailUiState.Playing(
                isPlaying = player.isPlaying.value,
                currentTime = currentTimeFormatted,
                totalTime = totalTimeFormatted,
                progress = progress,
                maxProgress = 100
            )
        }
    }
    
    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
    
    override fun onCleared() {
        super.onCleared()
        audioPlayer?.release()
    }
}

/**
 * Estados de la UI para el detalle de recuerdo
 */
sealed class MemoryDetailUiState {
    object Loading : MemoryDetailUiState()
    data class Success(val memory: Memory) : MemoryDetailUiState()
    data class Error(val message: String) : MemoryDetailUiState()
    data class Playing(
        val isPlaying: Boolean,
        val currentTime: String,
        val totalTime: String,
        val progress: Int,
        val maxProgress: Int
    ) : MemoryDetailUiState()
}

