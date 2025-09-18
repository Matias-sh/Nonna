package com.cocido.nonna.domain.model

/**
 * Tipos de recuerdos sensoriales que se pueden crear en Nonna
 */
enum class MemoryType {
    PHOTO_WITH_AUDIO,  // Foto con narración de voz
    PHOTO_ONLY,        // Solo foto
    AUDIO_ONLY,        // Solo audio/narración
    RECIPE,            // Receta familiar
    NOTE               // Nota o texto
}

