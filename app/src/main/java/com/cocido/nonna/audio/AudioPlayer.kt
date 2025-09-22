package com.cocido.nonna.audio

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

/**
 * Clase para reproducir archivos de audio
 * Maneja la reproducción, pausa, seek y estados del reproductor
 */
class AudioPlayer(private val context: Context) {
    
    private var mediaPlayer: MediaPlayer? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    fun loadAudio(audioPath: String) {
        try {
            release()
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioPath)
                prepare()
                
                _duration.value = duration.toLong()
                
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPosition.value = 0L
                }
                
                setOnErrorListener { _, _, _ ->
                    _isPlaying.value = false
                    false
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    
    fun play() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying) {
                player.start()
                _isPlaying.value = true
                startPositionUpdates()
            }
        }
    }
    
    fun pause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
            }
        }
    }
    
    fun seekToProgress(progress: Int) {
        mediaPlayer?.let { player ->
            val duration = player.duration
            val position = (duration * progress / 100).toInt()
            player.seekTo(position)
            _currentPosition.value = position.toLong()
        }
    }
    
    fun getCurrentProgress(): Int {
        return mediaPlayer?.let { player ->
            val duration = player.duration
            val position = player.currentPosition
            if (duration > 0) {
                (position * 100 / duration)
            } else {
                0
            }
        } ?: 0
    }
    
    fun formatTime(timeMs: Long): String {
        val totalSeconds = (timeMs / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    private fun startPositionUpdates() {
        // En una implementación real, esto se haría con un timer
        // Por simplicidad, solo actualizamos cuando se llama explícitamente
    }
    
    fun release() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
        }
        mediaPlayer = null
        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
    }
}