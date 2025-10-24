package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.toImmutableList

sealed class GameCardAction {
    data object Analyze : GameCardAction()
    data object Pin : GameCardAction()
    data object Delete : GameCardAction()
    data object Check : GameCardAction()
    data object Share : GameCardAction()
}

@Composable
fun GameCard(
    game: LotofacilGame,
    modifier: Modifier = Modifier,
    onAction: (GameCardAction) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isPinned = game.isPinned

    val elevation by animateDpAsState(
        targetValue = if (isPinned) Dimen.Elevation.Level3 else Dimen.Elevation.Level1,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "elevation"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isPinned) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline.copy(
            alpha = 0.2f
        ),
        animationSpec = tween(AppConfig.Animation.ShortDuration),
        label = "borderColor"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isPinned) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = AppConfig.UI.GameCardPinnedContainerAlpha)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        animationSpec = tween(AppConfig.Animation.ShortDuration),
        label = "containerColor"
    )

    SectionCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(elevation),
        border = BorderStroke(
            width = if (isPinned) Dimen.Border.Thick else Dimen.Border.Default,
            color = borderColor
        ),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        NumberGrid(
            allNumbers = game.numbers.sorted().toImmutableList(),
            selectedNumbers = game.numbers,
            onNumberClick = {},
            maxSelection = game.numbers.size,
            numberSize = Dimen.NumberBall,
            ballVariant = NumberBallVariant.Secondary
        )

        AppDivider()

        GameCardActions(
            isPinned = isPinned,
            onAction = { action ->
                val feedback = when (action) {
                    GameCardAction.Pin, GameCardAction.Delete -> HapticFeedbackType.LongPress
                    else -> HapticFeedbackType.TextHandleMove
                }
                haptic.performHapticFeedback(feedback)
                onAction(action)
            }
        )
    }
}

@Composable
private fun GameCardActions(
    isPinned: Boolean,
    onAction: (GameCardAction) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimen.ExtraSmallPadding)) {
            IconButton(onClick = { onAction(GameCardAction.Pin) }) {
                Icon(
                    imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = if (isPinned)
                        stringResource(R.string.games_unpin_game_description)
                    else
                        stringResource(R.string.games_pin_game_description),
                    tint = if (isPinned) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onAction(GameCardAction.Delete) }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.games_delete_game_description),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Dimen.ExtraSmallPadding)) {
            IconButton(onClick = { onAction(GameCardAction.Share) }) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = stringResource(R.string.games_share_game_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onAction(GameCardAction.Analyze) }) {
                Icon(
                    imageVector = Icons.Filled.Analytics,
                    contentDescription = stringResource(R.string.games_analyze_button),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onAction(GameCardAction.Check) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.FactCheck,
                    contentDescription = stringResource(R.string.games_check_button),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}