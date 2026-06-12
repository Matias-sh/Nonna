package com.cocido.nonna.util

import android.content.Context
import android.net.Uri
import com.cocido.nonna.ui.components.MemoryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Prepara imágenes una sola vez (recorte → compresión final) para subir sin recomprimir al guardar.
 */
object PhotoUploadPreparer {

    const val PREPARED_PREFIX = "nonna_upload_"

    enum class Profile {
        /** Recuerdos foto + portada de audio. */
        Memory,
        /** Portada de cofre. */
        Cover,
        /** Avatar de perfil. */
        Avatar
    }

    fun isPrepared(file: File): Boolean =
        file.name.startsWith(PREPARED_PREFIX) && file.exists() && file.length() > 0L

    fun preparedUri(file: File): Uri = Uri.fromFile(file)

    /** Comprime tras recortar/seleccionar; devuelve URI lista para subir. */
    suspend fun prepare(context: Context, source: Uri, profile: Profile): Uri? =
        withContext(Dispatchers.IO) {
            val (maxEdge, maxBytes) = limits(profile)
            val compressed = ImageCompressor.compressForUpload(
                context = context,
                uri = source,
                maxBytes = maxBytes,
                maxLongEdge = maxEdge
            ) ?: return@withContext null
            Uri.fromFile(finalizePrepared(compressed, profile))
        }

    /** Resuelve a [File] de subida; recomprime solo si aún no está preparada. */
    suspend fun resolveFile(context: Context, uri: Uri, profile: Profile): File? =
        withContext(Dispatchers.IO) {
            uriToLocalFile(uri)?.takeIf { isPrepared(it) }
                ?: prepare(context, uri, profile)?.let { preparedUri ->
                    uriToLocalFile(preparedUri)
                }
        }

    private fun limits(profile: Profile): Pair<Int, Long> = when (profile) {
        Profile.Memory -> 1600 to MemoryUploadLimits.maxBytesFor(MemoryType.Photo)
        Profile.Cover, Profile.Avatar -> 1920 to MemoryUploadLimits.maxBytesFor(MemoryType.Photo)
    }

    private fun uriToLocalFile(uri: Uri): File? {
        if (uri.scheme != "file") return null
        val path = uri.path?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val file = File(path)
        return file.takeIf { it.exists() && it.isFile }
    }

    private fun finalizePrepared(compressed: File, profile: Profile): File {
        if (compressed.name.startsWith(PREPARED_PREFIX)) return compressed
        val suffix = when (profile) {
            Profile.Memory -> "memory"
            Profile.Cover -> "cover"
            Profile.Avatar -> "avatar"
        }
        val dest = File.createTempFile("${PREPARED_PREFIX}${suffix}_", ".jpg", compressed.parentFile)
        if (compressed.renameTo(dest)) {
            return dest
        }
        compressed.copyTo(dest, overwrite = true)
        compressed.delete()
        return dest
    }
}
