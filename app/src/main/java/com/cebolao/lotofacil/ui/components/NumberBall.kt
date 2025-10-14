package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cebolao.lotofacil.ui.theme.Sizes

private object NumberBallConstants {
    val ELEVATION_SELECTED = 4.dp
    val ELEVATION_DEFAULT = 1.dp
    val BORDER_WIDTH = 1.dp
    const val FONT_SIZE_FACTOR = 3.2f
    const val ANIMATION_DURATION_MS = 250
}

enum class NumberBallVariant { Primary, Secondary, Lotofacil }

@Composable
fun NumberBall(
    number: Int,
    modifier: Modifier = Modifier,
    size: Dp = Sizes.NumberBall,
    isSelected: Boolean = false,
    isHighlighted: Boolean = false,
    isDisabled: Boolean = false,
    variant: NumberBallVariant = NumberBallVariant.Primary
) {
    val elevation by animateDpAsState(
        targetValue = if (isSelected) NumberBallConstants.ELEVATION_SELECTED else NumberBallConstants.ELEVATION_DEFAULT,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "ballElevation"
    )

    val (containerColor, contentColor, borderColor) = rememberBallColors(
        isSelected = isSelected,
        isHighlighted = isHighlighted,
        isDisabled = isDisabled,
        variant = variant
    )

    val animatedContainer by animateColorAsState(containerColor, tween(NumberBallConstants.ANIMATION_DURATION_MS), label = "ballContainer")
    val animatedContent by animateColorAsState(contentColor, tween(NumberBallConstants.ANIMATION_DURATION_MS), label = "ballContent")
    val animatedBorder by animateColorAsState(borderColor, tween(NumberBallConstants.ANIMATION_DURATION_MS), label = "ballBorder")

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = elevation,
                shape = CircleShape,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        animatedContainer,
                        animatedContainer.copy(alpha = 0.85f)
                    )
                ),
                shape = CircleShape
            )
            .border(
                width = NumberBallConstants.BORDER_WIDTH,
                color = animatedBorder,
                shape = CircleShape
            )
            .semantics {
                contentDescription = "Número %02d, %s".format(
                    number, when {
                        isSelected -> "selecionado"
                        isHighlighted -> "destacado"
                        isDisabled -> "desabilitado"
                        else -> "disponível"
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "%02d".format(number),
            color = animatedContent,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = (size.value / NumberBallConstants.FONT_SIZE_FACTOR).sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun rememberBallColors(
    isSelected: Boolean,
    isHighlighted: Boolean,
    isDisabled: Boolean,
    variant: NumberBallVariant
): Triple<Color, Color, Color> {
    val primaryTone = when (variant) {
        NumberBallVariant.Primary -> MaterialTheme.colorScheme.primary
        NumberBallVariant.Secondary -> MaterialTheme.colorScheme.secondary
        NumberBallVariant.Lotofacil -> MaterialTheme.colorScheme.tertiary
    }

    return when {
        isDisabled -> Triple(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
        isSelected -> Triple(
            primaryTone,
            MaterialTheme.colorScheme.onPrimary,
            primaryTone.copy(alpha = 0.3f)
        )
        isHighlighted -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    }
}