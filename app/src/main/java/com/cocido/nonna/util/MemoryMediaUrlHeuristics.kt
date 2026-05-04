package com.cocido.nonna.util

/**
 * Heurísticas para URLs de medios en recuerdos (CDN/R2 sin extensión, multipart, etc.).
 */
object MemoryMediaUrlHeuristics {

    fun isHttpUrl(url: String?): Boolean {
        val s = url?.trim() ?: return false
        return s.startsWith("http://", ignoreCase = true) ||
            s.startsWith("https://", ignoreCase = true)
    }

    fun looksLikeAudioFileUrl(url: String): Boolean {
        val path = url.substringBefore('?').substringBefore('#').lowercase()
        return path.endsWith(".m4a") || path.endsWith(".mp3") || path.endsWith(".ogg") ||
            path.endsWith(".wav") || path.endsWith(".aac") || path.endsWith(".opus")
    }

    fun looksLikePlainTextFileUrl(url: String): Boolean {
        val path = url.substringBefore('?').substringBefore('#').lowercase()
        return path.endsWith(".txt")
    }

    /**
     * URL que puede intentarse como imagen en Coil (portada de tarjeta o carátula de audio).
     * Excluye audio y .txt; si es https sin extensión típica, se acepta (URLs firmadas).
     */
    fun isDisplayableImageUrl(url: String): Boolean {
        if (!isHttpUrl(url)) return false
        if (looksLikeAudioFileUrl(url)) return false
        if (looksLikePlainTextFileUrl(url)) return false
        val path = url.substringBefore('?').substringBefore('#').lowercase()
        if (path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png") ||
            path.endsWith(".webp") || path.endsWith(".gif") || path.endsWith(".bmp")
        ) {
            return true
        }
        if (path.endsWith(".pdf") || path.endsWith(".zip")) return false
        return true
    }
}
