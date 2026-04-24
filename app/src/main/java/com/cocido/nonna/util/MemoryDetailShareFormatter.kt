package com.cocido.nonna.util

import android.content.Context
import com.cocido.nonna.R
import com.cocido.nonna.ui.components.EmotionalTag
import com.cocido.nonna.ui.components.MemoryType
import com.cocido.nonna.ui.components.MemoryUiModel
import java.text.SimpleDateFormat
import java.util.Locale

object MemoryDetailShareFormatter {

    /** URLs de fotos cuando el backend envía varias separadas por coma o salto de línea. */
    fun splitPhotoUrls(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        val separatorsNormalized = raw
            .replace(";", ",")
            .replace("\n", ",")
        return separatorsNormalized
            .split(",")
            .map { it.trim() }
            .filter { it.startsWith("http://") || it.startsWith("https://") }
            .distinct()
    }

    fun formatDisplayDate(raw: String, locale: Locale): String {
        if (raw.isBlank()) return raw
        return runCatching {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(raw.trim()) ?: return raw
            SimpleDateFormat("d MMM yyyy", locale).format(parsed)
        }.getOrDefault(raw)
    }

    fun mediaKindLabel(context: Context, type: MemoryType): String {
        val res = context.resources
        return when (type) {
            MemoryType.Photo -> res.getString(R.string.memory_detail_media_photo)
            MemoryType.Audio -> res.getString(R.string.memory_detail_media_audio)
            MemoryType.Text -> res.getString(R.string.memory_detail_media_text)
        }
    }

    fun buildShareMessage(
        context: Context,
        memory: MemoryUiModel,
        cofreContextName: String?,
        cofreCreatorDisplayName: String? = null,
        locale: Locale = Locale.getDefault()
    ): String {
        val res = context.resources
        val lines = mutableListOf<String>()
        lines += "━━━━━━━━━━━━━━━━"
        lines += res.getString(R.string.memory_share_brand_line)
        lines += "━━━━━━━━━━━━━━━━"
        lines += ""
        lines += "${res.getString(R.string.memory_share_type_prefix)} ${mediaKindLabel(context, memory.type)}"
        lines += memory.title.trim().ifBlank { res.getString(R.string.memory_detail_title) }
        if (!cofreContextName.isNullOrBlank()) {
            lines += res.getString(R.string.memory_share_cofre_line, cofreContextName.trim())
        }
        lines += "${res.getString(R.string.memory_share_date_prefix)} ${formatDisplayDate(memory.date, locale)}"
        if (!cofreCreatorDisplayName.isNullOrBlank()) {
            lines += res.getString(R.string.memory_added_by, cofreCreatorDisplayName.trim())
        }
        val emotion = memory.emotionalTag?.let { tag ->
            context.getString(
                when (tag) {
                    EmotionalTag.Alegre -> R.string.emotion_alegre
                    EmotionalTag.Nostalgico -> R.string.emotion_nostalgico
                    EmotionalTag.Calmo -> R.string.emotion_calmo
                    EmotionalTag.Familiar -> R.string.emotion_familiar
                }
            )
        } ?: memory.emotionalCustomLabel?.trim()?.takeIf { it.isNotBlank() }
        if (!emotion.isNullOrBlank()) {
            lines += "${res.getString(R.string.memory_share_emotion_prefix)} $emotion"
        }
        val desc = memory.description?.trim()?.takeIf { it.isNotBlank() }
        if (desc != null) {
            lines += ""
            lines += desc
        }
        if (memory.type == MemoryType.Photo) {
            val url = memory.thumbnailUrl?.trim()?.takeIf {
                it.startsWith("http://", true) || it.startsWith("https://", true)
            }
            if (url != null) {
                lines += ""
                lines += res.getString(R.string.memory_share_link_line)
                lines += url
            }
        }
        lines += ""
        lines += "━━━━━━━━━━━━━━━━"
        lines += res.getString(R.string.memory_share_footer)
        lines += "━━━━━━━━━━━━━━━━"
        return lines.joinToString("\n")
    }
}
