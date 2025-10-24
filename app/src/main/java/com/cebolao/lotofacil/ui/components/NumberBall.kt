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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.util.DEFAULT_NUMBER_FORMAT

enum class NumberBallVariant { Primary, Secondary, Lotofacil }

@Composable
fun NumberBall(
    number: Int,
    modifier: Modifier = Modifier,
    size: Dp = Dimen.NumberBall,
    isSelected: Boolean = false,
    isHighlighted: Boolean = false,
    isDisabled: Boolean = false,
    variant: NumberBallVariant = NumberBallVariant.Primary
) {
    val elevation by animateDpAsState(
        targetValue = if (isSelected) Dimen.Elevation.Level4 else Dimen.Elevation.Level1,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ballElevation"
    )

    val (containerColors, contentColor, borderColor) = rememberBallColors(
        isSelected = isSelected,
        isHighlighted = isHighlighted,
        isDisabled = isDisabled,
        variant = variant
    )

    val animatedContainerStart by animateColorAsState(
        containerColors.first,
        tween(AppConfig.Animation.ShortDuration),
        label = "ballContainerStart"
    )
    val animatedContainerEnd by animateColorAsState(
        containerColors.second,
        tween(AppConfig.Animation.ShortDuration),
        label = "ballContainerEnd"
    )
    val animatedContent by animateColorAsState(
        contentColor,
        tween(AppConfig.Animation.ShortDuration),
        label = "ballContent"
    )
    val animatedBorder by animateColorAsState(
        borderColor,
        tween(AppConfig.Animation.ShortDuration),
        label = "ballBorder"
    )

    val stateDescription = when {
        isSelected -> stringResource(R.string.number_ball_state_selected)
        isHighlighted -> stringResource(R.string.number_ball_state_highlighted)
        isDisabled -> stringResource(R.string.number_ball_state_disabled)
        else -> stringResource(R.string.number_ball_state_available)
    }
    val numberFormatted = DEFAULT_NUMBER_FORMAT.format(number)
    val fullContentDescription = stringResource(R.string.number_ball_content_description, numberFormatted, stateDescription)

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = elevation,
                shape = CircleShape,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = AppConfig.UI.NumberBallShadowSpotAlpha)
            )
            .clip(CircleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(animatedContainerStart, animatedContainerEnd)
                )
            )
            .border(
                width = Dimen.Border.Default,
                color = animatedBorder,
                shape = CircleShape
            )
            .semantics {
                contentDescription = fullContentDescription
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = numberFormatted,
            color = animatedContent,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = (size.value / AppConfig.UI.NumberBallFontSizeFactor).sp,
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
): Triple<Pair<Color, Color>, Color, Color> {
    val primaryTone = when (variant) {
        NumberBallVariant.Primary -> MaterialTheme.colorScheme.primary
        NumberBallVariant.Secondary -> MaterialTheme.colorScheme.secondary
        NumberBallVariant.Lotofacil -> MaterialTheme.colorScheme.tertiary
    }

    val primaryToneContainer = when (variant) {
        NumberBallVariant.Primary -> MaterialTheme.colorScheme.primaryContainer
        NumberBallVariant.Secondary -> MaterialTheme.colorScheme.secondaryContainer
        NumberBallVariant.Lotofacil -> MaterialTheme.colorScheme.tertiaryContainer
    }

    val onPrimaryToneContainer = when (variant) {
        NumberBallVariant.Primary -> MaterialTheme.colorScheme.onPrimaryContainer
        NumberBallVariant.Secondary -> MaterialTheme.colorScheme.onSecondaryContainer
        NumberBallVariant.Lotofacil -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    return when {
        isDisabled -> Triple(
            Pair(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppConfig.UI.NumberBallDisabledAlpha),
                MaterialTheme.colorScheme.surface.copy(alpha = AppConfig.UI.NumberBallDisabledAlpha)
            ),
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AppConfig.UI.NumberBallDisabledAlpha),
            MaterialTheme.colorScheme.outline.copy(alpha = AppConfig.UI.NumberBallBorderAlphaDisabled)
        )
        isSelected -> Triple(
            Pair(primaryTone, primaryTone.copy(alpha = AppConfig.UI.NumberBallGradientAlpha)),
            MaterialTheme.colorScheme.onPrimary,
            primaryTone.copy(alpha = AppConfig.UI.NumberBallBorderAlphaSelected)
        )
        isHighlighted -> Triple(
            Pair(primaryToneContainer, primaryToneContainer.copy(alpha = AppConfig.UI.NumberBallGradientAlpha)),
            onPrimaryToneContainer,
            primaryTone.copy(alpha = AppConfig.UI.NumberBallBorderAlphaHighlighted)
        )
        else -> Triple(
            Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface),
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.outline.copy(alpha = AppConfig.UI.NumberBallBorderAlphaDefault)
        )
    }
}