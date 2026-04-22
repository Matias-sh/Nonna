package com.cocido.nonna.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cocido.nonna.R
import com.cocido.nonna.ui.theme.NonnaCorners
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun NonnaImageEditorDialog(
    sourceUri: Uri,
    aspectRatio: Float,
    title: String,
    onDismiss: () -> Unit,
    onApply: (Uri) -> Unit
) {
    val context = LocalContext.current
    var scale by remember(sourceUri) { mutableFloatStateOf(1f) }
    var offset by remember(sourceUri) { mutableStateOf(Offset.Zero) }
    var viewportSize by remember(sourceUri) { mutableStateOf(IntSize.Zero) }
    var isProcessing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = {
            if (!isProcessing) onDismiss()
        },
        confirmButton = {
            NonnaButton(
                text = if (isProcessing) stringResource(R.string.common_applying) else stringResource(R.string.common_apply),
                onClick = {
                    if (isProcessing) return@NonnaButton
                    isProcessing = true
                    scope.launch {
                        val edited = createCroppedFile(
                            context = context,
                            uri = sourceUri,
                            viewportSize = viewportSize,
                            scale = scale,
                            offset = offset
                        )
                        isProcessing = false
                        if (edited != null) onApply(Uri.fromFile(edited)) else onDismiss()
                    }
                },
                enabled = viewportSize.width > 0 && viewportSize.height > 0 && !isProcessing
            )
        },
        dismissButton = {
            NonnaButton(
                text = stringResource(R.string.common_cancel),
                onClick = onDismiss,
                style = NonnaButtonStyle.Outline,
                enabled = !isProcessing
            )
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Crop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(title)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    val cropWidth = maxWidth * 0.9f
                    val cropHeight = cropWidth / aspectRatio

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(cropWidth)
                                .height(cropHeight)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), NonnaCorners.Large)
                                .onSizeChanged { viewportSize = it }
                        ) {
                            AsyncImage(
                                model = sourceUri,
                                contentDescription = stringResource(R.string.common_preview),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        translationX = offset.x
                                        translationY = offset.y
                                    }
                                    .pointerInput(scale, offset) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            val newScale = (scale * zoom).coerceIn(1f, 4f)
                                            val unclamped = offset + pan
                                            offset = clampOffset(unclamped, newScale, IntSize(size.width, size.height))
                                            scale = newScale
                                        }
                                    },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.common_zoom),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = scale,
                    onValueChange = {
                        scale = it
                        offset = clampOffset(offset, scale, viewportSize)
                    },
                    valueRange = 1f..4f
                )
                Text(
                    text = stringResource(R.string.image_editor_drag_zoom_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

private fun clampOffset(offset: Offset, scale: Float, viewportSize: IntSize): Offset {
    if (viewportSize.width <= 0 || viewportSize.height <= 0) return offset
    val maxX = ((scale - 1f) * viewportSize.width / 2f).coerceAtLeast(0f)
    val maxY = ((scale - 1f) * viewportSize.height / 2f).coerceAtLeast(0f)
    return Offset(
        x = offset.x.coerceIn(-maxX, maxX),
        y = offset.y.coerceIn(-maxY, maxY)
    )
}

private suspend fun createCroppedFile(
    context: Context,
    uri: Uri,
    viewportSize: IntSize,
    scale: Float,
    offset: Offset
): File? = withContext(Dispatchers.IO) {
    if (viewportSize.width <= 0 || viewportSize.height <= 0) return@withContext null
    runCatching {
        val input = context.contentResolver.openInputStream(uri) ?: return@runCatching null
        val sourceBitmap = input.use { BitmapFactory.decodeStream(it) } ?: return@runCatching null
        val outputBitmap = Bitmap.createBitmap(viewportSize.width, viewportSize.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val baseScale = maxOf(
            viewportSize.width.toFloat() / sourceBitmap.width.toFloat(),
            viewportSize.height.toFloat() / sourceBitmap.height.toFloat()
        )
        val finalScale = baseScale * scale
        val drawWidth = sourceBitmap.width * finalScale
        val drawHeight = sourceBitmap.height * finalScale
        val left = (viewportSize.width - drawWidth) / 2f + offset.x
        val top = (viewportSize.height - drawHeight) / 2f + offset.y
        val dst = android.graphics.RectF(left, top, left + drawWidth, top + drawHeight)
        canvas.drawBitmap(sourceBitmap, null, dst, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))

        File.createTempFile("nonna_edited_", ".jpg", context.cacheDir).apply {
            FileOutputStream(this).use { out ->
                outputBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
        }
    }.getOrNull()
}
