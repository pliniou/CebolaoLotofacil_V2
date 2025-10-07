package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.viewmodels.GenerationUiState
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable
fun GenerationActionsPanel(
    generationState: GenerationUiState,
    onGenerate: (Int) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val options = remember { listOf(1, 2, 3, 5, 7, 9, 10, 12, 15, 20) }
    var selectedIndex by remember { mutableIntStateOf(0) }
    val quantity = options[selectedIndex]
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }
    val isLoading = generationState is GenerationUiState.Loading

    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QuantitySelector(
                    quantity = quantity,
                    onDecrement = {
                        if (selectedIndex > 0) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedIndex--
                        }
                    },
                    onIncrement = {
                        if (selectedIndex < options.lastIndex) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedIndex++
                        }
                    },
                    isDecrementEnabled = selectedIndex > 0 && !isLoading,
                    isIncrementEnabled = selectedIndex < options.lastIndex && !isLoading
                )
                Text(
                    text = currencyFormat.format(
                        LotofacilConstants.GAME_COST.multiply(BigDecimal(quantity))
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isLoading) {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCancel()
                    }) {
                        Icon(Icons.Filled.Cancel, contentDescription = stringResource(R.string.filters_button_cancel_description))
                    }
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onGenerate(quantity)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = !isLoading,
                    shape = MaterialTheme.shapes.large
                ) {
                    AnimatedContent(
                        targetState = isLoading,
                        label = "GenerateButtonContent"
                    ) { loading ->
                        if (loading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val loadingText = when (generationState) {
                                    is GenerationUiState.Loading -> {
                                        val current = generationState.progress
                                        val total = generationState.total.takeIf { it > 0 } ?: 0
                                        if (total > 0 && current < total) {
                                            stringResource(R.string.filters_button_generating_progress, current, total)
                                        } else {
                                            generationState.message
                                        }
                                    }
                                    else -> stringResource(R.string.filters_button_generating)
                                }
                                Text(loadingText, style = MaterialTheme.typography.labelLarge)
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text(stringResource(R.string.filters_button_generate))
                            }
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(onClick = onDecrement, enabled = isDecrementEnabled, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Filled.Remove, stringResource(R.string.filters_quantity_decrease))
        }
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.widthIn(min = 28.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        IconButton(onClick = onIncrement, enabled = isIncrementEnabled, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Filled.Add, stringResource(R.string.filters_quantity_increase))
        }
    }
}