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
import com.cebolao.lotofacil.ui.theme.LotofacilPurple

enum class NumberBallVariant {
    Primary, Secondary, Lotofacil
}

@Composable
fun NumberBall(
    number: Int,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    isSelected: Boolean = false,
    isHighlighted: Boolean = false,
    isDisabled: Boolean = false,
    variant: NumberBallVariant = NumberBallVariant.Primary
) {
    val elevation by animateDpAsState(
        targetValue = when {
            isSelected -> 4.dp
            isHighlighted -> 2.dp
            else -> 1.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "elevation"
    )

    val (containerColor, contentColor, borderColor) = getBallColors(
        isSelected = isSelected,
        isHighlighted = isHighlighted,
        isDisabled = isDisabled,
        variant = variant
    )

    val animatedContainerColor by animateColorAsState(containerColor, tween(250), label = "containerColor")
    val animatedContentColor by animateColorAsState(contentColor, tween(250), label = "contentColor")
    val animatedBorderColor by animateColorAsState(borderColor, tween(250), label = "borderColor")

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
                    colors = listOf(animatedContainerColor, animatedContainerColor.copy(alpha = 0.85f))
                ),
                shape = CircleShape
            )
            .border(
                width = if (isHighlighted && !isSelected) 1.5.dp else 0.5.dp,
                color = animatedBorderColor,
                shape = CircleShape
            )
            .semantics {
                val state = when {
                    isSelected -> "selecionado"
                    isHighlighted -> "destacado"
                    isDisabled -> "desabilitado"
                    else -> "disponível"
                }
                contentDescription = "Número $number, $state"
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "%02d".format(number),
            color = animatedContentColor,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = (size.value / 3.2).sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun getBallColors(
    isSelected: Boolean,
    isHighlighted: Boolean,
    isDisabled: Boolean,
    variant: NumberBallVariant
): Triple<Color, Color, Color> {
    val primaryColor = when (variant) {
        NumberBallVariant.Primary -> MaterialTheme.colorScheme.primary
        NumberBallVariant.Secondary -> MaterialTheme.colorScheme.secondary
        NumberBallVariant.Lotofacil -> LotofacilPurple
    }

    return when {
        variant == NumberBallVariant.Lotofacil -> Triple(
            LotofacilPurple,
            Color.White,
            LotofacilPurple.copy(alpha = 0.5f)
        )
        isSelected -> Triple(
            primaryColor,
            MaterialTheme.colorScheme.onPrimary,
            primaryColor.copy(alpha = 0.3f)
        )
        isDisabled -> Triple(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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