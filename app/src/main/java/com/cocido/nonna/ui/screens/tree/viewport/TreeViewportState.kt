package com.cocido.nonna.ui.screens.tree.viewport

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer

@Stable
class TreeViewportState(
    initialZoom: Float = 1f,
    initialPanX: Float = 0f,
    initialPanY: Float = 0f
) {
    var zoom by mutableFloatStateOf(initialZoom)
        private set
    var panX by mutableFloatStateOf(initialPanX)
        private set
    var panY by mutableFloatStateOf(initialPanY)
        private set

    fun onTransform(panDx: Float, panDy: Float, zoomChange: Float) {
        zoom = (zoom * zoomChange).coerceIn(0.5f, 3f)
        panX += panDx
        panY += panDy
    }

    fun zoomIn() {
        zoom = (zoom + 0.1f).coerceAtMost(3f)
    }

    fun zoomOut() {
        zoom = (zoom - 0.1f).coerceAtLeast(0.5f)
    }
}

@Composable
fun rememberTreeViewportState(): TreeViewportState = remember { TreeViewportState() }
