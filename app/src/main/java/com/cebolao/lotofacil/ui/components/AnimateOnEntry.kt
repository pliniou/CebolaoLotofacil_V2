package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

private const val DEFAULT_ANIMATION_DURATION_MS = 400
private const val INITIAL_OFFSET_DIVISOR = 8

/**
 * Um wrapper que anima a entrada de seu conteúdo na tela.
 * A animação pode ser controlada globalmente através do `LocalAnimationEnabled`.
 */
@Composable
fun AnimateOnEntry(
    modifier: Modifier = Modifier,
    delayMillis: Long = 0,
    durationMillis: Int = DEFAULT_ANIMATION_DURATION_MS,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (delayMillis > 0) delay(delayMillis)
        isVisible = true
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it / INITIAL_OFFSET_DIVISOR },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(animationSpec = tween(durationMillis)),
        exit = slideOutVertically() + fadeOut()
    ) {
        content()
    }
}