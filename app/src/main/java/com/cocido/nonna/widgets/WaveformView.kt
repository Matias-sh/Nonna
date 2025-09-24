package com.cocido.nonna.widgets

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

/**
 * View personalizada para mostrar la forma de onda del audio
 * Renderiza picos de amplitud como barras verticales
 */
class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var waveformData = IntArray(0)
    private var maxAmplitude = 0
    private var isRecording = false
    
    // Colores
    private val waveformColor = Color.parseColor("#FF8B4513") // Marrón cálido
    private val recordingColor = Color.parseColor("#FFDAA520") // Dorado
    private val backgroundColor = Color.parseColor("#FFF5F5DC") // Crema
    
    init {
        setupPaints()
    }
    
    private fun setupPaints() {
        paint.apply {
            color = waveformColor
            strokeWidth = 4f
            style = Paint.Style.FILL
        }
        
        backgroundPaint.apply {
            color = backgroundColor
            style = Paint.Style.FILL
        }
    }
    
    /**
     * Actualiza los datos de la forma de onda
     * @param data Array de amplitudes (0-100)
     */
    fun updateWaveform(data: IntArray) {
        waveformData = data
        maxAmplitude = data.maxOrNull() ?: 1
        invalidate()
    }
    
    /**
     * Agrega un nuevo pico de amplitud
     * @param amplitude Amplitud del pico (0-100)
     */
    fun addAmplitude(amplitude: Int) {
        val newData = waveformData + amplitude
        updateWaveform(newData)
    }
    
    /**
     * Establece el estado de grabación
     * @param recording true si está grabando, false en caso contrario
     */
    fun setRecording(recording: Boolean) {
        isRecording = recording
        paint.color = if (recording) recordingColor else waveformColor
        invalidate()
    }
    
    /**
     * Limpia la forma de onda
     */
    fun clear() {
        waveformData = IntArray(0)
        maxAmplitude = 1
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val width = width.toFloat()
        val height = height.toFloat()
        
        // Dibujar fondo
        canvas.drawRect(0f, 0f, width, height, backgroundPaint)
        
        if (waveformData.isEmpty()) {
            // Dibujar línea central cuando no hay datos
            paint.strokeWidth = 2f
            paint.color = Color.parseColor("#FFBDBDBD")
            canvas.drawLine(0f, height / 2, width, height / 2, paint)
            paint.strokeWidth = 4f
            paint.color = if (isRecording) recordingColor else waveformColor
            return
        }
        
        // Calcular ancho de cada barra
        val barWidth = width / waveformData.size
        val centerY = height / 2
        
        // Dibujar barras de la forma de onda
        waveformData.forEachIndexed { index, amplitude ->
            val barHeight = (amplitude.toFloat() / maxAmplitude) * (height * 0.8f)
            val x = index * barWidth
            val top = centerY - barHeight / 2
            val bottom = centerY + barHeight / 2
            
            canvas.drawRect(
                x + barWidth * 0.1f,
                top,
                x + barWidth * 0.9f,
                bottom,
                paint
            )
        }
        
        // Dibujar línea central
        paint.strokeWidth = 1f
        paint.color = Color.parseColor("#FFBDBDBD")
        canvas.drawLine(0f, centerY, width, centerY, paint)
        paint.strokeWidth = 4f
        paint.color = if (isRecording) recordingColor else waveformColor
    }
    
    /**
     * Genera datos de forma de onda basados en audio real
     */
    fun generateFromAudioData(audioData: FloatArray) {
        val waveformData = IntArray(audioData.size) { index ->
            (audioData[index] * 100).toInt().coerceIn(0, 100)
        }
        updateWaveform(waveformData)
    }
}

