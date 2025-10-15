package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.viewmodels.GenerationUiState
import java.text.NumberFormat
import java.util.Locale

private val GAME_QUANTITY_OPTIONS = listOf(1, 2, 3, 5, 7, 9, 10, 12, 15, 20)

@Composable
fun GenerationActionsPanel(
    generationState: GenerationUiState,
    onGenerate: (Int) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var selectedIndex by remember { mutableIntStateOf(0) }
    val quantity = GAME_QUANTITY_OPTIONS[selectedIndex]
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }
    val isLoading = generationState is GenerationUiState.Loading

    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = Dimen.Elevation.Level4,
        tonalElevation = Dimen.Elevation.Level2
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = Dimen.CardPadding, vertical = Dimen.SmallPadding)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                QuantitySelector(
                    quantity = quantity,
                    onDecrement = {
                        if (selectedIndex > 0) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedIndex--
                        }
                    },
                    onIncrement = {
                        if (selectedIndex < GAME_QUANTITY_OPTIONS.lastIndex) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedIndex++
                        }
                    },
                    isDecrementEnabled = selectedIndex > 0 && !isLoading,
                    isIncrementEnabled = selectedIndex < GAME_QUANTITY_OPTIONS.lastIndex && !isLoading
                )
                Text(
                    text = currencyFormat.format(LotofacilConstants.GAME_COST.multiply(quantity.toBigDecimal())),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)
            ) {
                if (isLoading) {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCancel()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = stringResource(R.string.filters_button_cancel_description)
                        )
                    }
                }

                PrimaryActionButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimen.LargeButtonHeight),
                    enabled = !isLoading,
                    loading = isLoading,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onGenerate(quantity)
                    },
                    leading = {
                        if (!isLoading) Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    }
                ) {
                    AnimatedContent(targetState = generationState, label = "generateButton") { state ->
                        if (state is GenerationUiState.Loading) {
                            val text = when {
                                state.total > 0 -> stringResource(R.string.filters_button_generating_progress, state.progress, state.total)
                                else -> state.message
                            }
                            Text(text = text, style = MaterialTheme.typography.labelLarge)
                        } else {
                            Text(
                                text = stringResource(R.string.filters_button_generate),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    isDecrementEnabled: Boolean,
    isIncrementEnabled: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimen.ExtraSmallPadding)) {
        IconButton(onClick = onDecrement, enabled = isDecrementEnabled) {
            Icon(imageVector = Icons.Filled.Remove, contentDescription = stringResource(R.string.filters_quantity_decrease))
        }
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onIncrement, enabled = isIncrementEnabled) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(R.string.filters_quantity_increase))
        }
    }
}
