package com.cocido.nonna.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.cocido.nonna.R
import com.cocido.nonna.ui.components.EmotionalTag
import com.cocido.nonna.ui.components.MemoryType
import com.cocido.nonna.ui.components.MemoryUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Genera un JPEG estilo polaroid / tarjeta Nonna para compartir un recuerdo como imagen
 * (en lugar de texto plano).
 */
object MemorySharePolaroidGenerator {

    private const val CANVAS_W = 1080
    private const val PHOTO_H = 780
    private const val CARD_PAD = 40f
    private const val INNER_PAD = 48f
    private const val PHOTO_CORNER = 18f
    private const val CARD_CORNER = 28f

    private val bgWarm = android.graphics.Color.parseColor("#EDE8DF")
    private val paper = android.graphics.Color.parseColor("#FFFDF8")
    private val sepia = android.graphics.Color.parseColor("#4A4238")
    private val muted = android.graphics.Color.parseColor("#756F66")
    private val caramel = android.graphics.Color.parseColor("#AE7C4B")
    private val olive = android.graphics.Color.parseColor("#8B9556")
    private val chipBg = android.graphics.Color.parseColor("#F5EFE6")

    suspend fun generate(
        context: Context,
        memory: MemoryUiModel,
        cofreContextName: String?,
        cofreCreatorDisplayName: String?,
        locale: Locale
    ): File? = withContext(Dispatchers.IO) {
        val res = context.resources
        val photoUrl = when (memory.type) {
            MemoryType.Photo ->
                memory.carouselImageUrls.firstOrNull()
                    ?: MemoryDetailShareFormatter.splitPhotoUrls(memory.thumbnailUrl).firstOrNull()
            MemoryType.Audio ->
                memory.audioCoverUrl?.takeIf { it.startsWith("http://") || it.startsWith("https://") }
            else -> null
        }

        val photoBitmap = if (photoUrl != null) {
            loadPhotoBitmap(context, photoUrl)
        } else null

        val title = memory.title.trim().ifBlank { res.getString(R.string.memory_detail_title) }
        val dateStr = MemoryDetailShareFormatter.formatDisplayDate(memory.date, locale)
        val emotion = emotionLabel(context, memory)

        val metaParts = mutableListOf<String>()
        metaParts += dateStr
        if (!cofreContextName.isNullOrBlank()) {
            metaParts += res.getString(R.string.memory_share_cofre_line, cofreContextName.trim())
        }
        if (!cofreCreatorDisplayName.isNullOrBlank()) {
            metaParts += res.getString(R.string.memory_added_by, cofreCreatorDisplayName.trim())
        }
        val metaLine = metaParts.joinToString(" · ")

        val desc = memory.description?.trim()?.takeIf { it.isNotBlank() }

        val cardW = CANVAS_W - 2 * CARD_PAD
        val photoW = cardW - 2 * INNER_PAD
        val textMaxW = (photoW - 8f).roundToInt().coerceAtLeast(320)

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = sepia
            textSize = 46f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val titleLayout = StaticLayout.Builder.obtain(title, 0, title.length, titlePaint, textMaxW)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setMaxLines(3)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setEllipsizedWidth(textMaxW)
            .build()

        val metaPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = muted
            textSize = 30f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val metaLayout = StaticLayout.Builder.obtain(metaLine, 0, metaLine.length, metaPaint, textMaxW)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setMaxLines(4)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setEllipsizedWidth(textMaxW)
            .build()

        val descLayout = if (desc != null) {
            val dp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = sepia
                textSize = 32f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            StaticLayout.Builder.obtain(desc, 0, desc.length, dp, textMaxW)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setMaxLines(7)
                .setEllipsize(TextUtils.TruncateAt.END)
                .setEllipsizedWidth(textMaxW)
                .build()
        } else null

        val emotionBlockH = if (!emotion.isNullOrBlank()) {
            val chipPadV = 14f
            val chipPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = caramel
                textSize = 28f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val emText = "${res.getString(R.string.memory_share_emotion_prefix)} $emotion"
            val chipLayout = StaticLayout.Builder.obtain(emText, 0, emText.length, chipPaint, textMaxW - 80)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setMaxLines(2)
                .build()
            chipPadV * 2 + chipLayout.height + 8f
        } else 0f

        val brandPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = caramel
            textSize = 34f
            letterSpacing = 0.25f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val subPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = muted
            textSize = 24f
        }
        val brandFm = brandPaint.fontMetrics
        val subFm = subPaint.fontMetrics
        /** Aire libre respecto al borde inferior redondeado del polaroid (texto adentro del blanco). */
        val bottomSafePad = 36f + CARD_CORNER
        val gapBrandSub = 14f
        val brandFooterReserved =
            bottomSafePad +
                (subFm.bottom - subFm.ascent) +
                gapBrandSub +
                (brandFm.bottom - brandFm.ascent) +
                20f

