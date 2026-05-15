package com.cocido.nonna.ui.screens.memory

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.cocido.nonna.ui.components.NonnaCropContract
import com.cocido.nonna.ui.components.NonnaCropRequest
import com.cocido.nonna.ui.components.NonnaDatePickerField
import com.cocido.nonna.ui.components.NonnaTextArea
import com.cocido.nonna.ui.components.NonnaTextField
import com.cocido.nonna.ui.components.PageHeader
import com.cocido.nonna.ui.components.emotionalTagLabel
import com.cocido.nonna.R
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.util.ImageCompressor
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
    val scope = rememberCoroutineScope()
    val memory by viewModel.memory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
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
    val maxArchivosPlan by viewModel.maxArchivosPorRecuerdo.collectAsState()
    val maxGalleryExtra = remember(maxArchivosPlan) {
        (maxArchivosPlan - 1).coerceIn(0, 2)
    }
    var extraGalleryUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var newCoverUri by remember { mutableStateOf<Uri?>(null) }
    var markRemoveAudioCover by remember { mutableStateOf(false) }
    var markClearGalleryExtras by remember { mutableStateOf(false) }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = PickMultipleVisualMedia(maxGalleryExtra.coerceIn(1, 2))
    ) { uris ->
        if (maxGalleryExtra > 0 && uris.isNotEmpty()) {
            extraGalleryUris = (extraGalleryUris + uris)
                .distinctBy { it.toString() }
                .take(maxGalleryExtra)
            markClearGalleryExtras = false
        }
    }
    val audioCoverCropLauncher = rememberLauncherForActivityResult(
        contract = NonnaCropContract()
    ) { result ->
        if (result != null) {
            newCoverUri = result
            markRemoveAudioCover = false
        }
    }
    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            audioCoverCropLauncher.launch(
                NonnaCropRequest(
                    sourceUri = it,
                    aspectRatio = 1f,
                    title = context.getString(R.string.memory_audio_cover_crop_title),
                    lockAspectRatio = false
                )
            )
        }
    }

    val cropLauncher = rememberLauncherForActivityResult(
        contract = NonnaCropContract()
    ) { result ->
        replacementUri = result
        replacementDisplayName = result?.let { resolveDisplayName(context, it) }
    }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        val current = memory ?: return@rememberLauncherForActivityResult
        if (uri == null) return@rememberLauncherForActivityResult
        if (current.type == MemoryType.Photo) {
            cropLauncher.launch(
                NonnaCropRequest(
                    sourceUri = uri,
                    aspectRatio = 4f / 5f,
                    title = context.getString(R.string.memory_edit_new_image_title),
                    lockAspectRatio = false
                )
            )
        } else {
            replacementUri = uri
            replacementDisplayName = resolveDisplayName(context, uri)
        }
    }

    LaunchedEffect(memory?.id) {
        val current = memory ?: return@LaunchedEffect
        extraGalleryUris = emptyList()
        newCoverUri = null
        markRemoveAudioCover = false
        markClearGalleryExtras = false
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        PageHeader(
            title = stringResource(R.string.edit_memory_title),
            subtitle = if (isLoading) stringResource(R.string.common_loading) else stringResource(R.string.edit_memory_subtitle),
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
            Spacer(modifier = Modifier.height(16.dp))

            val currentMemory = memory
            if (currentMemory != null) {
                Text(
                    text = stringResource(R.string.common_file),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (currentMemory.type == MemoryType.Photo) {
                    val previewModel: Any? = replacementUri ?: currentMemory.thumbnailUrl
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        if (previewModel != null) {
                            AsyncImage(
                                model = previewModel,
                                contentDescription = stringResource(R.string.memory_photo_cd),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.memory_no_photo_loaded),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                } else if (replacementUri != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = replacementDisplayName ?: stringResource(R.string.memory_new_file_selected),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                NonnaButton(
                    text = stringResource(R.string.common_upload_file),
                    onClick = {
                        val mime = when (currentMemory.type) {
                            MemoryType.Photo -> "image/*"
                            MemoryType.Audio -> "audio/*"
                            MemoryType.Text -> "text/*"
                        }
                        filePickerLauncher.launch(mime)
                    },
                    style = if (replacementUri != null) NonnaButtonStyle.Primary else NonnaButtonStyle.Outline
                )
                if (replacementUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.memory_new_file_selected),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (currentMemory.type == MemoryType.Photo && maxGalleryExtra > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.memory_carousel_optional_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.memory_carousel_optional_subtitle, maxGalleryExtra),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NonnaButton(
                            text = stringResource(R.string.memory_carousel_pick),
                            onClick = {
                                galleryPickerLauncher.launch(
                                    PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                                )
                            },
                            style = NonnaButtonStyle.Outline
                        )
                        if (extraGalleryUris.isNotEmpty() || currentMemory.carouselImageUrls.size > 1) {
                            NonnaButton(
                                text = stringResource(R.string.memory_carousel_clear),
                                onClick = {
                                    extraGalleryUris = emptyList()
                                    markClearGalleryExtras = true
                                },
                                style = NonnaButtonStyle.Ghost
                            )
                        }
                    }
                    if (extraGalleryUris.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            extraGalleryUris.forEach { u ->
                                AsyncImage(
                                    model = u,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(NonnaCorners.Medium),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
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
                            val replacementFile = withContext(Dispatchers.IO) {
                                when {
                                    replacementUri != null && current.type == MemoryType.Photo -> {
                                        ImageCompressor.compressForUpload(
                                            context = context,
                                            uri = replacementUri!!,
                                            maxBytes = MemoryUploadLimits.maxBytesFor(MemoryType.Photo),
                                            maxLongEdge = 1600
                                        )
                                    }
                                    replacementUri != null -> {
                                        copyUriToCacheFile(context, replacementUri!!)
                                    }
                                    current.type == MemoryType.Text && description.isNotBlank() -> {
                                        File.createTempFile("recuerdo_edit_", ".txt", context.cacheDir).apply {
                                            writeText(description)
                                        }
                                    }
                                    else -> null
                                }
                            }
                            val portadaFile = if (current.type == MemoryType.Audio && newCoverUri != null) {
                                withContext(Dispatchers.IO) {
                                    ImageCompressor.compressForUpload(
                                        context = context,
                                        uri = newCoverUri!!,
                                        maxBytes = MemoryUploadLimits.maxBytesFor(MemoryType.Photo),
                                        maxLongEdge = 1600
                                    )
                                }
                            } else {
                                null
                            }
                            val galleryFiles = if (current.type == MemoryType.Photo && extraGalleryUris.isNotEmpty()) {
                                withContext(Dispatchers.IO) {
                                    extraGalleryUris.mapNotNull { uri ->
                                        ImageCompressor.compressForUpload(
                                            context = context,
                                            uri = uri,
                                            maxBytes = MemoryUploadLimits.maxBytesFor(MemoryType.Photo),
                                            maxLongEdge = 1600
                                        )
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
                            val limpiar = markClearGalleryExtras && extraGalleryUris.isEmpty()
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
                                limpiarImagenesGaleria = limpiar
                            )
                        }
                    },
                    enabled = title.isNotBlank() &&
                        !isSaving &&
                        !isPreparingReplacement &&
                        (isTextMemory || description.length <= MAX_MEMORY_DESCRIPTION_LENGTH) &&
                        FormValidators.hasMinLength(title, 2) &&
                        FormValidators.isValidIsoDate(date),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
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

