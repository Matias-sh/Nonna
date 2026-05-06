package com.cocido.nonna.ui.screens.memory

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
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
import com.cocido.nonna.ui.components.CustomEmotionBadge
import com.cocido.nonna.ui.components.EmotionalTagBadge
import com.cocido.nonna.ui.components.MemoryType
import com.cocido.nonna.ui.components.PlaceholderCover
import com.cocido.nonna.ui.components.emotionalTagLabel
import com.cocido.nonna.util.MemoryDetailShareFormatter
import com.cocido.nonna.util.MemoryMediaUrlHeuristics
import com.cocido.nonna.util.MemorySharePolaroidGenerator
import com.cocido.nonna.R
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.PrimaryGradientEnd
import com.cocido.nonna.ui.theme.PrimaryGradientStart
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.Locale

@Composable
fun MemoryDetailScreen(
    memoryId: String,
    cofreContextName: String? = null,
    cofreCreatorDisplayName: String? = null,
    onBack: () -> Unit,
    onDelete: () -> Unit = {},
    onEdit: (String) -> Unit = {},
    viewModel: com.cocido.nonna.ui.viewmodel.MemoryDetailViewModel = hiltViewModel()
) {
    val memory by viewModel.memory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

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
                    text = stringResource(R.string.memory_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        else -> {
            val currentMemory = memory!!
            MemoryDetailContent(
                memory = currentMemory,
                cofreContextName = cofreContextName,
                cofreCreatorDisplayName = cofreCreatorDisplayName,
                onBack = onBack,
                onEdit = { onEdit(currentMemory.id) },
                onDelete = {
                    viewModel.delete(onSuccess = onDelete)
                }
            )
        }
    }
}

