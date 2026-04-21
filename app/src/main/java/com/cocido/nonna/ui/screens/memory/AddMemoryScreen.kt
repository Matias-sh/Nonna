package com.cocido.nonna.ui.screens.memory

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    var showCameraSettingsDialog by remember { mutableStateOf(false) }
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
                    title = "Editar imagen del recuerdo",
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
                    title = "Editar imagen del recuerdo",
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

    val stepTitle = when (step) {
        AddMemoryStep.Type -> "Elegí el tipo de recuerdo"
        AddMemoryStep.Content -> "Subí o creá el contenido"
        AddMemoryStep.Details -> "Agregá los detalles"
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
                title = { Text("Permiso de cámara bloqueado") },
                text = { Text("Para usar la cámara, activá el permiso desde Ajustes de la app.") },
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
                    ) { Text("Abrir ajustes") }
                },
                dismissButton = {
                    TextButton(onClick = { showCameraSettingsDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        PageHeader(
            title = "Agregar Recuerdo",
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
                    }
                )
                
                AddMemoryStep.Details -> DetailsStep(
                    type = selectedType!!,
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
                        val cid = cofreId
                        if (cid != null) {
                        scope.launch {
                            val file = withContext(Dispatchers.IO) {
                                when (selectedType) {
                                    MemoryType.Photo -> selectedImageUri?.let { uri ->
                                        ImageCompressor.compressForUpload(
                                            context = context,
                                            uri = uri,
                                            maxBytes = 1024L * 1024L,
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
                            if (file != null) {
                                val customEmotionClean = customEmotion.trim().takeIf { it.isNotBlank() }
                                val (emotionId, emotionCustomFromTag) = viewModel.resolveEmotionPayload(emotionalTag)
                                viewModel.save(
                                    cofreRecuerdosId = cid,
                                    titulo = title,
                                    file = file,
                                    descripcion = description.ifBlank { null },
                                    fecha = date,
                                    emocionId = if (customEmotionClean != null) null else emotionId,
                                    emocionPersonalizada = customEmotionClean ?: emotionCustomFromTag
                                )
                            } else {
                                feedbackMessage = "La imagen es demasiado pesada. Probá con otra o recortala más."
                                feedbackType = NonnaFeedbackType.Error
                                feedbackVisible = true
                                delay(2200)
                                feedbackVisible = false
                            }
                        }
                        } else {
                            onSave()
                        }
                    },
                    canSave = title.isNotBlank() && !isLoading,
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
            text = "¿Qué tipo de recuerdo querés guardar?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TypeOption(
                icon = Icons.Outlined.Image,
                title = "Foto",
                description = "Una imagen especial o documento",
                onClick = { onTypeSelected(MemoryType.Photo) },
                modifier = Modifier.fillMaxWidth()
            )

            TypeOption(
                icon = Icons.Outlined.AudioFile,
                title = "Audio",
                description = "Su voz, una canción o historia",
                onClick = { onTypeSelected(MemoryType.Audio) },
                modifier = Modifier.fillMaxWidth()
            )

            TypeOption(
                icon = Icons.Outlined.Description,
                title = "Texto",
                description = "Una anécdota, receta o carta",
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
    onAudioRecorded: (File) -> Unit,
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
                onRecorded = onAudioRecorded
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
                    text = "Volver",
                    onClick = onBackToType,
                    style = NonnaButtonStyle.Outline,
                    modifier = Modifier.weight(1f)
                )
                NonnaButton(
                    text = "Continuar",
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
                    contentDescription = "Imagen seleccionada",
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
                        text = "Tocá la imagen para cambiarla",
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
                        text = "Subir foto",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tocá aquí para elegir desde galería",
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
            NonnaButton(text = "Cámara", onClick = onCaptureImageClick, style = NonnaButtonStyle.Outline)
        }
    }
}

@Composable
private fun AudioContent(
    onRecorded: (File) -> Unit
) {
    AudioRecorderComponent(
        onRecordingComplete = { file, _ -> onRecorded(file) }
    )
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
            label = "Escribí el recuerdo",
            placeholder = "Contá la historia, la receta, la anécdota...",
            minLines = 12,
            maxLines = 12,
            helperText = "Escribí con calma o dictalo con el micrófono.",
            modifier = Modifier.fillMaxWidth()
        )
        NonnaButton(
            text = "Dictar con micrófono",
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
    isLoading: Boolean = false
) {
    val descriptionLength = description.length
    val descriptionIsTooLong = descriptionLength > MAX_MEMORY_DESCRIPTION_LENGTH
    val canSelectPresetEmotion = customEmotion.isBlank()
    val canWriteCustomEmotion = emotionalTag == null
    Column {
        // Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = NonnaCorners.Large
                )
                .padding(NonnaDimens.cardPaddingLarge)
        ) {
            when (type) {
                MemoryType.Photo -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Foto seleccionada",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                MemoryType.Audio -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AudioFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Audio grabado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                MemoryType.Text -> {
                    Text(
                        text = textContent.take(200) + if (textContent.length > 200) "..." else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        NonnaTextField(
            value = title,
            onValueChange = onTitleChange,
            label = "Título *",
            placeholder = "Ej: Cumpleaños de los 80, Receta de fideos...",
            modifier = Modifier.fillMaxWidth()
        )
        
        // Description (only for photo/audio)
        if (type != MemoryType.Text) {
            Spacer(modifier = Modifier.height(16.dp))
            
            NonnaTextArea(
                value = description,
                onValueChange = onDescriptionChange,
                label = "Contexto o historia (opcional)",
                placeholder = "Contá algo sobre este recuerdo...",
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
            label = "Fecha (aproximada)",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Emotional tag
        Text(
            text = "¿Cómo te hace sentir? (opcional)",
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
                            text = tag.label,
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
                text = "Las emociones sugeridas se desactivan mientras escribís una emoción personalizada.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        NonnaTextField(
            value = customEmotion,
            onValueChange = onCustomEmotionChange,
            label = "O escribí una emoción personalizada",
            placeholder = "Ej: Agradecido, Orgullosa, Melancólico",
            enabled = canWriteCustomEmotion,
            modifier = Modifier.fillMaxWidth()
        )
        if (!canWriteCustomEmotion) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "El campo personalizado se habilita al deseleccionar la emoción sugerida.",
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
                text = "Volver",
                onClick = onBack,
                style = NonnaButtonStyle.Outline,
                modifier = Modifier.weight(1f)
            )
            NonnaButton(
                text = if (isLoading) "Guardando..." else "Guardar recuerdo",
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

