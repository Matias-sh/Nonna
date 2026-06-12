package com.cocido.nonna.ui.screens.memory

import android.net.Uri
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.cocido.nonna.R
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.theme.NonnaCorners
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> MemoryPhotoSelectionContent(
    selectedPhotos: List<T>,
    maxPhotos: Int,
    onPhotosChanged: (List<T>) -> Unit,
    onPickGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onCropPhoto: (index: Int) -> Unit = {}
) {
    var showSourceSheet by remember { mutableStateOf(false) }
    var previewIndex by remember { mutableIntStateOf(0) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val canAddMore = selectedPhotos.size < maxPhotos
    val openSourceSheet = { showSourceSheet = true }
    val hapticFeedback = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        if (from.index !in selectedPhotos.indices || to.index !in selectedPhotos.indices) return@rememberReorderableLazyListState
        val reordered = selectedPhotos.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        previewIndex = when {
            from.index == previewIndex -> to.index
            from.index < previewIndex && to.index >= previewIndex -> previewIndex - 1
            from.index > previewIndex && to.index <= previewIndex -> previewIndex + 1
            else -> previewIndex
        }
        onPhotosChanged(reordered)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    LaunchedEffect(selectedPhotos) {
        if (selectedPhotos.isEmpty()) {
            previewIndex = 0
        } else if (previewIndex >= selectedPhotos.size) {
            previewIndex = selectedPhotos.lastIndex
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.memory_photos_select_hint, maxPhotos),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (selectedPhotos.isEmpty()) {
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
                    .clickable(onClick = openSourceSheet),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.memory_photos_add_slot),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.memory_tap_choose_gallery),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 5f)
                    .clip(NonnaCorners.Large)
                    .clickable(onClick = openSourceSheet)
            ) {
                AsyncImage(
                    model = selectedPhotos[previewIndex],
                    contentDescription = stringResource(R.string.memory_selected_image_cd),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (previewIndex == 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        shape = NonnaCorners.Full,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
                    ) {
                        Text(
                            text = stringResource(R.string.memory_photos_main_badge),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
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
                IconButton(
                    onClick = { onCropPhoto(previewIndex) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            shape = NonnaCorners.Full
                        )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Crop,
                        contentDescription = stringResource(R.string.photo_crop_action),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = stringResource(
                    R.string.memory_photos_count,
                    selectedPhotos.size,
                    maxPhotos
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (selectedPhotos.size > 1) {
                Text(
                    text = stringResource(R.string.memory_photos_drag_reorder),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyRow(
                state = lazyListState,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = selectedPhotos,
                    key = { _, photo -> photoItemKey(photo) }
                ) { index, photo ->
                    ReorderableItem(
                        state = reorderableState,
                        key = photoItemKey(photo)
                    ) { isDragging ->
                        val scale by animateFloatAsState(
                            targetValue = if (isDragging) 1.08f else 1f,
                            label = "photoDragScale"
                        )
                        val elevation by animateDpAsState(
                            targetValue = if (isDragging) 8.dp else 0.dp,
                            label = "photoDragElevation"
                        )
                        val isMain = index == 0
                        Surface(
                            modifier = Modifier
                                .size(72.dp)
                                .zIndex(if (isDragging) 1f else 0f)
                                .longPressDraggableHandle(
                                    onDragStarted = {
                                        hapticFeedback.performHapticFeedback(
                                            HapticFeedbackType.GestureThresholdActivate
                                        )
                                    },
                                    onDragStopped = {
                                        hapticFeedback.performHapticFeedback(
                                            HapticFeedbackType.GestureEnd
                                        )
                                    }
                                ),
                            shape = NonnaCorners.Medium,
                            shadowElevation = elevation,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(
                                        width = if (isMain) 2.dp else 1.dp,
                                        color = if (isMain) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                        },
                                        shape = NonnaCorners.Medium
                                    )
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                    .clip(NonnaCorners.Medium)
                                    .clickable { previewIndex = index }
                            ) {
                                AsyncImage(
                                    model = photo,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = {
                                        val newPhotos = selectedPhotos.filterIndexed { i, _ -> i != index }
                                        previewIndex = when {
                                            newPhotos.isEmpty() -> 0
                                            previewIndex == index -> 0
                                            previewIndex > index -> previewIndex - 1
                                            else -> previewIndex
                                        }
                                        onPhotosChanged(newPhotos)
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                            shape = NonnaCorners.Full
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = stringResource(R.string.memory_photo_remove_cd),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                if (canAddMore) {
                    item(key = "memory_photo_add_slot") {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(NonnaCorners.Medium)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = NonnaCorners.Medium
                                )
                                .clickable(onClick = openSourceSheet),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = stringResource(R.string.memory_photos_add_slot),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSourceSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.memory_photo_source_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                NonnaButton(
                    text = stringResource(R.string.memory_photo_source_gallery),
                    onClick = {
                        showSourceSheet = false
                        onPickGallery()
                    },
                    icon = Icons.Outlined.PhotoLibrary,
                    fullWidth = true,
                    enabled = canAddMore
                )
                NonnaButton(
                    text = stringResource(R.string.memory_photo_source_camera),
                    onClick = {
                        showSourceSheet = false
                        onTakePhoto()
                    },
                    icon = Icons.Outlined.CameraAlt,
                    style = NonnaButtonStyle.Outline,
                    fullWidth = true,
                    enabled = canAddMore
                )
            }
        }
    }
}

private fun photoItemKey(item: Any): String = when (item) {
    is Uri -> "uri:$item"
    is String -> "url:${item.substringBefore('?')}"
    else -> "photo:${item.hashCode()}:${item.javaClass.simpleName}"
}
