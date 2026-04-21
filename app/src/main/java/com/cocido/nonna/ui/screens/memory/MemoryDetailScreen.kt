package com.cocido.nonna.ui.screens.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import androidx.compose.runtime.collectAsState
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.hilt.navigation.compose.hiltViewModel
import com.cocido.nonna.ui.components.MemoryType
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.PrimaryGradientEnd
import com.cocido.nonna.ui.theme.PrimaryGradientStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

@Composable
fun MemoryDetailScreen(
    memoryId: String,
    onBack: () -> Unit,
    onDelete: () -> Unit = {},
    onEdit: (String) -> Unit = {},
    viewModel: com.cocido.nonna.ui.viewmodel.MemoryDetailViewModel = hiltViewModel()
) {
    val memory by viewModel.memory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) { viewModel.load() }
    
    when {
        isLoading && memory == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        }
        memory == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Recuerdo no encontrado",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        else -> {
            val currentMemory = memory!!
            MemoryDetailContent(
            memory = currentMemory,
            onBack = onBack,
            onEdit = { onEdit(currentMemory.id) },
            onDelete = {
                viewModel.delete(onSuccess = onDelete)
            },
            showMenu = showMenu,
            onShowMenuChange = { showMenu = it }
        )
        }
    }
}

@Composable
private fun MemoryDetailContent(
    memory: com.cocido.nonna.ui.components.MemoryUiModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    showMenu: Boolean,
    onShowMenuChange: (Boolean) -> Unit
) {
    val photoUrls = remember(memory.thumbnailUrl) { parsePotentialPhotoUrls(memory.thumbnailUrl) }
    var selectedPhotoUrl by remember(photoUrls) { mutableStateOf(photoUrls.firstOrNull()) }
    var showPhotoViewer by remember { mutableStateOf(false) }
    var showTapHint by remember(selectedPhotoUrl) { mutableStateOf(selectedPhotoUrl != null) }
    var fallbackTextContent by remember(memory.id) { mutableStateOf<String?>(null) }
    var isLoadingFallbackText by remember(memory.id) { mutableStateOf(false) }

    LaunchedEffect(memory.id, memory.type, memory.description, memory.audioUrl) {
        if (memory.type != MemoryType.Text) return@LaunchedEffect
        if (!memory.description.isNullOrBlank()) {
            fallbackTextContent = null
            isLoadingFallbackText = false
            return@LaunchedEffect
        }
        val candidateUrl = memory.audioUrl?.trim().orEmpty()
        if (!looksLikeRemoteUrl(candidateUrl)) {
            fallbackTextContent = null
            isLoadingFallbackText = false
            return@LaunchedEffect
        }
        isLoadingFallbackText = true
        fallbackTextContent = withContext(Dispatchers.IO) {
            downloadTextFromUrl(candidateUrl)
        }
        isLoadingFallbackText = false
    }

    LaunchedEffect(selectedPhotoUrl) {
        if (selectedPhotoUrl != null) {
            showTapHint = true
            delay(5000)
            showTapHint = false
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Text(
                text = "Recuerdo",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            
            Box {
                IconButton(onClick = { onShowMenuChange(true) }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Más opciones",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { onShowMenuChange(false) }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            onShowMenuChange(false)
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Edit, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        onClick = {
                            onShowMenuChange(false)
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Delete, contentDescription = null)
                        }
                    )
                }
            }
        }
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(NonnaDimens.screenPaddingHorizontal)
        ) {
            // Media preview
            if (memory.type == MemoryType.Audio && !memory.audioUrl.isNullOrBlank()) {
                SpotifyStyleAudioPlayer(
                    audioUrl = memory.audioUrl,
                    title = memory.title.ifBlank { "Audio del recuerdo" },
                    subtitle = memory.description ?: "Recuerdo de audio",
                    coverUrl = memory.thumbnailUrl?.takeIf { isLikelyImageUrl(it) }
                )
            } else {
                if (memory.type == MemoryType.Photo && selectedPhotoUrl != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = NonnaCorners.Large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box {
                            AsyncImage(
                                model = selectedPhotoUrl,
                                contentDescription = memory.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showPhotoViewer = true },
                                contentScale = ContentScale.FillWidth
                            )
                            if (showTapHint) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 12.dp),
                                    shape = NonnaCorners.Full,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)
                                ) {
                                    Text(
                                        text = "Tocá para ampliar",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        shape = NonnaCorners.Large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val icon = when (memory.type) {
                                MemoryType.Photo -> Icons.Outlined.Image
                                MemoryType.Audio -> Icons.Outlined.AudioFile
                                MemoryType.Text -> Icons.Outlined.Description
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            if (memory.type == MemoryType.Photo && photoUrls.size > 1) {
                Text(
                    text = "Fotos del recuerdo",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(photoUrls) { url ->
                        Card(
                            modifier = Modifier
                                .size(72.dp)
                                .clickable { selectedPhotoUrl = url },
                            shape = NonnaCorners.Medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Foto del recuerdo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Title
            Text(
                text = memory.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Date and tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = memory.date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (memory.emotionalTag != null || !memory.emotionalCustomLabel.isNullOrBlank()) {
                    Surface(
                        shape = NonnaCorners.Full,
                        color = if (memory.emotionalTag != null) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    ) {
                        Text(
                            text = memory.emotionalTag?.label ?: memory.emotionalCustomLabel.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (memory.emotionalTag != null) {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Description
            if (!memory.description.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = NonnaCorners.Card,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(NonnaDimens.cardPaddingLarge)
                    ) {
                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = memory.description ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Text content for text memories
            if (memory.type == MemoryType.Text) {
                val textContent = memory.description?.trim().takeUnless { it.isNullOrBlank() }
                    ?: fallbackTextContent?.trim().takeUnless { it.isNullOrBlank() }
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = NonnaCorners.Card,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(NonnaDimens.cardPaddingLarge)
                    ) {
                        Text(
                            text = "Contenido",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = when {
                                !textContent.isNullOrBlank() -> textContent
                                isLoadingFallbackText -> "Cargando contenido del archivo..."
                                else -> "Este recuerdo de texto no tiene contenido visible todavía."
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            ),
                            color = if (textContent.isNullOrBlank()) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Info note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                        shape = NonnaCorners.Medium
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = "Este recuerdo fue guardado para preservar la memoria familiar. Podés editarlo o compartirlo con tu familia.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showPhotoViewer && selectedPhotoUrl != null) {
        PhotoFullScreenViewer(
            imageUrl = selectedPhotoUrl!!,
            onDismiss = { showPhotoViewer = false }
        )
    }
}

@Composable
private fun PhotoFullScreenViewer(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(onClick = onDismiss)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 4f)
                        offset = if (scale > 1f) offset + pan else Offset.Zero
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Imagen en pantalla completa",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(12.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.45f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Cerrar visor",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun SpotifyStyleAudioPlayer(
    audioUrl: String,
    title: String,
    subtitle: String,
    coverUrl: String?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var currentPositionMs by remember { mutableStateOf(0L) }
    var totalDurationMs by remember { mutableStateOf(0L) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resolvedAudioUrl by remember(audioUrl) { mutableStateOf(audioUrl) }
    var fallbackTried by remember(audioUrl) { mutableStateOf(false) }

    val exoPlayer = remember(resolvedAudioUrl) {
        val httpFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("NONNA-Android")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(30000)
        val dataSourceFactory = DefaultDataSource.Factory(context, httpFactory)
        val mediaItem = MediaItem.Builder()
            .setUri(resolvedAudioUrl)
            .build()
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)

        ExoPlayer.Builder(context).build().apply {
            setMediaSource(mediaSource)
            prepare()
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    errorMessage = null
                    totalDurationMs = exoPlayer.duration.coerceAtLeast(0L)
                }
                if (playbackState == Player.STATE_ENDED) {
                    isPlaying = false
                    progress = 1f
                }
            }

            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                if (!fallbackTried && resolvedAudioUrl.startsWith("http")) {
                    fallbackTried = true
                    scope.launch {
                        val fallback = withContext(Dispatchers.IO) {
                            downloadAudioToCache(context, resolvedAudioUrl)
                        }
                        if (fallback.localPath != null) {
                            resolvedAudioUrl = fallback.localPath
                            errorMessage = null
                        } else {
                            errorMessage = fallback.errorMessage
                                ?: "No se pudo reproducir este audio."
                        }
                    }
                } else {
                    errorMessage = "No se pudo reproducir este audio."
                }
                isPlaying = false
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(isPlaying, exoPlayer) {
        while (isPlaying) {
            val duration = exoPlayer.duration.coerceAtLeast(0L)
            val position = exoPlayer.currentPosition.coerceAtLeast(0L)
            currentPositionMs = position
            totalDurationMs = duration
            progress = if (duration > 0) (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
            delay(250)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = NonnaCorners.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = NonnaCorners.Large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (!coverUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = coverUrl,
                            contentDescription = "Portada del audio",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.AudioFile,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatPlaybackTime(currentPositionMs),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatPlaybackTime(totalDurationMs),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { exoPlayer.seekTo((exoPlayer.currentPosition - 15000L).coerceAtLeast(0L)) }) {
                        Icon(Icons.Outlined.SkipPrevious, contentDescription = "Retroceder 15s")
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            IconButton(onClick = {
                                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                            }) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                    contentDescription = if (isPlaying) "Pausar audio" else "Reproducir audio",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                    }
                    IconButton(onClick = { exoPlayer.seekTo((exoPlayer.currentPosition + 15000L).coerceAtMost(totalDurationMs)) }) {
                        Icon(Icons.Outlined.SkipNext, contentDescription = "Adelantar 15s")
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private data class AudioFallbackResult(
    val localPath: String?,
    val errorMessage: String? = null
)

private fun downloadAudioToCache(
    context: android.content.Context,
    sourceUrl: String
): AudioFallbackResult {
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
            if (!response.isSuccessful) {
                return AudioFallbackResult(
                    localPath = null,
                    errorMessage = "No se pudo descargar el audio (${response.code})."
                )
            }
            val body = response.body
                ?: return AudioFallbackResult(
                    localPath = null,
                    errorMessage = "El servidor no devolvió contenido de audio."
                )
            val bytes = body.bytes()
            if (bytes.isEmpty()) {
                return AudioFallbackResult(
                    localPath = null,
                    errorMessage = "Este audio está vacío o dañado en el servidor."
                )
            }
            val output = File.createTempFile("nonna_audio_", ".bin", context.cacheDir)
            output.writeBytes(bytes)
            AudioFallbackResult(localPath = output.absolutePath)
        }
    }.getOrElse {
        AudioFallbackResult(
            localPath = null,
            errorMessage = "No se pudo reproducir este audio."
        )
    }
}

private fun looksLikeRemoteUrl(url: String): Boolean {
    if (url.isBlank()) return false
    val normalized = url.lowercase()
    return normalized.startsWith("https://") || normalized.startsWith("http://")
}

private fun downloadTextFromUrl(sourceUrl: String): String? {
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
            val mediaType = response.body?.contentType()?.toString()?.lowercase().orEmpty()
            val rawBody = response.body?.string()
                ?.replace("\u0000", "")
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: return null

            if (isTextualContentType(mediaType) || looksLikeReadableText(rawBody)) {
                rawBody
            } else {
                null
            }
        }
    }.getOrNull()
}

private fun isTextualContentType(contentType: String): Boolean {
    if (contentType.isBlank()) return false
    return contentType.startsWith("text/") ||
        contentType.contains("json") ||
        contentType.contains("xml") ||
        contentType.contains("csv")
}

private fun looksLikeReadableText(value: String): Boolean {
    if (value.isBlank()) return false
    val sample = value.take(512)
    val printableChars = sample.count { it == '\n' || it == '\r' || it == '\t' || !it.isISOControl() }
    val ratio = printableChars.toFloat() / sample.length.coerceAtLeast(1)
    return ratio > 0.9f
}

private fun parsePotentialPhotoUrls(raw: String?): List<String> {
    if (raw.isNullOrBlank()) return emptyList()
    val separatorsNormalized = raw
        .replace(";", ",")
        .replace("\n", ",")
    return separatorsNormalized
        .split(",")
        .map { it.trim() }
        .filter { it.startsWith("http://") || it.startsWith("https://") }
        .distinct()
}

private fun formatPlaybackTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

private fun isLikelyImageUrl(url: String): Boolean {
    val normalized = url.lowercase()
    return normalized.endsWith(".jpg") ||
        normalized.endsWith(".jpeg") ||
        normalized.endsWith(".png") ||
        normalized.endsWith(".webp") ||
        normalized.endsWith(".gif")
}
