package com.cocido.nonna.ui.components

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * Dispara [onRefresh] al volver a primer plano, con intervalo mínimo para evitar ráfagas.
 */
@Composable
fun RefreshOnResume(
    minIntervalMs: Long = 1500L,
    onRefresh: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lastRefreshAt = remember { mutableLongStateOf(0L) }
    DisposableEffect(lifecycleOwner, minIntervalMs) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val now = SystemClock.elapsedRealtime()
                if (now - lastRefreshAt.longValue >= minIntervalMs) {
                    lastRefreshAt.longValue = now
                    onRefresh()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
