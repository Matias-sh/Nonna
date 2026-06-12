package com.cocido.nonna.ui.screens.memory

import android.Manifest
import android.app.Activity
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cocido.nonna.ui.components.EmotionalTag
import com.cocido.nonna.ui.components.MemoryType
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.rememberPhotoCropFlow
import com.cocido.nonna.ui.components.NonnaDatePickerField
import com.cocido.nonna.ui.components.NonnaTextArea
import com.cocido.nonna.ui.components.NonnaTextField
import com.cocido.nonna.ui.components.PageHeader
import com.cocido.nonna.ui.components.ScreenTitleSection
import com.cocido.nonna.ui.components.emotionalTagLabel
import com.cocido.nonna.R
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.util.CameraCaptureHelper
import com.cocido.nonna.util.PhotoUploadPreparer
import com.cocido.nonna.util.MemoryUploadLimits
import com.cocido.nonna.util.FormValidators
import com.cocido.nonna.util.UserMessages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

private const val MAX_MEMORY_DESCRIPTION_LENGTH = 280

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditMemoryScreen(
    memoryId: String,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: com.cocido.nonna.ui.viewmodel.EditMemoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val memory by viewModel.memory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val editForbidden by viewModel.editForbidden.collectAsState()
    val isTextMemory = memory?.type == MemoryType.Text

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var emotionalTag by remember { mutableStateOf<EmotionalTag?>(null) }
    var customEmotion by remember { mutableStateOf("") }
    var replacementUri by remember { mutableStateOf<Uri?>(null) }
    var replacementDisplayName by remember { mutableStateOf<String?>(null) }
    var isPreparingReplacement by remember { mutableStateOf(false) }
    var resolvedTextContent by remember { mutableStateOf<String?>(null) }
    var attemptedSave by remember { mutableStateOf(false) }
    var showCameraSettingsDialog by remember { mutableStateOf(false) }
    val maxArchivosPlan by viewModel.maxArchivosPorRecuerdo.collectAsState()
    val maxPhotosAllowed = remember(maxArchivosPlan) {
        MemoryUploadLimits.maxPhotoFilesForPlan(maxArchivosPlan)
    }
    var selectedPhotos by remember { mutableStateOf<List<Any>>(emptyList()) }
    var originalPhotoUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var newCoverUri by remember { mutableStateOf<Uri?>(null) }
    var markRemoveAudioCover by remember { mutableStateOf(false) }

    var pendingCropIndex by remember { mutableStateOf<Int?>(null) }
    val memoryPhotoCropTitle = stringResource(R.string.memory_photo_crop_title)
    val photoCropFlow = rememberPhotoCropFlow(
        title = memoryPhotoCropTitle,
        uploadProfile = PhotoUploadPreparer.Profile.Memory
    ) { croppedUri ->
        val replaceAt = pendingCropIndex
        pendingCropIndex = null
        selectedPhotos = if (replaceAt != null && replaceAt in selectedPhotos.indices) {
            selectedPhotos.toMutableList().apply { set(replaceAt, croppedUri) }
        } else {
            mergeIncomingPhotos(
                current = selectedPhotos,
                incoming = listOf(croppedUri),
                maxPhotos = maxPhotosAllowed
            )
        }
    }

    val multiPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = PickMultipleVisualMedia(maxOf(maxPhotosAllowed, 1))
    ) { uris ->
        if (uris.isNotEmpty()) {
            val remaining = (maxPhotosAllowed - selectedPhotos.size).coerceAtLeast(0)
            if (remaining > 0) {
                pendingCropIndex = null
                photoCropFlow.cropSequential(uris.take(remaining))
            }
        }
    }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        pendingCameraUri = null
        if (success && uri != null && selectedPhotos.size < maxPhotosAllowed) {
            pendingCropIndex = null
            photoCropFlow.cropSingle(uri)
        }
    }
    val launchCameraCapture: () -> Unit = {
        if (selectedPhotos.size < maxPhotosAllowed) {
            val outputUri = CameraCaptureHelper.createOutputUri(context)
            pendingCameraUri = outputUri
            takePictureLauncher.launch(outputUri)
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCameraCapture()
        } else {
            val permanentlyDenied = activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
            if (permanentlyDenied) {
                showCameraSettingsDialog = true
            }
        }
    }
    val openCropAtIndex: (Int) -> Unit = crop@{ index ->
        val item = selectedPhotos.getOrNull(index) ?: return@crop
        scope.launch {
            val sourceUri = when (item) {
                is Uri -> item
                is String -> {
                    val file = withContext(Dispatchers.IO) { downloadImageToCache(context, item) }
                        ?: return@launch
                    Uri.fromFile(file)
                }
                else -> return@launch
            }
            pendingCropIndex = index
            photoCropFlow.cropSingle(sourceUri)
        }
    }
    val openGalleryPicker: () -> Unit = {
        if (selectedPhotos.size < maxPhotosAllowed) {
            multiPhotoPickerLauncher.launch(
                PickVisualMediaRequest(PickVisualMedia.ImageOnly)
            )
        }
    }
    val openCamera: () -> Unit = {
        if (selectedPhotos.size < maxPhotosAllowed) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (granted) {
                launchCameraCapture()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    val audioCoverCropFlow = rememberPhotoCropFlow(
        title = stringResource(R.string.memory_audio_cover_crop_title),
        uploadProfile = PhotoUploadPreparer.Profile.Memory
    ) { croppedUri ->
        newCoverUri = croppedUri
        markRemoveAudioCover = false
    }
    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { audioCoverCropFlow.cropSingle(it) }
    }

    val genericFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (memory == null || uri == null) return@rememberLauncherForActivityResult
        replacementUri = uri
        replacementDisplayName = resolveDisplayName(context, uri)
    }

    LaunchedEffect(memory?.id) {
        val current = memory ?: return@LaunchedEffect
        newCoverUri = null
        markRemoveAudioCover = false
        replacementUri = null
        replacementDisplayName = null
        if (current.type == MemoryType.Photo) {
            val urls = when {
                current.carouselImageUrls.isNotEmpty() -> current.carouselImageUrls
                else -> listOfNotNull(
                    current.mainMediaUrl?.trim()?.takeIf { it.isNotBlank() },
                    current.thumbnailUrl?.trim()?.takeIf { it.isNotBlank() }
                ).distinct()
            }
            originalPhotoUrls = urls
            selectedPhotos = urls
        } else {
            originalPhotoUrls = emptyList()
            selectedPhotos = emptyList()
        }
        title = current.title
        date = current.date
        emotionalTag = current.emotionalTag
        customEmotion = current.emotionalCustomLabel.orEmpty()
        resolvedTextContent = current.description
        if (current.type == MemoryType.Text && current.description.isNullOrBlank()) {
            resolvedTextContent = withContext(Dispatchers.IO) {
                downloadTextFromUrlIfPossible(current.audioUrl)
            }
        }
        description = resolvedTextContent.orEmpty()
    }

    LaunchedEffect(Unit) {
        viewModel.updated.collect { onSaved() }
    }

    LaunchedEffect(editForbidden) {
        if (editForbidden) {
            Toast.makeText(
                context,
                context.getString(R.string.memory_edit_forbidden),
                Toast.LENGTH_LONG
            ).show()
            onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        PageHeader(
            onBack = onBack
        )

        if (isLoading && memory == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(NonnaDimens.screenPaddingHorizontal)
        ) {
            ScreenTitleSection(
                title = stringResource(R.string.edit_memory_title),
                subtitle = if (isLoading) {
                    stringResource(R.string.common_loading)
                } else {
                    stringResource(R.string.edit_memory_subtitle)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            val currentMemory = memory
            if (currentMemory != null) {
                if (currentMemory.type == MemoryType.Photo) {
                    MemoryPhotoSelectionContent(
                        selectedPhotos = selectedPhotos,
                        maxPhotos = maxPhotosAllowed,
                        onPhotosChanged = { selectedPhotos = it },
                        onPickGallery = openGalleryPicker,
                        onTakePhoto = openCamera,
                        onCropPhoto = openCropAtIndex
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                } else {
                    Text(
                        text = stringResource(R.string.common_file),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (replacementUri != null) {
                        Text(
                            text = replacementDisplayName ?: stringResource(R.string.memory_new_file_selected),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }

                    NonnaButton(
                        text = stringResource(R.string.common_upload_file),
                        onClick = {
                            when (currentMemory.type) {
                                MemoryType.Audio -> genericFilePickerLauncher.launch("audio/*")
                                MemoryType.Text -> genericFilePickerLauncher.launch("text/*")
                                else -> Unit
                            }
                        },
                        style = if (replacementUri != null) NonnaButtonStyle.Primary else NonnaButtonStyle.Outline
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                if (currentMemory.type == MemoryType.Audio) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.memory_audio_cover_optional_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.memory_audio_cover_section_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NonnaButton(
                            text = stringResource(R.string.memory_audio_cover_pick),
                            onClick = {
                                coverPickerLauncher.launch(
                                    PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                                )
                            },
                            style = NonnaButtonStyle.Outline
                        )
                        if (newCoverUri != null || !currentMemory.audioCoverUrl.isNullOrBlank()) {
                            NonnaButton(
                                text = stringResource(R.string.memory_audio_cover_clear),
                                onClick = {
                                    newCoverUri = null
                                    markRemoveAudioCover = true
                                },
                                style = NonnaButtonStyle.Ghost
                            )
                        }
                    }
                    val coverPreviewModel: Any? = when {
                        newCoverUri != null -> newCoverUri
                        markRemoveAudioCover -> null
                        else -> currentMemory.audioCoverUrl?.trim()?.takeIf { it.isNotBlank() }
                    }
                    if (coverPreviewModel != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AsyncImage(
                            model = coverPreviewModel,
                            contentDescription = stringResource(R.string.memory_audio_cover_cd),
                            modifier = Modifier
                                .width(140.dp)
                                .height(175.dp)
                                .clip(NonnaCorners.Medium),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            NonnaTextField(
                value = title,
                onValueChange = { title = it },
                label = stringResource(R.string.memory_title_required),
                isError = attemptedSave && !FormValidators.hasMinLength(title, 2),
                errorMessage = if (attemptedSave && !FormValidators.hasMinLength(title, 2)) {
                    UserMessages.INVALID_TITLE_MIN_2
                } else {
                    null
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            NonnaTextArea(
                value = description,
                onValueChange = { description = it },
                label = if (isTextMemory) stringResource(R.string.common_content) else stringResource(R.string.common_description),
                minLines = 4,
                maxLines = 8,
                helperText = if (isTextMemory) null else "${description.length}/$MAX_MEMORY_DESCRIPTION_LENGTH",
                isError = !isTextMemory && description.length > MAX_MEMORY_DESCRIPTION_LENGTH,
                errorMessage = if (!isTextMemory && description.length > MAX_MEMORY_DESCRIPTION_LENGTH) {
                    "Supera el máximo de $MAX_MEMORY_DESCRIPTION_LENGTH caracteres."
                } else {
                    null
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            NonnaDatePickerField(
                value = date,
                onValueChange = { date = it },
                label = stringResource(R.string.common_date),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.common_emotion),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            val canSelectPresetEmotion = customEmotion.isBlank()
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NonnaButton(
                    text = stringResource(R.string.memory_no_emotion),
                    onClick = { emotionalTag = null },
                    style = if (emotionalTag == null && customEmotion.isBlank()) {
                        NonnaButtonStyle.Primary
                    } else {
                        NonnaButtonStyle.Outline
                    }
                )
                EmotionalTag.entries.forEach { tag ->
                    NonnaButton(
                        text = emotionalTagLabel(tag),
                        onClick = {
                            emotionalTag = tag
                            customEmotion = ""
                        },
                        enabled = canSelectPresetEmotion,
                        style = if (emotionalTag == tag) NonnaButtonStyle.Primary else NonnaButtonStyle.Outline
                    )
                }
            }
            if (!canSelectPresetEmotion) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.emotion_presets_disabled_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            NonnaTextField(
                value = customEmotion,
                onValueChange = {
                    customEmotion = it
                    if (it.isNotBlank()) emotionalTag = null
                },
                label = stringResource(R.string.emotion_custom_label),
                placeholder = stringResource(R.string.emotion_custom_placeholder),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NonnaButton(
                    text = stringResource(R.string.common_cancel),
                    onClick = onBack,
                    style = NonnaButtonStyle.Outline,
                    modifier = Modifier.weight(1f)
                )
                NonnaButton(
                    text = if (isSaving) stringResource(R.string.common_saving) else stringResource(R.string.common_save_changes),
                    onClick = {
                        attemptedSave = true
                        if (!FormValidators.hasMinLength(title, 2) || !FormValidators.isValidIsoDate(date)) {
                            return@NonnaButton
                        }
                        val current = memory ?: return@NonnaButton
                        scope.launch {
                            isPreparingReplacement = true
                            val photosForSave = selectedPhotos
                            val photosChanged = current.type == MemoryType.Photo &&
                                photoKeys(photosForSave) != photoKeys(originalPhotoUrls)
                            val keepExistingMainUrl = originalPhotoUrls.firstOrNull()?.let { main ->
                                photosForSave.any { isSameRemote(it, main) }
                            } ?: true
                            val replacementFile = withContext(Dispatchers.IO) {
                                when (current.type) {
                                    MemoryType.Photo -> {
                                        if (photosChanged) {
                                            photosForSave.firstOrNull()?.let { main ->
                                                resolvePhotoForUpload(context, main)
                                            }
                                        } else {
                                            null
                                        }
                                    }
                                    else -> when {
                                        replacementUri != null -> copyUriToCacheFile(context, replacementUri!!)
                                        current.type == MemoryType.Text && description.isNotBlank() -> {
                                            File.createTempFile("recuerdo_edit_", ".txt", context.cacheDir).apply {
                                                writeText(description)
                                            }
                                        }
                                        else -> null
                                    }
                                }
                            }
                            val portadaFile = if (current.type == MemoryType.Audio && newCoverUri != null) {
                                withContext(Dispatchers.IO) {
                                    PhotoUploadPreparer.resolveFile(
                                        context = context,
                                        uri = newCoverUri!!,
                                        profile = PhotoUploadPreparer.Profile.Memory
                                    )
                                }
                            } else {
                                null
                            }
                            val currentExtras = photosForSave.drop(1)
                            val originalExtras = originalPhotoUrls.drop(1)
                            val galleryFiles = if (photosChanged && currentExtras.isNotEmpty()) {
                                withContext(Dispatchers.IO) {
                                    currentExtras.mapNotNull { item ->
                                        resolvePhotoForUpload(context, item)
                                    }
                                }
                            } else {
                                null
                            }
                            isPreparingReplacement = false
                            val urlPortada = if (
                                current.type == MemoryType.Audio &&
                                markRemoveAudioCover &&
                                newCoverUri == null &&
                                portadaFile == null
                            ) {
                                ""
                            } else {
                                null
                            }
                            val limpiar = photosChanged &&
                                currentExtras.isEmpty() &&
                                originalExtras.isNotEmpty()
                            viewModel.save(
                                title = title.trim(),
                                description = if (current.type == MemoryType.Text) null else description,
                                date = date,
                                emotionalTag = emotionalTag,
                                customEmotion = customEmotion,
                                replacementFile = replacementFile,
                                newPortadaAudio = portadaFile,
                                urlPortadaAudio = urlPortada,
                                galleryImages = galleryFiles,
                                limpiarImagenesGaleria = limpiar,
                                keepExistingMainUrl = keepExistingMainUrl
                            )
                        }
                    },
                    enabled = title.isNotBlank() &&
                        !isSaving &&
                        !isPreparingReplacement &&
                        (isTextMemory || description.length <= MAX_MEMORY_DESCRIPTION_LENGTH) &&
                        FormValidators.hasMinLength(title, 2) &&
                        FormValidators.isValidIsoDate(date) &&
                        (memory?.type != MemoryType.Photo || selectedPhotos.isNotEmpty()),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showCameraSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showCameraSettingsDialog = false },
            title = { Text(stringResource(R.string.camera_permission_blocked_title)) },
            text = { Text(stringResource(R.string.camera_permission_blocked_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCameraSettingsDialog = false
                        context.startActivity(
                            android.content.Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        )
                    }
                ) {
                    Text(stringResource(R.string.open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCameraSettingsDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

private fun normalizePhotoUrl(url: String): String = url.trim().substringBefore('?')

private fun isSameRemote(item: Any, url: String): Boolean =
    item is String && normalizePhotoUrl(item) == normalizePhotoUrl(url)

/** Agrega fotos nuevas al final; el usuario reordena arrastrando. */
private fun mergeIncomingPhotos(
    current: List<Any>,
    incoming: List<Uri>,
    maxPhotos: Int
): List<Any> {
    return if (current.isEmpty()) {
        incoming.take(maxPhotos)
    } else {
        (current + incoming).distinctBy { photoKey(it) }.take(maxPhotos)
    }
}

private fun photoKey(item: Any): String = when (item) {
    is Uri -> "local:$item"
    is String -> "remote:$item"
    else -> item.toString()
}

private fun photoKeys(items: List<Any>): List<String> = items.map { photoKey(it) }

private suspend fun resolvePhotoForUpload(context: android.content.Context, item: Any): File? {
    return when (item) {
        is Uri -> PhotoUploadPreparer.resolveFile(
            context = context,
            uri = item,
            profile = PhotoUploadPreparer.Profile.Memory
        )
        is String -> {
            val downloaded = downloadImageToCache(context, item) ?: return null
            PhotoUploadPreparer.resolveFile(
                context = context,
                uri = Uri.fromFile(downloaded),
                profile = PhotoUploadPreparer.Profile.Memory
            )
        }
        else -> null
    }
}

private fun downloadImageToCache(context: android.content.Context, url: String): File? {
    if (!url.startsWith("http://") && !url.startsWith("https://")) return null
    return runCatching {
        val client = OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "NONNA-Android")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val bytes = response.body?.bytes() ?: return null
            if (bytes.isEmpty()) return null
            File.createTempFile("recuerdo_remote_", ".jpg", context.cacheDir).apply {
                writeBytes(bytes)
            }
        }
    }.getOrNull()
}

private fun copyUriToCacheFile(context: android.content.Context, uri: Uri): File? {
    return runCatching {
        val ext = context.contentResolver.getType(uri)
            ?.substringAfterLast('/', "")
            ?.takeIf { it.isNotBlank() }
            ?: "bin"
        val outFile = File.createTempFile("recuerdo_edit_", ".$ext", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            outFile.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        outFile
    }.getOrNull()
}

private fun downloadTextFromUrlIfPossible(sourceUrl: String?): String? {
    if (sourceUrl.isNullOrBlank()) return null
    if (!sourceUrl.startsWith("http://") && !sourceUrl.startsWith("https://")) return null
    return runCatching {
        val client = OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
        val request = Request.Builder()
            .url(sourceUrl)
            .header("User-Agent", "NONNA-Android")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            response.body?.string()
                ?.replace("\u0000", "")
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        }
    }.getOrNull()
}

private fun resolveDisplayName(context: android.content.Context, uri: Uri): String? {
    return runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
    }.getOrNull()
}

