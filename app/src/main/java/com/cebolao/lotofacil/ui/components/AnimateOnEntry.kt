package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.cebolao.lotofacil.ui.theme.LocalAnimationEnabled
import kotlinx.coroutines.delay

/**
 * Um wrapper que anima a entrada de seu conteúdo na tela.
 * A animação pode ser controlada globalmente através do `LocalAnimationEnabled`.
 */
@Composable
fun AnimateOnEntry(
    modifier: Modifier = Modifier,
    delayMillis: Long = 0,
    durationMillis: Int = 400,
    content: @Composable () -> Unit
) {
    val animationsEnabled = LocalAnimationEnabled.current
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (delayMillis > 0) delay(delayMillis)
        isVisible = true
    }

    if (animationsEnabled) {
        AnimatedVisibility(
            modifier = modifier,
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 8 }, // Anima de baixo para cima
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(animationSpec = tween(durationMillis)),
            exit = slideOutVertically() + fadeOut()
        ) {
            content()
        }
    } else {
        // Se as animações estiverem desabilitadas, apenas mostra o conteúdo.
        if (isVisible) {
            Box(modifier) {
                content()
            }
        }
    }
}