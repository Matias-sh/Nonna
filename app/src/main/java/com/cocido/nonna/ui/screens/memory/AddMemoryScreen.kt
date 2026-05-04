package com.cocido.nonna.ui.screens.memory

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.cocido.nonna.ui.components.AudioRecorderComponent
import com.cocido.nonna.ui.components.EmotionalTag
import com.cocido.nonna.ui.components.MemoryType
import com.cocido.nonna.util.ImageCompressor
import com.cocido.nonna.util.MemoryUploadLimits
import com.cocido.nonna.util.FormValidators
import com.cocido.nonna.util.UserMessages
import com.cocido.nonna.ui.components.emotionalTagLabel
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.NonnaBottomFeedbackBanner
import com.cocido.nonna.ui.components.NonnaCropContract
import com.cocido.nonna.ui.components.NonnaCropRequest
import com.cocido.nonna.ui.components.NonnaDatePickerField
import com.cocido.nonna.ui.components.NonnaFeedbackType
import com.cocido.nonna.ui.components.NonnaTextArea
import com.cocido.nonna.ui.components.NonnaTextField
import com.cocido.nonna.ui.components.PageHeader
import com.cocido.nonna.R
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AddMemoryStep {
    Type, Content, Details
}

private const val MAX_MEMORY_DESCRIPTION_LENGTH = 280

