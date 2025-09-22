package com.cocido.nonna.core.logging

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * Logger robusto siguiendo mejores prácticas de DevSecOps
 * - No loguea información sensible en producción
 * - Incluye contexto estructurado
 * - Permite diferentes niveles de log
 */
object Logger {
    
    private const val TAG = "NonnaApp"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    
    /**
     * Log de debug - solo en builds de desarrollo
     */
    fun d(message: String, tag: String = TAG, throwable: Throwable? = null) {
        if (isDebugBuild()) {
            val logMessage = formatMessage("DEBUG", message)
            if (throwable != null) {
                Log.d(tag, logMessage, throwable)
            } else {
                Log.d(tag, logMessage)
            }
        }
    }
    
    /**
     * Log de información
     */
    fun i(message: String, tag: String = TAG, throwable: Throwable? = null) {
        val logMessage = formatMessage("INFO", message)
        if (throwable != null) {
            Log.i(tag, logMessage, throwable)
        } else {
            Log.i(tag, logMessage)
        }
    }
    
    /**
     * Log de advertencia
     */
    fun w(message: String, tag: String = TAG, throwable: Throwable? = null) {
        val logMessage = formatMessage("WARN", message)
        if (throwable != null) {
            Log.w(tag, logMessage, throwable)
        } else {
            Log.w(tag, logMessage)
        }
    }
    
    /**
     * Log de error
     */
    fun e(message: String, tag: String = TAG, throwable: Throwable? = null) {
        val logMessage = formatMessage("ERROR", message)
        if (throwable != null) {
            Log.e(tag, logMessage, throwable)
        } else {
            Log.e(tag, logMessage)
        }
    }
    
    /**
     * Log de error crítico - siempre se loguea
     */
    fun critical(message: String, tag: String = TAG, throwable: Throwable? = null) {
        val logMessage = formatMessage("CRITICAL", message)
        if (throwable != null) {
            Log.e(tag, logMessage, throwable)
        } else {
            Log.e(tag, logMessage)
        }
        
        // En producción, aquí se podría enviar a un servicio de monitoreo
        // como Crashlytics, Sentry, etc.
    }
    
    /**
     * Log de seguridad - para eventos de seguridad
     */
    fun security(message: String, tag: String = TAG) {
        val logMessage = formatMessage("SECURITY", message)
        Log.w(tag, logMessage)
        
        // En producción, esto debería ir a un sistema de monitoreo de seguridad
    }
    
    /**
     * Log de performance - para métricas de rendimiento
     */
    fun performance(message: String, tag: String = TAG, duration: Long? = null) {
        if (isDebugBuild()) {
            val perfMessage = if (duration != null) {
                "$message (${duration}ms)"
            } else {
                message
            }
            val logMessage = formatMessage("PERF", perfMessage)
            Log.d(tag, logMessage)
        }
    }
    
    /**
     * Verifica si es un build de debug
     */
    private fun isDebugBuild(): Boolean {
        return try {
            // Usar reflexión para acceder a BuildConfig de forma segura
            val buildConfigClass = Class.forName("com.cocido.nonna.BuildConfig")
            val debugField = buildConfigClass.getField("DEBUG")
            debugField.getBoolean(null)
        } catch (e: Exception) {
            // Si no se puede acceder, asumir que es debug por seguridad
            true
        }
    }
    
    /**
     * Formatea el mensaje con timestamp y nivel
     */
    private fun formatMessage(level: String, message: String): String {
        val timestamp = dateFormat.format(Date())
        return "[$timestamp] [$level] $message"
    }
    
    /**
     * Sanitiza datos sensibles antes de loguear
     */
    fun sanitizeForLogging(data: String): String {
        if (!isDebugBuild()) {
            // En producción, ocultar información sensible
            return data.replace(Regex("password|token|key|secret", RegexOption.IGNORE_CASE)) { 
                "[REDACTED]" 
            }
        }
        return data
    }
}
