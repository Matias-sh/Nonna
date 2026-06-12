package com.cocido.nonna.ui.components

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.cocido.nonna.util.PhotoUploadPreparer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface PhotoCropFlowHandle {
    /** Abre el editor de recorte para una sola imagen. */
    fun cropSingle(uri: Uri)

    /** Recorta varias imágenes en secuencia (galería múltiple). */
    fun cropSequential(uris: List<Uri>)
}

@Composable
fun rememberPhotoCropFlow(
    title: String,
    uploadProfile: PhotoUploadPreparer.Profile? = null,
    onPrepareFailed: () -> Unit = {},
    onPhotoCropped: (Uri) -> Unit
): PhotoCropFlowHandle {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val titleState = rememberUpdatedState(title)
    val onCroppedState = rememberUpdatedState(onPhotoCropped)
    val profileState = rememberUpdatedState(uploadProfile)
    val onFailedState = rememberUpdatedState(onPrepareFailed)
    val pendingQueue = remember { mutableStateListOf<Uri>() }
    val launcherRef = remember {
        arrayOfNulls<ManagedActivityResultLauncher<NonnaCropRequest, Uri?>>(1)
    }

    fun launchNextCrop() {
        val next = pendingQueue.removeFirstOrNull() ?: return
        launcherRef[0]?.launch(
            NonnaCropRequest.freeForm(
                sourceUri = next,
                title = titleState.value
            )
        )
    }

    fun deliverCropped(croppedUri: Uri) {
        scope.launch {
            val profile = profileState.value
            val readyUri = if (profile == null) {
                croppedUri
            } else {
                withContext(Dispatchers.IO) {
                    PhotoUploadPreparer.prepare(context, croppedUri, profile)
                } ?: run {
                    onFailedState.value()
                    launchNextCrop()
                    return@launch
                }
            }
            onCroppedState.value(readyUri)
            launchNextCrop()
        }
    }

    val cropLauncher = rememberLauncherForActivityResult(
        contract = NonnaCropContract()
    ) { result ->
        if (result == null) {
            pendingQueue.clear()
            return@rememberLauncherForActivityResult
        }
        deliverCropped(result)
    }
    launcherRef[0] = cropLauncher

    return remember(cropLauncher) {
        object : PhotoCropFlowHandle {
            override fun cropSingle(uri: Uri) {
                pendingQueue.clear()
                cropLauncher.launch(
                    NonnaCropRequest.freeForm(
                        sourceUri = uri,
                        title = titleState.value
                    )
                )
            }

            override fun cropSequential(uris: List<Uri>) {
                if (uris.isEmpty()) return
                pendingQueue.clear()
                if (uris.size > 1) {
                    pendingQueue.addAll(uris.drop(1))
                }
                cropLauncher.launch(
                    NonnaCropRequest.freeForm(
                        sourceUri = uris.first(),
                        title = titleState.value
                    )
                )
            }
        }
    }
}
