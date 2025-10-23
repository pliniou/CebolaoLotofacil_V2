package com.cebolao.lotofacil.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cebolao.lotofacil.ui.theme.AppConfig

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
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppConfig.UI.StatInfoChipDisabledAlpha),
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}