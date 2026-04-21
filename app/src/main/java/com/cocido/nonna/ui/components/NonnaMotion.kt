package com.cocido.nonna.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.delay

object NonnaMotion {
    const val DurationFastMs = 150
    const val DurationMediumMs = 250
    const val DurationScreenMs = 400
    const val StaggerStepMs = 60

    val interactionSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val buttonSpring = spring<Float>(
        dampingRatio = 0.86f,
        stiffness = Spring.StiffnessMedium
    )

    val bounceSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val navSpring = spring<Float>(
        dampingRatio = 0.82f,
        stiffness = Spring.StiffnessMedium
    )
    val screenFadeIn = tween<Float>(durationMillis = DurationScreenMs)
    val screenFadeOut = tween<Float>(durationMillis = DurationScreenMs)
}

@Composable
fun rememberMotionInteractionSource(): MutableInteractionSource = remember { MutableInteractionSource() }

@Composable
fun Modifier.nonnaInteractiveScale(
    interactionSource: MutableInteractionSource,
    base: Float = 1f,
    pressed: Float = 0.98f
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val targetScale = when {
        isPressed -> pressed
        else -> base
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = NonnaMotion.buttonSpring,
        label = "nonna_scale"
    )
    return this.scale(scale)
}

@Composable
fun NonnaStaggerItem(
    index: Int,
    baseDelayMs: Int = 0,
    stepDelayMs: Int = NonnaMotion.StaggerStepMs,
    content: @Composable () -> Unit
) {
    var visible by remember(index) { mutableStateOf(false) }
    LaunchedEffect(index) {
        delay((baseDelayMs + (index * stepDelayMs)).toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = NonnaMotion.DurationMediumMs)) +
            slideInVertically(
                initialOffsetY = { 16 },
                animationSpec = spring(
                    dampingRatio = 0.9f,
                    stiffness = Spring.StiffnessMedium
                )
            )
    ) {
        content()
    }
}
