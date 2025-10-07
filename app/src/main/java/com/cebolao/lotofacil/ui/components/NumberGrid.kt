package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NumberGrid(
    modifier: Modifier = Modifier,
    allNumbers: List<Int> = (1..25).toList(),
    selectedNumbers: Set<Int>,
    onNumberClick: (Int) -> Unit,
    maxSelection: Int? = null
) {
    val haptic = LocalHapticFeedback.current
    val selectionFull = maxSelection != null && selectedNumbers.size >= maxSelection

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 5
    ) {
        allNumbers.forEach { number ->
            val isSelected = number in selectedNumbers
            val isClickable = !selectionFull || isSelected

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(
                        enabled = isClickable,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNumberClick(number)
                        }
                    )
                    .padding(2.dp)
            ) {
                NumberBall(
                    number = number,
                    isSelected = isSelected,
                    isDisabled = !isClickable,
                    size = 40.dp,
                    variant = NumberBallVariant.Primary
                )
            }
        }
    }
}