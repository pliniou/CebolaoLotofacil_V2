package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.ui.theme.Padding

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameCard(
    game: LotofacilGame,
    modifier: Modifier = Modifier,
    onAnalyzeClick: () -> Unit,
    onPinClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCheckClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isPinned = game.isPinned

    val elevation by animateDpAsState(if (isPinned) 4.dp else 2.dp, spring(stiffness = Spring.StiffnessMedium), label = "elevation")
    val borderColor by animateColorAsState(if (isPinned) MaterialTheme.colorScheme.primary else Color.Transparent, tween(250), label = "borderColor")
    val containerColor by animateColorAsState(
        if (isPinned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceColorAtElevation(elevation),
        tween(250), label = "containerColor"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(elevation),
        border = BorderStroke(1.5.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Padding.Card, vertical = Padding.Medium),
            verticalArrangement = Arrangement.spacedBy(Padding.Medium)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                maxItemsInEachRow = 5
            ) {
                game.numbers.sorted().forEach { number ->
                    NumberBall(number = number, size = 40.dp, variant = NumberBallVariant.Secondary)
                }
            }
            GameCardActions(
                isPinned = isPinned,
                onAnalyzeClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onAnalyzeClick()
                },
                onCheckClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCheckClick()
                },
                onPinClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onPinClick()
                },
                onDeleteClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDeleteClick()
                },
                onShareClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onShareClick()
                }
            )
        }
    }
}

@Composable
private fun GameCardActions(
    isPinned: Boolean,
    onAnalyzeClick: () -> Unit,
    onCheckClick: () -> Unit,
    onPinClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Padding.ExtraSmall)) {
            IconButton(onClick = onPinClick) {
                Icon(
                    imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = if (isPinned) stringResource(R.string.games_unpin_game_description)
                    else stringResource(R.string.games_pin_game_description),
                    tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, stringResource(R.string.games_delete_game_description), tint = MaterialTheme.colorScheme.error)
            }
            IconButton(onClick = onShareClick) {
                Icon(Icons.Filled.Share, stringResource(R.string.games_share_game_description), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Padding.ExtraSmall)) {
            TextButton(onClick = onAnalyzeClick) {
                Icon(Icons.Filled.Analytics, null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.games_analyze_button))
            }
            TextButton(onClick = onCheckClick) {
                Icon(Icons.Filled.Check, null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.games_check_button))
            }
        }
    }
}