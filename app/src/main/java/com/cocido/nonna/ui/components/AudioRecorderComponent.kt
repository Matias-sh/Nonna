package com.cocido.nonna.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.PrimaryGradientEnd
import com.cocido.nonna.ui.theme.PrimaryGradientStart
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class RecorderState {
    Idle, Recording, Paused, Recorded, Playing
}

@Composable
fun AudioRecorderComponent(
    onRecordingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var state by remember { mutableStateOf(RecorderState.Idle) }
    var durationSeconds by remember { mutableIntStateOf(0) }
    
    // Timer for recording
    LaunchedEffect(state) {
        if (state == RecorderState.Recording) {
            while (state == RecorderState.Recording) {
                delay(1000)
                durationSeconds++
            }
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = NonnaCorners.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(NonnaDimens.cardPaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Waveform visualization
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(NonnaCorners.Medium)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                PrimaryGradientStart.copy(alpha = 0.1f),
                                PrimaryGradientEnd.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    RecorderState.Recording -> WaveformAnimation()
                    RecorderState.Recorded, RecorderState.Playing -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Audio grabado",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Listo para grabar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Duration display
            Text(
                text = formatDuration(durationSeconds),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (state == RecorderState.Recording) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Grabando...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (state == RecorderState.Paused) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pausado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (state) {
                    RecorderState.Idle -> {
                        // Record button
                        IconButton(
                            onClick = { state = RecorderState.Recording },
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Grabar",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    
                    RecorderState.Recording -> {
                        // Pause button
                        IconButton(
                            onClick = { state = RecorderState.Paused },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pausar",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                        
                        // Stop button
                        IconButton(
                            onClick = {
                                state = RecorderState.Recorded
                                onRecordingComplete()
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.error,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Detener",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                    
                    RecorderState.Paused -> {
                        // Resume button
                        IconButton(
                            onClick = { state = RecorderState.Recording },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Reanudar",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                        
                        // Stop button
                        IconButton(
                            onClick = {
                                state = RecorderState.Recorded
                                onRecordingComplete()
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.error,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Detener",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                    
                    RecorderState.Recorded, RecorderState.Playing -> {
                        // Play/Pause button
                        IconButton(
                            onClick = {
                                state = if (state == RecorderState.Playing) {
                                    RecorderState.Recorded
                                } else {
                                    RecorderState.Playing
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (state == RecorderState.Playing) {
                                    Icons.Default.Pause
                                } else {
                                    Icons.Default.PlayArrow
                                },
                                contentDescription = if (state == RecorderState.Playing) "Pausar" else "Reproducir",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                        
                        // Delete button
                        IconButton(
                            onClick = {
                                state = RecorderState.Idle
                                durationSeconds = 0
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        // Save button
                        NonnaButton(
                            text = "Guardar audio",
                            onClick = onRecordingComplete
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WaveformAnimation() {
    Row(
        modifier = Modifier.height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(20) { index ->
            var height by remember { mutableStateOf(Random.nextFloat() * 0.5f + 0.2f) }
            
            LaunchedEffect(Unit) {
                while (true) {
                    delay(100)
                    height = Random.nextFloat() * 0.8f + 0.2f
                }
            }
            
            val animatedHeight by animateFloatAsState(
                targetValue = height,
                animationSpec = tween(100),
                label = "waveform"
            )
            
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((48.dp * animatedHeight))
                    .clip(NonnaCorners.Full)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", mins, secs)
}
