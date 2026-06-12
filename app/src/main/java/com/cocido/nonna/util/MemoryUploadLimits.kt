package com.cocido.nonna.util

import com.cocido.nonna.ui.components.MemoryType
import java.util.Locale

object MemoryUploadLimits {
    /** Fotos extra permitidas en multipart `imagenesGaleria` (Swagger: órdenes 1 y 2). */
    const val MAX_CAROUSEL_GALLERY_IMAGES = 2

    /** Total de fotos por recuerdo imagen que acepta el backend (principal + galería). */
    const val MAX_PHOTO_FILES_PER_MEMORY = 1 + MAX_CAROUSEL_GALLERY_IMAGES

    const val IMAGE_MAX_BYTES: Long = 1L * 1024L * 1024L
    const val AUDIO_MAX_BYTES: Long = 10L * 1024L * 1024L
    const val TEXT_MAX_BYTES: Long = 256L * 1024L

    fun maxPhotoFilesForPlan(planMaxArchivos: Int): Int =
        minOf(planMaxArchivos.coerceAtLeast(1), MAX_PHOTO_FILES_PER_MEMORY)

    fun maxBytesFor(type: MemoryType): Long = when (type) {
        MemoryType.Photo -> IMAGE_MAX_BYTES
        MemoryType.Audio -> AUDIO_MAX_BYTES
        MemoryType.Text -> TEXT_MAX_BYTES
    }

    fun typeLabel(type: MemoryType): String = when (type) {
        MemoryType.Photo -> "imagen"
        MemoryType.Audio -> "audio"
        MemoryType.Text -> "texto"
    }

    fun exceededMessage(type: MemoryType, maxBytes: Long = maxBytesFor(type)): String {
        return "Superaste el tamaño máximo de archivo para ${typeLabel(type)}. El máximo es: ${formatBytes(maxBytes)}."
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = 1024.0
        val mb = kb * 1024.0
        return when {
            bytes >= mb -> String.format(Locale.US, "%.1f MB", bytes / mb)
            bytes >= kb -> String.format(Locale.US, "%.0f KB", bytes / kb)
            else -> "$bytes B"
        }
    }
}
