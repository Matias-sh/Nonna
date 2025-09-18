package com.cocido.nonna.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

/**
 * Wrapper para MediaRecorder para grabación de audio
 * Configurado para AAC, 44.1kHz, 96kbps, mono según especificaciones
 */
class AudioRecorder(private val context: Context) {
    
    private var mediaRecorder: MediaRecorder? = null
    private var outputPath: String? = null
    private var isRecording = false
    
    /**
     * Inicia la grabación de audio
     * @return true si se inició correctamente, false en caso contrario
     */
    fun startRecording(): Boolean {
        return try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(96000)
                setAudioChannels(1) // Mono
                
                // Crear archivo de salida
                outputPath = createAudioFile()
                setOutputFile(outputPath)
                
                prepare()
                start()
            }
            
            isRecording = true
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Detiene la grabación de audio
     * @return la ruta del archivo grabado o null si hubo error
     */
    fun stopRecording(): String? {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            outputPath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Cancela la grabación y elimina el archivo
     */
    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            
            // Eliminar archivo si existe
            outputPath?.let { path ->
                File(path).delete()
            }
            outputPath = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Verifica si está grabando
     */
    fun isRecording(): Boolean = isRecording
    
    /**
     * Obtiene la ruta del archivo de audio
     */
    fun getOutputPath(): String? = outputPath
    
    /**
     * Crea un archivo de audio con nombre único
     */
    private fun createAudioFile(): String {
        val timeStamp = System.currentTimeMillis()
        val audioFileName = "AUDIO_${timeStamp}.m4a"
        val audioDir = File(context.getExternalFilesDir(null), "Audio")
        
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }
        
        return File(audioDir, audioFileName).absolutePath
    }
    
    /**
     * Libera recursos
     */
    fun release() {
        try {
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

