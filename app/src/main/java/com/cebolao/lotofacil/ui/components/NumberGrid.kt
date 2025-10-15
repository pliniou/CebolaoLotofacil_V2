package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

private val ALL_LOTOFACIL_NUMBERS = (1..25).toImmutableList()
private const val ITEMS_PER_ROW = 5

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NumberGrid(
    modifier: Modifier = Modifier,
    allNumbers: ImmutableList<Int> = ALL_LOTOFACIL_NUMBERS,
    selectedNumbers: Set<Int>,
    onNumberClick: (Int) -> Unit,
    maxSelection: Int? = null,
    numberSize: Dp = Dimen.NumberBall
) {
    val haptic = LocalHapticFeedback.current
    val selectionFull = maxSelection != null && selectedNumbers.size >= maxSelection

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimen.SmallPadding),
        horizontalArrangement = Arrangement.spacedBy(
            Dimen.SmallPadding,
            Alignment.CenterHorizontally
        ),
        verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding),
        maxItemsInEachRow = ITEMS_PER_ROW
    ) {
        allNumbers.forEach { number ->
            val isSelected = number in selectedNumbers
            val isClickable = !(selectionFull && !isSelected)

            NumberGridItem(
                number = number,
                isSelected = isSelected,
                isClickable = isClickable,
                haptic = haptic,
                numberSize = numberSize,
                onNumberClick = onNumberClick
            )
        }
    }
}

@Composable
private fun NumberGridItem(
    number: Int,
    isSelected: Boolean,
    isClickable: Boolean,
    haptic: HapticFeedback,
    numberSize: Dp,
    onNumberClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = isClickable,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onNumberClick(number)
                }
            )
            .padding(Dimen.ExtraSmallPadding)
    ) {
        NumberBall(
            number = number,
            isSelected = isSelected,
            isDisabled = !isClickable,
            size = numberSize
        )
    }
}