@Composable
fun AddMemoryScreen(
    cofreId: String?,
    onBack: () -> Unit,
    onSave: () -> Unit,
    viewModel: com.cocido.nonna.ui.viewmodel.AddMemoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scope = rememberCoroutineScope()
    var feedbackVisible by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackType by remember { mutableStateOf(NonnaFeedbackType.Success) }
    val isLoading by viewModel.isLoading.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.saved.collectLatest { _: com.cocido.nonna.ui.components.MemoryUiModel ->
            feedbackMessage = "Recuerdo guardado correctamente"
            feedbackType = NonnaFeedbackType.Success
            feedbackVisible = true
            delay(1200)
            feedbackVisible = false
            onSave()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { message ->
            feedbackMessage = message
            feedbackType = NonnaFeedbackType.Error
            feedbackVisible = true
            delay(2600)
            feedbackVisible = false
        }
    }
    var step by remember { mutableStateOf(AddMemoryStep.Type) }
    var selectedType by remember { mutableStateOf<MemoryType?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var textContent by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var emotionalTag by remember { mutableStateOf<EmotionalTag?>(null) }
    var customEmotion by remember { mutableStateOf("") }
    var hasAudioRecording by remember { mutableStateOf(false) }
    var hasImageSelected by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var recordedAudioFile by remember { mutableStateOf<File?>(null) }
    var selectedAudioLabel by remember { mutableStateOf<String?>(null) }
    var showCameraSettingsDialog by remember { mutableStateOf(false) }
    var attemptedSave by remember { mutableStateOf(false) }
    val cropLauncher = rememberLauncherForActivityResult(
        contract = NonnaCropContract()
    ) { result ->
        result?.let { croppedUri ->
            selectedImageUri = croppedUri
            hasImageSelected = true
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            cropLauncher.launch(
                NonnaCropRequest(
                    sourceUri = it,
                    aspectRatio = 4f / 5f,
                    title = context.getString(R.string.memory_edit_image_title),
                    lockAspectRatio = false
                )
            )
        }
    }
    val cameraPreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            val imageFile = File.createTempFile("nonna_camera_", ".jpg", context.cacheDir)
            FileOutputStream(imageFile).use { out ->
                it.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            cropLauncher.launch(
                NonnaCropRequest(
                    sourceUri = Uri.fromFile(imageFile),
                    aspectRatio = 4f / 5f,
                    title = context.getString(R.string.memory_edit_image_title),
                    lockAspectRatio = false
                )
            )
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraPreviewLauncher.launch(null)
        } else {
            val permanentlyDenied = activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
            if (permanentlyDenied) {
                showCameraSettingsDialog = true
            } else {
                feedbackMessage = "Necesitás habilitar el permiso de cámara"
                feedbackType = NonnaFeedbackType.Error
                feedbackVisible = true
                scope.launch {
                    delay(1800)
                    feedbackVisible = false
                }
            }
        }
    }
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spokenText = result.data
            ?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            ?.trim()
        if (!spokenText.isNullOrBlank()) {
            textContent = if (textContent.isBlank()) spokenText else "$textContent $spokenText"
        }
    }
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val copied = withContext(Dispatchers.IO) {
                copyAudioToCache(context, uri)
            }
            if (copied != null) {
                recordedAudioFile = copied
                hasAudioRecording = true
                selectedAudioLabel = resolveDisplayName(context, uri) ?: copied.name
            } else {
                feedbackMessage = "No se pudo cargar el archivo de audio."
                feedbackType = NonnaFeedbackType.Error
                feedbackVisible = true
                delay(1800)
                feedbackVisible = false
            }
        }
    }

    val maxArchivosPlan by viewModel.maxArchivosPorRecuerdo.collectAsState()
    val maxGalleryExtra = remember(maxArchivosPlan) {
        (maxArchivosPlan - 1).coerceIn(0, 2)
    }
    var extraGalleryUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var audioCoverUri by remember { mutableStateOf<Uri?>(null) }

    val audioCoverCropLauncher = rememberLauncherForActivityResult(
        contract = NonnaCropContract()
    ) { result ->
        result?.let { audioCoverUri = it }
    }
    val audioCoverVisualLauncher = rememberLauncherForActivityResult(
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

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = PickMultipleVisualMedia(maxGalleryExtra.coerceIn(1, 2))
    ) { uris ->
        if (maxGalleryExtra > 0 && uris.isNotEmpty()) {
            // Acumular: muchas galerías solo devuelven la última tanda; el usuario puede sumar con varios toques.
            extraGalleryUris = (extraGalleryUris + uris)
                .distinctBy { it.toString() }
                .take(maxGalleryExtra)
        }
    }
    val stepTitle = when (step) {
        AddMemoryStep.Type -> stringResource(R.string.add_memory_step_type)
        AddMemoryStep.Content -> stringResource(R.string.add_memory_step_content)
        AddMemoryStep.Details -> stringResource(R.string.add_memory_step_details)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (showCameraSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showCameraSettingsDialog = false },
                title = { Text(stringResource(R.string.camera_permission_blocked_title)) },
                text = { Text(stringResource(R.string.camera_permission_blocked_text)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showCameraSettingsDialog = false
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                            context.startActivity(intent)
                        }
                    ) { Text(stringResource(R.string.open_settings)) }
                },
                dismissButton = {
                    TextButton(onClick = { showCameraSettingsDialog = false }) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }
            )
        }

        PageHeader(
            title = stringResource(R.string.add_memory_title),
            subtitle = stepTitle,
            onBack = {
                when (step) {
                    AddMemoryStep.Type -> onBack()
                    AddMemoryStep.Content -> step = AddMemoryStep.Type
                    AddMemoryStep.Details -> step = AddMemoryStep.Content
                }
            }
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(NonnaDimens.screenPaddingHorizontal)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            when (step) {
                AddMemoryStep.Type -> TypeSelectionStep(
                    onTypeSelected = { type ->
                        selectedType = type
                        step = AddMemoryStep.Content
                    }
                )
                
                AddMemoryStep.Content -> ContentStep(
                    type = selectedType!!,
                    textContent = textContent,
                    onTextContentChange = { textContent = it },
                    hasAudioRecording = hasAudioRecording,
                    selectedAudioLabel = selectedAudioLabel,
                    hasImageSelected = hasImageSelected,
                    selectedImageUri = selectedImageUri,
                    onRequestPickImage = { imagePickerLauncher.launch("image/*") },
                    onRequestCaptureImage = {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            cameraPreviewLauncher.launch(null)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onBackToType = { step = AddMemoryStep.Type },
                    onContinue = { step = AddMemoryStep.Details },
                    onRequestDictation = {
                        val intent = Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "es-AR")
                        }
                        speechLauncher.launch(intent)
                    },
                    onAudioRecorded = { file ->
                        recordedAudioFile = file
                        hasAudioRecording = true
                        selectedAudioLabel = file.name
                    },
                    onPickAudioFile = { audioPickerLauncher.launch("audio/*") }
                )
                
                AddMemoryStep.Details -> DetailsStep(
                    type = selectedType!!,
                    selectedImageUri = selectedImageUri,
                    maxGalleryExtra = maxGalleryExtra,
                    extraGalleryUris = extraGalleryUris,
                    onRequestPickGallery = {
                        galleryPickerLauncher.launch(
                            PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                        )
                    },
                    onClearGallery = { extraGalleryUris = emptyList() },
                    audioCoverUri = audioCoverUri,
                    onRequestPickAudioCover = {
                        audioCoverVisualLauncher.launch(
                            PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                        )
                    },
                    onClearAudioCover = { audioCoverUri = null },
                    title = title,
                    onTitleChange = { title = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    textContent = textContent,
                    date = date,
                    onDateChange = { date = it },
                    emotionalTag = emotionalTag,
                    onEmotionalTagChange = {
                        emotionalTag = it
                        if (it != null) customEmotion = ""
                    },
                    customEmotion = customEmotion,
                    onCustomEmotionChange = {
                        customEmotion = it
                        if (it.isNotBlank()) emotionalTag = null
                    },
                    onBack = { step = AddMemoryStep.Content },
                    onSave = {
                        attemptedSave = true
                        val normalizedTitle = title.trim()
                        if (!FormValidators.hasMinLength(normalizedTitle, 2)) {
                            feedbackMessage = UserMessages.INVALID_TITLE_MIN_2
                            feedbackType = NonnaFeedbackType.Error
                            feedbackVisible = true
                            scope.launch {
                                delay(1800)
                                feedbackVisible = false
                            }
                            return@DetailsStep
                        }
                        if (!FormValidators.isValidIsoDate(date)) {
                            feedbackMessage = UserMessages.INVALID_DATE
                            feedbackType = NonnaFeedbackType.Error
                            feedbackVisible = true
                            scope.launch {
                                delay(1800)
                                feedbackVisible = false
                            }
                            return@DetailsStep
                        }
                        if (description.length > MAX_MEMORY_DESCRIPTION_LENGTH) {
                            feedbackMessage = "La descripción es muy larga (máximo $MAX_MEMORY_DESCRIPTION_LENGTH caracteres)."
                            feedbackType = NonnaFeedbackType.Error
                            feedbackVisible = true
                            scope.launch {
                                delay(1800)
                                feedbackVisible = false
                            }
                            return@DetailsStep
                        }
                        if (selectedType == MemoryType.Audio) {
                            val audioFile = recordedAudioFile
                            val maxAudioBytes = MemoryUploadLimits.maxBytesFor(MemoryType.Audio)
                            if (audioFile == null || audioFile.length() > maxAudioBytes) {
                                feedbackMessage = MemoryUploadLimits.exceededMessage(MemoryType.Audio, maxAudioBytes)
                                feedbackType = NonnaFeedbackType.Error
                                feedbackVisible = true
                                scope.launch {
                                    delay(2600)
                                    feedbackVisible = false
                                }
                                return@DetailsStep
                            }
                        }
                        if (selectedType == MemoryType.Text) {
                            val textBytes = textContent.toByteArray(Charsets.UTF_8).size.toLong()
                            val maxTextBytes = MemoryUploadLimits.maxBytesFor(MemoryType.Text)
                            if (textBytes > maxTextBytes) {
                                feedbackMessage = MemoryUploadLimits.exceededMessage(MemoryType.Text, maxTextBytes)
                                feedbackType = NonnaFeedbackType.Error
                                feedbackVisible = true
                                scope.launch {
                                    delay(2600)
                                    feedbackVisible = false
                                }
                                return@DetailsStep
                            }
                        }
                        val cid = cofreId
                        if (cid != null) {
                        scope.launch {
                            if (selectedType == MemoryType.Photo &&
                                1 + extraGalleryUris.size > maxArchivosPlan
                            ) {
                                feedbackMessage = context.getString(
                                    R.string.memory_gallery_too_many,
                                    maxArchivosPlan
                                )
                                feedbackType = NonnaFeedbackType.Error
                                feedbackVisible = true
                                delay(2600)
                                feedbackVisible = false
                                return@launch
                            }
                            val file = withContext(Dispatchers.IO) {
                                when (selectedType) {
                                    MemoryType.Photo -> selectedImageUri?.let { uri ->
                                        ImageCompressor.compressForUpload(
                                            context = context,
                                            uri = uri,
                                            maxBytes = MemoryUploadLimits.maxBytesFor(MemoryType.Photo),
                                            maxLongEdge = 1600
                                        )
                                    }
                                    MemoryType.Text -> File.createTempFile("recuerdo", ".txt").apply {
                                        writeText(textContent.ifBlank { "" })
                                    }
                                    MemoryType.Audio -> recordedAudioFile
                                    null -> null
                                }
                            }
                            val galleryFiles = if (selectedType == MemoryType.Photo && extraGalleryUris.isNotEmpty()) {
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
                                emptyList()
                            }
                            val portadaFile = if (selectedType == MemoryType.Audio && audioCoverUri != null) {
                                withContext(Dispatchers.IO) {
                                    ImageCompressor.compressForUpload(
                                        context = context,
                                        uri = audioCoverUri!!,
                                        maxBytes = MemoryUploadLimits.maxBytesFor(MemoryType.Photo),
                                        maxLongEdge = 1600
                                    )
                                }
                            } else {
                                null
                            }
                            if (file != null) {
                                val chosenType = selectedType
                                if (chosenType != null) {
                                    val maxBytes = MemoryUploadLimits.maxBytesFor(chosenType)
                                    if (file.length() > maxBytes) {
                                        feedbackMessage = MemoryUploadLimits.exceededMessage(chosenType, maxBytes)
                                        feedbackType = NonnaFeedbackType.Error
                                        feedbackVisible = true
                                        delay(2600)
                                        feedbackVisible = false
                                        return@launch
                                    }
                                }
                                val customEmotionClean = customEmotion.trim().takeIf { it.isNotBlank() }
                                val (emotionId, emotionCustomFromTag) = viewModel.resolveEmotionPayload(emotionalTag)
                                viewModel.save(
                                    cofreRecuerdosId = cid,
                                    titulo = normalizedTitle,
                                    file = file,
                                    descripcion = description.ifBlank { null },
                                    fecha = date,
                                    emocionId = if (customEmotionClean != null) null else emotionId,
                                    emocionPersonalizada = customEmotionClean ?: emotionCustomFromTag,
                                    portadaAudio = portadaFile,
                                    galleryImages = galleryFiles
                                )
                            } else {
                                feedbackMessage = MemoryUploadLimits.exceededMessage(MemoryType.Photo)
                                feedbackType = NonnaFeedbackType.Error
                                feedbackVisible = true
                                delay(2600)
                                feedbackVisible = false
                            }
                        }
                        } else {
                            onSave()
                        }
                    },
                    canSave = FormValidators.hasMinLength(title, 2) &&
                        FormValidators.isValidIsoDate(date) &&
                        !isLoading,
                    attemptedSave = attemptedSave,
                    isLoading = isLoading
                )
            }
        }
    }
        NonnaBottomFeedbackBanner(
            visible = feedbackVisible,
            message = feedbackMessage,
            type = feedbackType,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun TypeSelectionStep(
    onTypeSelected: (MemoryType) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.add_memory_type_question),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TypeOption(
                icon = Icons.Outlined.Image,
                title = stringResource(R.string.memory_type_photo),
                description = stringResource(R.string.add_memory_type_photo_desc),
                onClick = { onTypeSelected(MemoryType.Photo) },
                modifier = Modifier.fillMaxWidth()
            )

            TypeOption(
                icon = Icons.Outlined.AudioFile,
                title = stringResource(R.string.memory_type_audio),
                description = stringResource(R.string.add_memory_type_audio_desc),
                onClick = { onTypeSelected(MemoryType.Audio) },
                modifier = Modifier.fillMaxWidth()
            )

            TypeOption(
                icon = Icons.Outlined.Description,
                title = stringResource(R.string.memory_type_text),
                description = stringResource(R.string.add_memory_type_text_desc),
                onClick = { onTypeSelected(MemoryType.Text) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TypeOption(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(NonnaCorners.Large)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = NonnaCorners.Large
            )
            .clickable(onClick = onClick)
            .padding(NonnaDimens.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ContentStep(
    type: MemoryType,
    textContent: String,
    onTextContentChange: (String) -> Unit,
    hasAudioRecording: Boolean,
    selectedAudioLabel: String?,
    onAudioRecorded: (File) -> Unit,
    onPickAudioFile: () -> Unit,
    hasImageSelected: Boolean,
    selectedImageUri: Uri?,
    onRequestPickImage: () -> Unit,
    onRequestCaptureImage: () -> Unit,
    onBackToType: () -> Unit,
    onContinue: () -> Unit,
    onRequestDictation: () -> Unit
) {
    val contentReady = when (type) {
        MemoryType.Photo -> hasImageSelected
        MemoryType.Audio -> hasAudioRecording
        MemoryType.Text -> textContent.isNotBlank()
    }

    Column {
        when (type) {
            MemoryType.Photo -> PhotoContent(
                hasImage = hasImageSelected,
                selectedImageUri = selectedImageUri,
                onPickImageClick = onRequestPickImage,
                onCaptureImageClick = onRequestCaptureImage
            )
            MemoryType.Audio -> AudioContent(
                onRecorded = onAudioRecorded,
                selectedAudioLabel = selectedAudioLabel,
                onPickAudioFile = onPickAudioFile
            )
            MemoryType.Text -> TextContent(
                content = textContent,
                onContentChange = onTextContentChange,
                onRequestDictation = onRequestDictation
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Show continue button when content is ready (text, photo or audio)
        if (contentReady) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NonnaButton(
                    text = stringResource(R.string.common_back),
                    onClick = onBackToType,
                    style = NonnaButtonStyle.Outline,
                    modifier = Modifier.weight(1f)
                )
                NonnaButton(
                    text = stringResource(R.string.onboarding_continue),
                    onClick = onContinue,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PhotoContent(
    hasImage: Boolean,
    selectedImageUri: Uri?,
    onPickImageClick: () -> Unit,
    onCaptureImageClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 5f)
                .clip(NonnaCorners.Large)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                    shape = NonnaCorners.Large
                )
                .clickable(onClick = onPickImageClick),
            contentAlignment = Alignment.Center
        ) {
            if (hasImage && selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = stringResource(R.string.memory_selected_image_cd),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(NonnaCorners.Large)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            shape = NonnaCorners.Large
                        ),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    shape = NonnaCorners.Full,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                ) {
                    Text(
                        text = stringResource(R.string.memory_tap_image_change),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.memory_upload_photo),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.memory_tap_choose_gallery),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NonnaButton(
                text = if (hasImage) "Cambiar imagen" else "Galería",
                onClick = onPickImageClick
            )
            NonnaButton(text = stringResource(R.string.common_camera), onClick = onCaptureImageClick, style = NonnaButtonStyle.Outline)
        }
    }
}

@Composable
private fun AudioContent(
    onRecorded: (File) -> Unit,
    selectedAudioLabel: String?,
    onPickAudioFile: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AudioRecorderComponent(
            onRecordingComplete = { file, _ -> onRecorded(file) }
        )
        NonnaButton(
            text = "Subir archivo de audio",
            onClick = onPickAudioFile,
            style = NonnaButtonStyle.Outline,
            icon = Icons.Outlined.Upload,
            fullWidth = true
        )
        selectedAudioLabel?.takeIf { it.isNotBlank() }?.let { name ->
            Text(
                text = "Audio seleccionado: $name",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TextContent(
    content: String,
    onContentChange: (String) -> Unit,
    onRequestDictation: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NonnaTextArea(
            value = content,
            onValueChange = onContentChange,
            label = stringResource(R.string.memory_write_label),
            placeholder = stringResource(R.string.memory_write_placeholder),
            minLines = 12,
            maxLines = 12,
            helperText = "Escribí con calma o dictalo con el micrófono.",
            modifier = Modifier.fillMaxWidth()
        )
        NonnaButton(
            text = stringResource(R.string.memory_dictate_button),
            onClick = onRequestDictation,
            style = NonnaButtonStyle.Outline,
            icon = Icons.Outlined.Mic
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailsStep(
    type: MemoryType,
    selectedImageUri: Uri?,
    maxGalleryExtra: Int,
    extraGalleryUris: List<Uri>,
    onRequestPickGallery: () -> Unit,
    onClearGallery: () -> Unit,
    audioCoverUri: Uri?,
    onRequestPickAudioCover: () -> Unit,
    onClearAudioCover: () -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    textContent: String,
    date: String,
    onDateChange: (String) -> Unit,
    emotionalTag: EmotionalTag?,
    onEmotionalTagChange: (EmotionalTag?) -> Unit,
    customEmotion: String,
    onCustomEmotionChange: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean,
    attemptedSave: Boolean,
    isLoading: Boolean = false
) {
    val titleError = attemptedSave && !FormValidators.hasMinLength(title, 2)
    val dateError = attemptedSave && !FormValidators.isValidIsoDate(date)
    val descriptionLength = description.length
    val descriptionIsTooLong = descriptionLength > MAX_MEMORY_DESCRIPTION_LENGTH
    val canSelectPresetEmotion = customEmotion.isBlank()
    val canWriteCustomEmotion = emotionalTag == null
    Column {
        // Vista previa del contenido (foto real en memoria tipo imagen)
        when (type) {
            MemoryType.Photo -> {
                if (selectedImageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(NonnaCorners.Large)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                                shape = NonnaCorners.Large
                            )
                    ) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = stringResource(R.string.memory_selected_image_cd),
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3f),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(NonnaCorners.Large)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(NonnaDimens.cardPaddingLarge)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.memory_no_photo_loaded),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            MemoryType.Audio -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (audioCoverUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(NonnaCorners.Large)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        ) {
                            AsyncImage(
                                model = audioCoverUri,
                                contentDescription = stringResource(R.string.memory_audio_cover_cd),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4f / 3f),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(NonnaCorners.Large)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .padding(NonnaDimens.cardPaddingLarge)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.AudioFile,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.memory_audio_recorded),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            MemoryType.Text -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(NonnaCorners.Large)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .padding(NonnaDimens.cardPaddingLarge)
                ) {
                    Text(
                        text = textContent.take(200) + if (textContent.length > 200) "..." else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4
                    )
                }
            }
        }

        if (type == MemoryType.Photo && maxGalleryExtra > 0) {
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
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NonnaButton(
                    text = stringResource(R.string.memory_carousel_pick),
                    onClick = onRequestPickGallery,
                    style = NonnaButtonStyle.Outline
                )
                if (extraGalleryUris.isNotEmpty()) {
                    NonnaButton(
                        text = stringResource(R.string.memory_carousel_clear),
                        onClick = onClearGallery,
                        style = NonnaButtonStyle.Ghost
                    )
                }
            }
            if (extraGalleryUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    extraGalleryUris.forEach { u ->
                        AsyncImage(
                            model = u,
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(NonnaCorners.Medium),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        if (type == MemoryType.Audio) {
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
                    onClick = onRequestPickAudioCover,
                    style = NonnaButtonStyle.Outline
                )
                if (audioCoverUri != null) {
                    NonnaButton(
                        text = stringResource(R.string.memory_audio_cover_clear),
                        onClick = onClearAudioCover,
                        style = NonnaButtonStyle.Ghost
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        NonnaTextField(
            value = title,
            onValueChange = onTitleChange,
            label = stringResource(R.string.memory_title_required),
            placeholder = stringResource(R.string.memory_title_placeholder),
            isError = titleError,
            errorMessage = if (titleError) UserMessages.INVALID_TITLE_MIN_2 else null,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Description (only for photo/audio)
        if (type != MemoryType.Text) {
            Spacer(modifier = Modifier.height(16.dp))
            
            NonnaTextArea(
                value = description,
                onValueChange = onDescriptionChange,
                label = stringResource(R.string.memory_context_optional_label),
                placeholder = stringResource(R.string.memory_context_optional_placeholder),
                minLines = 4,
                helperText = "$descriptionLength/$MAX_MEMORY_DESCRIPTION_LENGTH",
                isError = descriptionIsTooLong,
                errorMessage = if (descriptionIsTooLong) {
                    "Supera el máximo de $MAX_MEMORY_DESCRIPTION_LENGTH caracteres."
                } else {
                    null
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Date
        NonnaDatePickerField(
            value = date,
            onValueChange = onDateChange,
            label = stringResource(R.string.memory_date_approx_label),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Emotional tag
        Text(
            text = stringResource(R.string.memory_emotion_optional_question),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EmotionalTag.entries.forEach { tag ->
                val isSelected = emotionalTag == tag
                
                Surface(
                    onClick = {
                        onEmotionalTagChange(if (isSelected) null else tag)
                    },
                    enabled = canSelectPresetEmotion,
                    shape = NonnaCorners.Medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = emotionalTagLabel(tag),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
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
            onValueChange = onCustomEmotionChange,
            label = stringResource(R.string.emotion_custom_label),
            placeholder = stringResource(R.string.emotion_custom_placeholder),
            enabled = canWriteCustomEmotion,
            modifier = Modifier.fillMaxWidth()
        )
        if (!canWriteCustomEmotion) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.emotion_custom_enabled_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NonnaButton(
                text = stringResource(R.string.common_back),
                onClick = onBack,
                style = NonnaButtonStyle.Outline,
                modifier = Modifier.weight(1f)
            )
            NonnaButton(
                text = if (isLoading) stringResource(R.string.common_saving) else stringResource(R.string.memory_save_button),
                onClick = onSave,
                enabled = canSave && !isLoading && !descriptionIsTooLong,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

private fun android.content.Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun copyAudioToCache(context: android.content.Context, uri: Uri): File? {
    val extension = context.contentResolver.getType(uri)
        ?.substringAfterLast('/')
        ?.takeIf { it.isNotBlank() }
        ?: "m4a"
    val outFile = File.createTempFile("nonna_audio_upload_", ".$extension", context.cacheDir)
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        } ?: return null
        outFile
    }.getOrNull()
}

private fun resolveDisplayName(context: android.content.Context, uri: Uri): String? {
    return runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(0)
                } else null
            }
    }.getOrNull()
}

