package com.cocido.nonna.ui.screens.memory

import android.Manifest
import android.app.Activity
import android.content.Intent
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.cocido.nonna.ui.components.AudioRecorderComponent
import com.cocido.nonna.ui.components.EmotionalTag
import com.cocido.nonna.ui.components.MemoryType
import com.cocido.nonna.util.CameraCaptureHelper
import com.cocido.nonna.util.PhotoUploadPreparer
import com.cocido.nonna.util.MemoryUploadLimits
import com.cocido.nonna.util.FormValidators
import com.cocido.nonna.util.UserMessages
import com.cocido.nonna.ui.components.emotionalTagLabel
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.NonnaBottomFeedbackBanner
import com.cocido.nonna.ui.components.rememberPhotoCropFlow
import com.cocido.nonna.ui.components.NonnaDatePickerField
import com.cocido.nonna.ui.components.NonnaFeedbackType
import com.cocido.nonna.ui.components.NonnaTextArea
import com.cocido.nonna.ui.components.NonnaTextField
import com.cocido.nonna.ui.components.PageHeader
import com.cocido.nonna.ui.components.ScreenTitleSection
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
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val uploadProgress by viewModel.uploadProgress.collectAsStateWithLifecycle()
    var isPreparingSave by remember { mutableStateOf(false) }
    val isSaving = isLoading || isPreparingSave
    LaunchedEffect(Unit) {
        viewModel.saved.collectLatest { _: com.cocido.nonna.ui.components.MemoryUiModel ->
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
    var selectedPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var recordedAudioFile by remember { mutableStateOf<File?>(null) }
    var selectedAudioLabel by remember { mutableStateOf<String?>(null) }
    var showCameraSettingsDialog by remember { mutableStateOf(false) }
    var attemptedSave by remember { mutableStateOf(false) }

    val maxArchivosPlan by viewModel.maxArchivosPorRecuerdo.collectAsStateWithLifecycle()
    val maxPhotosAllowed = remember(maxArchivosPlan) {
        MemoryUploadLimits.maxPhotoFilesForPlan(maxArchivosPlan)
    }

    var pendingCropIndex by remember { mutableStateOf<Int?>(null) }
    val memoryPhotoCropTitle = stringResource(R.string.memory_photo_crop_title)
    val photoCropFlow = rememberPhotoCropFlow(
        title = memoryPhotoCropTitle,
        uploadProfile = PhotoUploadPreparer.Profile.Memory,
        onPrepareFailed = {
            feedbackMessage = context.getString(R.string.photo_prepare_error)
            feedbackType = NonnaFeedbackType.Error
            feedbackVisible = true
            scope.launch {
                delay(2600)
                feedbackVisible = false
            }
        }
    ) { croppedUri ->
        val replaceAt = pendingCropIndex
        pendingCropIndex = null
        selectedPhotoUris = if (replaceAt != null && replaceAt in selectedPhotoUris.indices) {
            selectedPhotoUris.toMutableList().apply { set(replaceAt, croppedUri) }
        } else {
            (selectedPhotoUris + croppedUri)
                .distinctBy { it.toString() }
                .take(maxPhotosAllowed)
        }
    }

    val multiPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = PickMultipleVisualMedia(maxOf(maxPhotosAllowed, 1))
    ) { uris ->
        if (uris.isNotEmpty()) {
            val remaining = (maxPhotosAllowed - selectedPhotoUris.size).coerceAtLeast(0)
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
        if (success && uri != null && selectedPhotoUris.size < maxPhotosAllowed) {
            pendingCropIndex = null
            photoCropFlow.cropSingle(uri)
        }
    }
    val launchCameraCapture: () -> Unit = {
        if (selectedPhotoUris.size < maxPhotosAllowed) {
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
            } else {
                feedbackMessage = context.getString(R.string.camera_permission_required_message)
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
                feedbackMessage = context.getString(R.string.memory_audio_file_load_error)
                feedbackType = NonnaFeedbackType.Error
                feedbackVisible = true
                delay(1800)
                feedbackVisible = false
            }
        }
    }

    var audioCoverUri by remember { mutableStateOf<Uri?>(null) }

    val audioCoverCropFlow = rememberPhotoCropFlow(
        title = stringResource(R.string.memory_audio_cover_crop_title),
        uploadProfile = PhotoUploadPreparer.Profile.Memory
    ) { croppedUri ->
        audioCoverUri = croppedUri
    }
    val audioCoverVisualLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { audioCoverCropFlow.cropSingle(it) }
    }

    val openCropAtIndex: (Int) -> Unit = crop@{ index ->
        val uri = selectedPhotoUris.getOrNull(index) ?: return@crop
        pendingCropIndex = index
        photoCropFlow.cropSingle(uri)
    }
    val openGalleryPicker: () -> Unit = {
        if (selectedPhotoUris.size < maxPhotosAllowed) {
            multiPhotoPickerLauncher.launch(
                PickVisualMediaRequest(PickVisualMedia.ImageOnly)
            )
        }
    }
    val openCamera: () -> Unit = {
        if (selectedPhotoUris.size < maxPhotosAllowed) {
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
            .imePadding()
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
            onBack = {
                when (step) {
                    AddMemoryStep.Type -> onBack()
                    AddMemoryStep.Content -> step = AddMemoryStep.Type
                    AddMemoryStep.Details -> step = AddMemoryStep.Content
                }
            }
        )

        if (isSaving && uploadProgress != null) {
            LinearProgressIndicator(
                progress = { uploadProgress ?: 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = NonnaDimens.screenPaddingHorizontal)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(NonnaDimens.screenPaddingHorizontal)
        ) {
            ScreenTitleSection(
                title = stringResource(R.string.add_memory_title),
                subtitle = stepTitle
            )
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
                    selectedPhotoUris = selectedPhotoUris,
                    maxPhotos = maxPhotosAllowed,
                    onPhotosChanged = { selectedPhotoUris = it },
                    onPickGallery = openGalleryPicker,
                    onTakePhoto = openCamera,
                    onCropPhoto = openCropAtIndex,
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
                    selectedPhotoUris = selectedPhotoUris,
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
                            feedbackMessage = context.getString(
                                R.string.memory_description_too_long,
                                MAX_MEMORY_DESCRIPTION_LENGTH
                            )
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
                            isPreparingSave = true
                            try {
                            if (selectedType == MemoryType.Photo &&
                                selectedPhotoUris.size > maxPhotosAllowed
                            ) {
                                feedbackMessage = context.getString(
                                    R.string.memory_gallery_too_many,
                                    maxPhotosAllowed
                                )
                                feedbackType = NonnaFeedbackType.Error
                                feedbackVisible = true
                                delay(2600)
                                feedbackVisible = false
                                return@launch
                            }
                            val mainPhotoUri = selectedPhotoUris.firstOrNull()
                            val extraPhotoUris = selectedPhotoUris.drop(1)
                            val file = withContext(Dispatchers.IO) {
                                when (selectedType) {
                                    MemoryType.Photo -> mainPhotoUri?.let { uri ->
                                        PhotoUploadPreparer.resolveFile(
                                            context = context,
                                            uri = uri,
                                            profile = PhotoUploadPreparer.Profile.Memory
                                        )
                                    }
                                    MemoryType.Text -> File.createTempFile("recuerdo", ".txt").apply {
                                        writeText(textContent.ifBlank { "" })
                                    }
                                    MemoryType.Audio -> recordedAudioFile
                                    null -> null
                                }
                            }
                            val galleryFiles = if (selectedType == MemoryType.Photo && extraPhotoUris.isNotEmpty()) {
                                withContext(Dispatchers.IO) {
                                    extraPhotoUris.mapNotNull { uri ->
                                        PhotoUploadPreparer.resolveFile(
                                            context = context,
                                            uri = uri,
                                            profile = PhotoUploadPreparer.Profile.Memory
                                        )
                                    }
                                }
                            } else {
                                emptyList()
                            }
                            val portadaFile = if (selectedType == MemoryType.Audio && audioCoverUri != null) {
                                withContext(Dispatchers.IO) {
                                    PhotoUploadPreparer.resolveFile(
                                        context = context,
                                        uri = audioCoverUri!!,
                                        profile = PhotoUploadPreparer.Profile.Memory
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
                            } finally {
                                isPreparingSave = false
                            }
                        }
                        } else {
                            onSave()
                        }
                    },
                    canSave = FormValidators.hasMinLength(title, 2) &&
                        FormValidators.isValidIsoDate(date) &&
                        !isSaving,
                    attemptedSave = attemptedSave,
                    isLoading = isSaving
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
    selectedPhotoUris: List<Uri>,
    maxPhotos: Int,
    onPhotosChanged: (List<Uri>) -> Unit,
    onPickGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onCropPhoto: (Int) -> Unit,
    onContinue: () -> Unit,
    onRequestDictation: () -> Unit
) {
    val contentReady = when (type) {
        MemoryType.Photo -> selectedPhotoUris.isNotEmpty()
        MemoryType.Audio -> hasAudioRecording
        MemoryType.Text -> textContent.isNotBlank()
    }

    Column {
        when (type) {
            MemoryType.Photo -> MemoryPhotoSelectionContent(
                selectedPhotos = selectedPhotoUris,
                maxPhotos = maxPhotos,
                onPhotosChanged = onPhotosChanged,
                onPickGallery = onPickGallery,
                onTakePhoto = onTakePhoto,
                onCropPhoto = onCropPhoto
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

        if (contentReady) {
            Spacer(modifier = Modifier.height(24.dp))
            NonnaButton(
                text = stringResource(R.string.onboarding_continue),
                onClick = onContinue,
                fullWidth = true
            )
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
            text = stringResource(R.string.memory_upload_audio_file),
            onClick = onPickAudioFile,
            style = NonnaButtonStyle.Outline,
            icon = Icons.Outlined.Upload,
            fullWidth = true
        )
        selectedAudioLabel?.takeIf { it.isNotBlank() }?.let { name ->
            Text(
                text = stringResource(R.string.memory_audio_selected, name),
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
            helperText = stringResource(R.string.memory_text_helper),
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
    selectedPhotoUris: List<Uri>,
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
                if (selectedPhotoUris.isNotEmpty()) {
                    if (selectedPhotoUris.size == 1) {
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
                                model = selectedPhotoUris.first(),
                                contentDescription = stringResource(R.string.memory_selected_image_cd),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4f / 3f),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(selectedPhotoUris) { _, uri ->
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(NonnaCorners.Medium),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
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
                    stringResource(
                        R.string.memory_description_limit_exceeded,
                        MAX_MEMORY_DESCRIPTION_LENGTH
                    )
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