        val gapSmall = 20f
        val gapAfterTitle = 18f
        val gapAfterMeta = 22f
        val gapAfterEmotion = 18f

        val footerContentH = titleLayout.height + gapAfterTitle +
            metaLayout.height + gapAfterMeta +
            emotionBlockH + (if (emotionBlockH > 0f) gapAfterEmotion else 0f) +
            (descLayout?.let { it.height + gapSmall } ?: 0f)

        val cardH = INNER_PAD + PHOTO_H + INNER_PAD + footerContentH + brandFooterReserved
        val canvasH = (CARD_PAD + cardH + CARD_PAD).roundToInt().coerceAtLeast(1200)

        val bitmap = Bitmap.createBitmap(CANVAS_W, canvasH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(bgWarm)

        val cardLeft = CARD_PAD
        val cardTop = CARD_PAD
        val cardRect = RectF(cardLeft, cardTop, cardLeft + cardW, cardTop + cardH)

        // Sombra suave
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x33000000 }
        canvas.drawRoundRect(cardRect.left + 6f, cardTop + 10f, cardRect.right + 6f, cardRect.bottom + 10f, CARD_CORNER, CARD_CORNER, shadowPaint)

        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = paper }
        canvas.drawRoundRect(cardRect, CARD_CORNER, CARD_CORNER, cardPaint)

        val photoLeft = cardLeft + INNER_PAD
        val photoTop = cardTop + INNER_PAD
        val photoRect = RectF(photoLeft, photoTop, photoLeft + photoW, photoTop + PHOTO_H)

        if (photoBitmap != null) {
            drawPhotoCenterCrop(canvas, photoBitmap, photoRect, PHOTO_CORNER)
        } else {
            drawPlaceholder(canvas, memory, photoRect, res)
        }

        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            color = 0x284A4238
        }
        canvas.drawRoundRect(photoRect, PHOTO_CORNER, PHOTO_CORNER, stroke)

        var textY = photoRect.bottom + INNER_PAD

        canvas.save()
        canvas.translate(photoLeft + 4f, textY)
        titleLayout.draw(canvas)
        canvas.restore()
        textY += titleLayout.height + gapAfterTitle

        canvas.save()
        canvas.translate(photoLeft + 4f, textY)
        metaLayout.draw(canvas)
        canvas.restore()
        textY += metaLayout.height + gapAfterMeta

        if (!emotion.isNullOrBlank()) {
            val chipPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = caramel
                textSize = 28f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val emText = "${res.getString(R.string.memory_share_emotion_prefix)} $emotion"
            val chipLayout = StaticLayout.Builder.obtain(emText, 0, emText.length, chipPaint, textMaxW - 80)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setMaxLines(2)
                .build()
            val chipH = chipLayout.height + 28f
            var maxLine = 120f
            for (i in 0 until chipLayout.lineCount) {
                maxLine = max(maxLine, chipLayout.getLineWidth(i))
            }
            val chipW = (maxLine + 44f).coerceAtMost(photoW - 8f)
            val chipRect = RectF(photoLeft + 4f, textY, photoLeft + 4f + chipW, textY + chipH)
            val chipFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = chipBg }
            canvas.drawRoundRect(chipRect, 20f, 20f, chipFill)
            canvas.save()
            canvas.translate(chipRect.left + 22f, chipRect.top + 14f)
            chipLayout.draw(canvas)
            canvas.restore()
            textY = chipRect.bottom + gapAfterEmotion
        }

        if (descLayout != null) {
            canvas.save()
            canvas.translate(photoLeft + 4f, textY)
            descLayout.draw(canvas)
            canvas.restore()
            textY += descLayout.height + gapSmall
        }

        val centerX = (cardRect.left + cardRect.right) / 2f
        val brand = "NONNA"
        val bw = brandPaint.measureText(brand)
        val sub = res.getString(R.string.memory_share_footer)
        val sw = subPaint.measureText(sub)
        // Subtítulo abajo del todo (cerca del borde interno); NONNA arriba del subtítulo.
        val subBaseline = cardRect.bottom - bottomSafePad - subFm.bottom
        val brandBaseline = subBaseline + subFm.ascent - gapBrandSub - brandFm.descent
        canvas.drawText(brand, centerX - bw / 2f, brandBaseline, brandPaint)
        canvas.drawText(sub, centerX - sw / 2f, subBaseline, subPaint)

        val dir = File(context.cacheDir, "shared_images").apply { mkdirs() }
        val out = File(dir, "memory_share_${memory.id}_${System.currentTimeMillis()}.jpg")
        return@withContext try {
            var ok = false
            FileOutputStream(out).use { os ->
                ok = bitmap.compress(Bitmap.CompressFormat.JPEG, 92, os)
            }
            if (!ok) {
                out.delete()
                null
            } else {
                out.takeIf { it.exists() && it.length() > 0L }
            }
        } catch (_: Exception) {
            runCatching { out.delete() }
            null
        } finally {
            if (photoBitmap != null && !photoBitmap.isRecycled) photoBitmap.recycle()
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }

    private suspend fun loadPhotoBitmap(context: Context, url: String): Bitmap? {
        val loader = context.imageLoader
        val request = ImageRequest.Builder(context)
            .data(url)
            .size(1200)
            .allowHardware(false)
            .build()
        val result = loader.execute(request)
        if (result !is SuccessResult) return null
        return runCatching { result.drawable.toBitmap() }.getOrNull()
    }

    private fun drawPhotoCenterCrop(canvas: Canvas, bmp: Bitmap, dst: RectF, corner: Float) {
        val path = Path().apply { addRoundRect(dst, corner, corner, Path.Direction.CW) }
        canvas.save()
        canvas.clipPath(path)
        val scale = max(dst.width() / bmp.width, dst.height() / bmp.height)
        val matrix = Matrix()
        matrix.postScale(scale, scale)
        val dx = dst.left + (dst.width() - bmp.width * scale) / 2f
        val dy = dst.top + (dst.height() - bmp.height * scale) / 2f
        matrix.postTranslate(dx, dy)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
        canvas.drawBitmap(bmp, matrix, p)
        canvas.restore()
    }

    private fun drawPlaceholder(canvas: Canvas, memory: MemoryUiModel, rect: RectF, res: android.content.res.Resources) {
        val grad = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                rect.left, rect.top, rect.left, rect.bottom,
                caramel, olive,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, PHOTO_CORNER, PHOTO_CORNER, grad)
        grad.shader = null

        val label = when (memory.type) {
            MemoryType.Audio -> res.getString(R.string.memory_detail_media_audio)
            MemoryType.Text -> res.getString(R.string.memory_detail_media_text)
            else -> res.getString(R.string.memory_detail_media_photo)
        }
        val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 44f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            alpha = 245
        }
        val lw = (rect.width().roundToInt() - 80).coerceAtLeast(160)
        val layout = StaticLayout.Builder.obtain(label, 0, label.length, tp, lw)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setMaxLines(2)
            .build()
        canvas.save()
        canvas.translate(rect.centerX() - layout.width / 2f, rect.centerY() - layout.height / 2f)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun emotionLabel(context: Context, memory: MemoryUiModel): String? {
        val fromTag = memory.emotionalTag?.let { tag ->
            context.getString(
                when (tag) {
                    EmotionalTag.Alegre -> R.string.emotion_alegre
                    EmotionalTag.Nostalgico -> R.string.emotion_nostalgico
                    EmotionalTag.Calmo -> R.string.emotion_calmo
                    EmotionalTag.Familiar -> R.string.emotion_familiar
                }
            )
        }
        val custom = memory.emotionalCustomLabel?.trim()?.takeIf { it.isNotBlank() }
        return custom ?: fromTag
    }
}
