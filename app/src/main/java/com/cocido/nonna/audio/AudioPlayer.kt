package com.cocido.nonna.audio

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Wrapper para ExoPlayer para reproducción de audio
 * Maneja la reproducción de archivos de audio locales y remotos
 */
class AudioPlayer(private val context: Context) {
    
    private var exoPlayer: ExoPlayer? = null
    private var currentMediaItem: MediaItem? = null
    
    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    init {
        initializePlayer()
    }
    
    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_IDLE -> {
                            _playbackState.value = PlaybackState.IDLE
                            _isPlaying.value = false
                        }
                        Player.STATE_BUFFERING -> {
                            _playbackState.value = PlaybackState.BUFFERING
                        }
                        Player.STATE_READY -> {
                            _playbackState.value = PlaybackState.READY
                            _duration.value = this@apply.duration
                        }
                        Player.STATE_ENDED -> {
                            _playbackState.value = PlaybackState.ENDED
                            _isPlaying.value = false
                        }
                    }
                }
                
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }
            })
        }
    }
    
    /**
     * Carga un archivo de audio para reproducción
     * @param audioPath Ruta del archivo de audio (local o URL)
     */
    fun loadAudio(audioPath: String) {
        val mediaItem = MediaItem.fromUri(audioPath)
        currentMediaItem = mediaItem
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
    }
    
    /**
     * Inicia la reproducción
     */
    fun play() {
        exoPlayer?.play()
    }
    
    /**
     * Pausa la reproducción
     */
    fun pause() {
        exoPlayer?.pause()
    }
    
    /**
     * Alterna entre reproducir y pausar
     */
    fun togglePlayback() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }
    
    /**
     * Busca a una posición específica
     * @param position Posición en milisegundos
     */
    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }
    
    /**
     * Busca a una posición específica por porcentaje
     * @param progress Porcentaje (0-100)
     */
    fun seekToProgress(progress: Int) {
        val duration = _duration.value
        if (duration > 0) {
            val position = (duration * progress) / 100
            seekTo(position)
        }
    }
    
    /**
     * Detiene la reproducción y resetea la posición
     */
    fun stop() {
        exoPlayer?.stop()
        _currentPosition.value = 0L
    }
    
    /**
     * Actualiza la posición actual (debe ser llamado periódicamente)
     */
    fun updateCurrentPosition() {
        exoPlayer?.let { player ->
            _currentPosition.value = player.currentPosition
        }
    }
    
    /**
     * Formatea el tiempo en formato MM:SS
     */
    fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    /**
     * Obtiene el progreso actual como porcentaje
     */
    fun getCurrentProgress(): Int {
        val duration = _duration.value
        val position = _currentPosition.value
        return if (duration > 0) {
            ((position * 100) / duration).toInt()
        } else {
            0
        }
    }
    
    /**
     * Libera los recursos del reproductor
     */
    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
}

/**
 * Estados de reproducción
 */
enum class PlaybackState {
    IDLE,
    BUFFERING,
    READY,
    ENDED,
    ERROR
}

