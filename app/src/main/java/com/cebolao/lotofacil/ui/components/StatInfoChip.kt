package com.cebolao.lotofacil.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private const val DISABLED_CONTAINER_ALPHA = 0.35f

@Composable
fun StatInfoChip(
    text: String,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = {},
        enabled = false,
        modifier = modifier,
        label = { Text(text = text, style = MaterialTheme.typography.labelLarge) },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = DISABLED_CONTAINER_ALPHA),
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}