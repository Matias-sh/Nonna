package com.cocido.nonna.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Comprime una imagen para subida, manteniendo el tamaño por debajo de [maxBytes].
 * Aplica orientación EXIF antes de comprimir para evitar fotos giradas.
 */
object ImageCompressor {

    private const val MAX_LONG_EDGE = 1920
    private const val DEFAULT_JPEG_QUALITY = 85
    private const val MIN_JPEG_QUALITY = 50
    private const val MIN_BITMAP_EDGE = 320
    private const val DOWNSCALE_FACTOR = 0.8f

    fun compressForUpload(
        context: Context,
        uri: Uri,
        maxBytes: Long = 1024 * 1024,
        maxLongEdge: Int? = MAX_LONG_EDGE
    ): File? {
        return context.contentResolver.openInputStream(uri)?.use { input ->
            compressStreamToFile(context, input, maxBytes, maxLongEdge)
        }
    }

    fun compressForUpload(
        context: Context,
        file: File,
        maxBytes: Long = 1024 * 1024,
        maxLongEdge: Int? = MAX_LONG_EDGE
    ): File? {
        return file.inputStream().use { input ->
            compressStreamToFile(context, input, maxBytes, maxLongEdge)
        }
    }

    private fun compressStreamToFile(
        context: Context,
        input: InputStream,
        maxBytes: Long,
        maxLongEdge: Int?
    ): File? {
        val bytes = input.readBytes()
        if (bytes.isEmpty()) return null

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        val w = options.outWidth
        val h = options.outHeight
        if (w <= 0 || h <= 0) return null

        val sampleSize = computeSampleSize(w, h, maxLongEdge)
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        var workingBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return null

        workingBitmap = applyExifOrientation(bytes, workingBitmap)
        workingBitmap = scaleToMaxEdge(workingBitmap, maxLongEdge)

        val outFile = File.createTempFile("recuerdo_compressed", ".jpg", context.cacheDir)
        while (true) {
            var quality = DEFAULT_JPEG_QUALITY
            var written = false
            while (quality >= MIN_JPEG_QUALITY) {
                FileOutputStream(outFile).use { out ->
                    written = workingBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                }
                if (written && outFile.length() <= maxBytes) {
                    workingBitmap.recycle()
                    return outFile
                }
                quality -= 15
            }

            if (!written) {
                workingBitmap.recycle()
                return null
            }

            val canDownscaleMore = workingBitmap.width > MIN_BITMAP_EDGE && workingBitmap.height > MIN_BITMAP_EDGE
            if (!canDownscaleMore) {
                workingBitmap.recycle()
                return null
            }

            val nextWidth = (workingBitmap.width * DOWNSCALE_FACTOR).toInt().coerceAtLeast(MIN_BITMAP_EDGE)
            val nextHeight = (workingBitmap.height * DOWNSCALE_FACTOR).toInt().coerceAtLeast(MIN_BITMAP_EDGE)
            val downscaled = Bitmap.createScaledBitmap(workingBitmap, nextWidth, nextHeight, true)
            workingBitmap.recycle()
            workingBitmap = downscaled
        }
    }

    private fun applyExifOrientation(sourceBytes: ByteArray, bitmap: Bitmap): Bitmap {
        val orientation = runCatching {
            ExifInterface(ByteArrayInputStream(sourceBytes)).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap
        }

        val transformed = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (transformed != bitmap) bitmap.recycle()
        return transformed
    }

    private fun computeSampleSize(width: Int, height: Int, maxLongEdge: Int?): Int {
        if (maxLongEdge == null) return 1
        var size = 1
        val longEdge = maxOf(width, height)
        while (longEdge / size > maxLongEdge) size *= 2
        return size
    }

    private fun scaleToMaxEdge(bitmap: Bitmap, maxEdge: Int?): Bitmap {
        if (maxEdge == null) return bitmap
        val w = bitmap.width
        val h = bitmap.height
        val long = maxOf(w, h)
        if (long <= maxEdge) return bitmap
        val scale = maxEdge.toFloat() / long
        val newW = (w * scale).toInt().coerceAtLeast(1)
        val newH = (h * scale).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        if (scaled != bitmap) bitmap.recycle()
        return scaled
    }
}
