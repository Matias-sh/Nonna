package com.cocido.nonna.util

import com.cocido.nonna.ui.components.EmotionalTag
import java.text.Normalizer

object EmotionPayloadResolver {

    fun normalizeKey(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
            .trim()

    fun lookupEmotionId(tag: EmotionalTag, emotionIdByName: Map<String, String>): String? {
        return lookupKeys(tag).firstNotNullOfOrNull { key -> emotionIdByName[key] }
    }

    fun lookupKeys(tag: EmotionalTag): List<String> {
        val labelKey = normalizeKey(tag.label)
        val aliases = when (tag) {
            EmotionalTag.Alegre -> listOf("alegre", "alegria", "alegría")
            EmotionalTag.Nostalgico -> listOf("nostalgico", "nostálgico", "nostalgia")
            EmotionalTag.Calmo -> listOf("calmo", "calma")
            EmotionalTag.Familiar -> listOf("familiar")
        }.map(::normalizeKey)
        return (listOf(labelKey) + aliases).distinct()
    }

    /** Devuelve (emocionId, emocionPersonalizada) para multipart. */
    fun buildPayload(
        emotionalTag: EmotionalTag?,
        customEmotion: String?,
        emotionIdByName: Map<String, String>
    ): Pair<String?, String?> {
        val custom = customEmotion?.trim().takeUnless { it.isNullOrBlank() }
        if (custom != null) return null to custom
        if (emotionalTag == null) return null to null
        val id = lookupEmotionId(emotionalTag, emotionIdByName)
        return if (id != null) {
            id to null
        } else {
            null to emotionalTag.label
        }
    }
}
