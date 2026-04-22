package com.cocido.nonna.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cocido.nonna.R
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.PrimaryGradientEnd
import com.cocido.nonna.ui.theme.PrimaryGradientStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import java.io.File

enum class RecorderState {
    Idle, Recording, Paused, Recorded, Playing
}

@Composable
fun AudioRecorderComponent(
    onRecordingComplete: (file: File, durationSeconds: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf(RecorderState.Idle) }
    var durationSeconds by remember { mutableIntStateOf(0) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var feedbackVisible by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }

    fun startRecording() {
        runCatching {
            val outputFile = File.createTempFile("nonna_audio_", ".m4a", context.cacheDir)
            val localRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            recorder = localRecorder
            recordedFile = outputFile
            durationSeconds = 0
            state = RecorderState.Recording
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startRecording()
        } else {
            feedbackMessage = "Necesitás habilitar el micrófono para grabar audio"
            feedbackVisible = true
            scope.launch {
                delay(1800)
                feedbackVisible = false
            }
        }
    }
    
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
                                text = stringResource(R.string.memory_audio_recorded),
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
                                text = stringResource(R.string.audio_recorder_ready),
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
                    text = stringResource(R.string.audio_recorder_recording),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (state == RecorderState.Paused) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.audio_recorder_paused),
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
                            onClick = {
                                val hasAudioPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!hasAudioPermission) {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    startRecording()
                                }
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = stringResource(R.string.common_record),
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    
                    RecorderState.Recording -> {
                        // Pause button
                        IconButton(
                            onClick = {
                                runCatching { recorder?.pause() }
                                state = RecorderState.Paused
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = stringResource(R.string.common_pause),
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                        
                        // Stop button
                        IconButton(
                            onClick = {
                                runCatching {
                                    recorder?.stop()
                                    recorder?.release()
                                }
                                recorder = null
                                state = RecorderState.Recorded
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
                                contentDescription = stringResource(R.string.common_stop),
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                    
                    RecorderState.Paused -> {
                        // Resume button
                        IconButton(
                            onClick = {
                                runCatching { recorder?.resume() }
                                state = RecorderState.Recording
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = stringResource(R.string.common_resume),
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                        
                        // Stop button
                        IconButton(
                            onClick = {
                                runCatching {
                                    recorder?.stop()
                                    recorder?.release()
                                }
                                recorder = null
                                state = RecorderState.Recorded
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
                                contentDescription = stringResource(R.string.common_stop),
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                    
                    RecorderState.Recorded, RecorderState.Playing -> {
                        // Play/Pause button
                        IconButton(
                            onClick = {
                                if (state == RecorderState.Playing) {
                                    runCatching { player?.pause() }
                                    state = RecorderState.Recorded
                                } else {
                                    val targetFile = recordedFile
                                    if (targetFile != null) {
                                        runCatching {
                                            player?.release()
                                            player = MediaPlayer().apply {
                                                setDataSource(targetFile.absolutePath)
                                                prepare()
                                                setOnCompletionListener {
                                                    state = RecorderState.Recorded
                                                }
                                                start()
                                            }
                                            state = RecorderState.Playing
                                        }
                                    }
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
                                contentDescription = if (state == RecorderState.Playing) {
                                    stringResource(R.string.common_pause)
                                } else {
                                    stringResource(R.string.common_play)
                                },
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                        
                        // Delete button
                        IconButton(
                            onClick = {
                                runCatching {
                                    recorder?.release()
                                    player?.release()
                                }
                                recorder = null
                                player = null
                                recordedFile?.delete()
                                recordedFile = null
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
                                contentDescription = stringResource(R.string.common_delete),
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        // Save button
                        NonnaButton(
                            text = stringResource(R.string.audio_recorder_save_audio),
                            onClick = {
                                recordedFile?.let { file ->
                                    onRecordingComplete(file, durationSeconds)
                                }
                            },
                            enabled = recordedFile != null
                        )
                    }
                }
            }

            NonnaBottomFeedbackBanner(
                visible = feedbackVisible,
                message = feedbackMessage,
                type = NonnaFeedbackType.Error,
                includeNavigationBarsPadding = false
            )
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
