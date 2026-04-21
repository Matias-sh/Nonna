package com.cocido.nonna.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Comprime una imagen desde [uri] para subida, manteniendo el tamaño por debajo de [maxBytes].
 * Redimensiona a máximo [maxLongEdge] píxeles en el lado largo y comprime en JPEG.
 * Si la imagen ya es pequeña o no es decodificable, devuelve un archivo temporal con la copia del stream.
 */
object ImageCompressor {

    private const val MAX_LONG_EDGE = 1920
    private const val DEFAULT_JPEG_QUALITY = 85
    private const val MIN_JPEG_QUALITY = 50
    private const val MIN_BITMAP_EDGE = 320
    private const val DOWNSCALE_FACTOR = 0.8f

    /**
     * Comprime la imagen en [uri] y devuelve un archivo temporal listo para subir.
     * @param maxBytes Tamaño máximo en bytes (por defecto 1 MB para evitar 413 en el servidor).
     * @param maxLongEdge Máximo del lado largo en píxeles. Si es null, no redimensiona por resolución.
     */
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
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return null

        var workingBitmap = scaleToMaxEdge(bitmap, maxLongEdge)
        if (workingBitmap != bitmap) bitmap.recycle()
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
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }
}
