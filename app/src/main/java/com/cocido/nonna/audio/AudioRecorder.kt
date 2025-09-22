package com.cocido.nonna.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

/**
 * Clase para grabar audio
 * Maneja la grabación de audio con MediaRecorder
 */
class AudioRecorder(private val context: Context) {
    
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false
    
    fun startRecording(): Boolean {
        return try {
            setupMediaRecorder()
            mediaRecorder?.start()
            isRecording = true
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
    
    fun stopRecording(): String? {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            
            outputFile?.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun isRecording(): Boolean = isRecording
    
    private fun setupMediaRecorder() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            
            // Crear archivo de salida
            outputFile = createOutputFile()
            setOutputFile(outputFile!!.absolutePath)
            
            prepare()
        }
    }
    
    private fun createOutputFile(): File {
        val timeStamp = System.currentTimeMillis()
        val fileName = "audio_recording_$timeStamp.m4a"
        val storageDir = File(context.getExternalFilesDir(null), "Recordings")
        
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        return File(storageDir, fileName)
    }
    
    fun release() {
        mediaRecorder?.release()
        mediaRecorder = null
        isRecording = false
    }
}