@Composable
private fun MemoryDetailContent(
    memory: com.cocido.nonna.ui.components.MemoryUiModel,
    cofreContextName: String?,
    cofreCreatorDisplayName: String?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val locale = remember { Locale.getDefault() }
    val displayDate = remember(memory.date, locale) {
        MemoryDetailShareFormatter.formatDisplayDate(memory.date, locale)
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val photoUrls = remember(memory.carouselImageUrls, memory.thumbnailUrl, memory.mainMediaUrl) {
        if (memory.carouselImageUrls.isNotEmpty()) {
            memory.carouselImageUrls
        } else {
            val fromThumb = MemoryDetailShareFormatter.splitPhotoUrls(memory.thumbnailUrl)
            if (fromThumb.isNotEmpty()) fromThumb
            else MemoryDetailShareFormatter.splitPhotoUrls(memory.mainMediaUrl)
        }
    }
    var showPhotoViewer by remember { mutableStateOf(false) }
    var fullscreenStartPage by remember { mutableIntStateOf(0) }
    var fallbackTextContent by remember(memory.id) { mutableStateOf<String?>(null) }
    var isLoadingFallbackText by remember(memory.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var sharingPolaroid by remember { mutableStateOf(false) }

    DisposableEffect(memory.id) {
        onDispose { sharingPolaroid = false }
    }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = NonnaDimens.screenPaddingHorizontal, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp, end = 8.dp)
            ) {
                Text(
                    text = memory.title.ifBlank { stringResource(R.string.memory_detail_title) },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!cofreContextName.isNullOrBlank()) {
                    Text(
                        text = cofreContextName.trim(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = NonnaDimens.screenPaddingHorizontal)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = memoryTypeIcon(memory.type),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = MemoryDetailShareFormatter.mediaKindLabel(context, memory.type),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (memory.emotionalTag != null || !memory.emotionalCustomLabel.isNullOrBlank()) {
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        when {
                            memory.emotionalTag != null -> EmotionalTagBadge(tag = memory.emotionalTag)
                            else -> CustomEmotionBadge(text = memory.emotionalCustomLabel.orEmpty())
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        enabled = !sharingPolaroid,
                        onClick = {
                            sharingPolaroid = true
                            scope.launch {
                                try {
                                    val file = try {
                                        MemorySharePolaroidGenerator.generate(
                                            context = context,
                                            memory = memory,
                                            cofreContextName = cofreContextName,
                                            cofreCreatorDisplayName = cofreCreatorDisplayName,
                                            locale = locale
                                        )
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (_: Exception) {
                                        null
                                    }
                                    if (!isActive) return@launch

                                    val activity = context.findActivityForShare()
                                    if (activity == null) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.memory_share_image_error),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@launch
                                    }

                                    if (file != null) {
                                        val uri = FileProvider.getUriForFile(
                                            activity,
                                            "${activity.packageName}.fileprovider",
                                            file
                                        )
                                        val builder = ShareCompat.IntentBuilder(activity)
                                            .setType("image/jpeg")
                                            .setStream(uri)
                                            .setSubject(
                                                memory.title.ifBlank { activity.getString(R.string.memory_detail_title) }
                                            )
                                            .setChooserTitle(activity.getString(R.string.memory_share_chooser_title))
                                        builder.intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        builder.intent.clipData = ClipData.newUri(
                                            activity.contentResolver,
                                            activity.getString(R.string.memory_share_cd),
                                            uri
                                        )
                                        runCatching {
                                            builder.startChooser()
                                        }.onFailure {
                                            Toast.makeText(
                                                activity,
                                                activity.getString(R.string.memory_share_image_error),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        Toast.makeText(
                                            activity,
                                            activity.getString(R.string.memory_share_image_error),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } finally {
                                    // Si la pantalla se desmontó o la corrutina se canceló, no tocar estado de Compose
                                    // (evita crash al cerrar el chooser sin elegir app o al volver atrás durante la generación).
                                    if (isActive) {
                                        sharingPolaroid = false
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = stringResource(R.string.memory_share_cd),
                            tint = if (sharingPolaroid) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.common_edit),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.common_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = memory.title.ifBlank { stringResource(R.string.memory_detail_title) },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!cofreCreatorDisplayName.isNullOrBlank()) {
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.memory_added_by, cofreCreatorDisplayName.trim()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }

            if (memory.type != MemoryType.Text && !memory.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = memory.description.trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (memory.type == MemoryType.Audio && !memory.audioUrl.isNullOrBlank()) {
                SpotifyStyleAudioPlayer(
                    audioUrl = memory.audioUrl,
                    title = memory.title.ifBlank { stringResource(R.string.memory_detail_title) },
                    subtitle = memory.description ?: stringResource(R.string.memory_audio_fallback_subtitle),
                    coverUrl = memory.audioCoverUrl?.takeIf { MemoryMediaUrlHeuristics.isDisplayableImageUrl(it) }
                        ?: memory.thumbnailUrl?.takeIf { MemoryMediaUrlHeuristics.isDisplayableImageUrl(it) },
                    showInlineHeader = false
                )
            } else if (memory.type == MemoryType.Photo && photoUrls.isNotEmpty()) {
                MemoryPhotoCarouselSection(
                    memoryId = memory.id,
                    photoUrls = photoUrls,
                    memoryTitle = memory.title.ifBlank { stringResource(R.string.memory_detail_title) },
                    onOpenFullscreen = { page ->
                        fullscreenStartPage = page
                        showPhotoViewer = true
                    }
                )
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
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PlaceholderCover(
                            type = memory.type,
                            compact = false,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

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
                            text = stringResource(R.string.common_content),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when {
                                !textContent.isNullOrBlank() -> textContent
                                isLoadingFallbackText -> "Cargando contenido del archivo..."
                                else -> stringResource(R.string.memory_text_no_visible_content)
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
                        text = stringResource(R.string.memory_family_comments_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.memory_family_comments_coming_soon),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    text = stringResource(R.string.memory_detail_info_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.memory_delete_confirm_title)) },
            text = { Text(stringResource(R.string.memory_delete_confirm_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.common_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (showPhotoViewer && photoUrls.isNotEmpty()) {
        PhotoFullScreenViewer(
            imageUrls = photoUrls,
            initialPage = fullscreenStartPage.coerceIn(0, photoUrls.lastIndex),
            onDismiss = { showPhotoViewer = false }
        )
    }
}

@Composable
private fun MemoryPhotoCarouselSection(
    memoryId: String,
    photoUrls: List<String>,
    memoryTitle: String,
    onOpenFullscreen: (pageIndex: Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { photoUrls.size })
    LaunchedEffect(memoryId, photoUrls) {
        pagerState.scrollToPage(0)
    }
    var showTapHint by remember { mutableStateOf(true) }
    LaunchedEffect(pagerState.settledPage) {
        showTapHint = true
        delay(5000)
        showTapHint = false
    }
    val hintText = if (photoUrls.size > 1) {
        stringResource(R.string.memory_photo_swipe_expand)
    } else {
        stringResource(R.string.memory_tap_to_expand)
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = NonnaCorners.Large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f),
                    beyondViewportPageCount = 1,
                    verticalAlignment = Alignment.CenterVertically
                ) { page ->
                    AsyncImage(
                        model = photoUrls[page],
                        contentDescription = memoryTitle,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onOpenFullscreen(page) },
                        contentScale = ContentScale.Crop
                    )
                }
                if (showTapHint) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = hintText,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            maxLines = 2
                        )
                    }
                }
            }
        }
        if (photoUrls.size > 1) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(photoUrls.size) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (selected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                                }
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.memory_photos_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(photoUrls.size) { index ->
                    val url = photoUrls[index]
                    val selected = pagerState.currentPage == index
                    Card(
                        modifier = Modifier
                            .size(72.dp)
                            .then(
                                if (selected) {
                                    Modifier.border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = NonnaCorners.Medium
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .clickable {
                                scope.launch { pagerState.animateScrollToPage(index) }
                            },
                        shape = NonnaCorners.Medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = stringResource(R.string.memory_photo_cd),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

private fun memoryTypeIcon(type: MemoryType): ImageVector = when (type) {
    MemoryType.Photo -> Icons.Outlined.Image
    MemoryType.Audio -> Icons.Outlined.AudioFile
    MemoryType.Text -> Icons.Outlined.Description
}

@Composable
private fun PhotoFullScreenViewer(
    imageUrls: List<String>,
    initialPage: Int,
    onDismiss: () -> Unit
) {
    val urls = remember(imageUrls) { imageUrls.filter { it.isNotBlank() } }
    if (urls.isEmpty()) return

    if (urls.size == 1) {
        PhotoFullScreenViewerSingle(imageUrl = urls.first(), onDismiss = onDismiss)
    } else {
        val start = initialPage.coerceIn(0, urls.lastIndex)
        val pagerState = rememberPagerState(
            initialPage = start,
            pageCount = { urls.size }
        )
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1,
                    verticalAlignment = Alignment.CenterVertically
                ) { page ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = urls[page],
                            contentDescription = stringResource(R.string.memory_fullscreen_image_cd),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = false) {},
                            contentScale = ContentScale.Fit
                        )
                    }
                }
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
                        contentDescription = stringResource(R.string.common_close_viewer),
                        tint = Color.White
                    )
                }
                Text(
                    text = stringResource(
                        R.string.memory_photo_page_counter,
                        pagerState.settledPage + 1,
                        urls.size
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 28.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.4f),
                            shape = NonnaCorners.Full
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun PhotoFullScreenViewerSingle(
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
                contentDescription = stringResource(R.string.memory_fullscreen_image_cd),
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
                    contentDescription = stringResource(R.string.common_close_viewer),
                    tint = Color.White
                )
            }
        }
    }
}

/** Zona magnética más angosta = snap más discreto y suave al tacto. */
private const val AUDIO_SEEK_EDGE_SNAP_FRACTION = 0.06f

private fun snapAudioSeekFraction(fraction: Float): Float = when {
    fraction <= AUDIO_SEEK_EDGE_SNAP_FRACTION -> 0f
    fraction >= 1f - AUDIO_SEEK_EDGE_SNAP_FRACTION -> 1f
    else -> fraction.coerceIn(0f, 1f)
}

@Composable
private fun SpotifyStyleAudioPlayer(
    audioUrl: String,
    title: String,
    subtitle: String,
    coverUrl: String?,
    showInlineHeader: Boolean = true
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableStateOf(0L) }
    var totalDurationMs by remember { mutableStateOf(0L) }
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubPositionMs by remember { mutableStateOf(0L) }
    var snapVisualEdge by remember { mutableStateOf<Int?>(null) }
    var snapScaleTarget by remember { mutableFloatStateOf(1f) }
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
                    currentPositionMs = totalDurationMs
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
        while (isPlaying && !isScrubbing) {
            val duration = exoPlayer.duration.coerceAtLeast(0L)
            val position = exoPlayer.currentPosition.coerceAtLeast(0L)
            currentPositionMs = position
            totalDurationMs = duration
            delay(250)
        }
    }

    LaunchedEffect(snapVisualEdge) {
        if (snapVisualEdge == null) return@LaunchedEffect
        delay(260)
        snapVisualEdge = null
    }

    LaunchedEffect(snapScaleTarget) {
        if (snapScaleTarget <= 1.008f) return@LaunchedEffect
        delay(200)
        snapScaleTarget = 1f
    }

    val snapScaleAnimated by animateFloatAsState(
        targetValue = snapScaleTarget,
        animationSpec = tween(durationMillis = 200),
        label = "audioSnapScale"
    )
    val snapEdgeFlashAlpha by animateFloatAsState(
        targetValue = if (snapVisualEdge != null) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "audioSnapEdgeFlash"
    )

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
                            contentDescription = stringResource(R.string.memory_audio_cover_cd),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        PlaceholderCover(
                            type = MemoryType.Audio,
                            compact = false,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                if (showInlineHeader) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                val displayedPositionMs = if (isScrubbing) scrubPositionMs else currentPositionMs
                val displayedFraction = if (totalDurationMs > 0L) {
                    (displayedPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f
                val animatedDisplayedFraction by animateFloatAsState(
                    targetValue = displayedFraction,
                    animationSpec = tween(durationMillis = 140),
                    label = "audioSeekFraction"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .graphicsLayer {
                            scaleX = snapScaleAnimated
                            scaleY = 1f
                        }
                ) {
                    if (snapVisualEdge == 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .width(16.dp)
                                .fillMaxHeight()
                                .clip(NonnaCorners.Medium)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.18f * snapEdgeFlashAlpha.coerceIn(0f, 1f)
                                    )
                                )
                        )
                    }
                    if (snapVisualEdge == 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(16.dp)
                                .fillMaxHeight()
                                .clip(NonnaCorners.Medium)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.18f * snapEdgeFlashAlpha.coerceIn(0f, 1f)
                                    )
                                )
                        )
                    }
                    Slider(
                        value = if (isScrubbing) displayedFraction else animatedDisplayedFraction,
                        onValueChange = { fraction ->
                            isScrubbing = true
                            val snapped = snapAudioSeekFraction(fraction)
                            scrubPositionMs = if (totalDurationMs > 0L) {
                                (snapped * totalDurationMs.toFloat()).toLong().coerceIn(0L, totalDurationMs)
                            } else 0L
                        },
                        onValueChangeFinished = {
                            if (totalDurationMs <= 0L) {
                                isScrubbing = false
                            } else {
                                val rawFraction =
                                    (scrubPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
                                val snappedFraction = snapAudioSeekFraction(rawFraction)
                                val didSnap = snappedFraction != rawFraction
                                val targetMs =
                                    (snappedFraction * totalDurationMs.toFloat()).toLong()
                                        .coerceIn(0L, totalDurationMs)
                                exoPlayer.seekTo(targetMs)
                                currentPositionMs = targetMs
                                if (didSnap) {
                                    snapVisualEdge = if (snappedFraction <= 0f) 0 else 1
                                    snapScaleTarget = 1.018f
                                    haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                                } else {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                isScrubbing = false
                            }
                        },
                        enabled = totalDurationMs > 0L,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatPlaybackTime(displayedPositionMs),
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
                        Icon(Icons.Outlined.SkipPrevious, contentDescription = stringResource(R.string.memory_rewind_15_cd))
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
                        Icon(Icons.Outlined.SkipNext, contentDescription = stringResource(R.string.memory_forward_15_cd))
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

private fun formatPlaybackTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

private tailrec fun Context.findActivityForShare(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivityForShare()
    else -> null
}

