package com.cocido.nonna.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object CameraCaptureHelper {
    fun createOutputUri(context: Context): Uri {
        val dir = File(context.cacheDir, "shared_images").apply { mkdirs() }
        val file = File.createTempFile("nonna_camera_", ".jpg", dir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
