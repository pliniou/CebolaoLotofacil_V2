package com.cebolao.lotofacil.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun OutlinedTonalIconButton(
    icon: ImageVector,
    contentDescription: String?,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val colors = if (selected) {
        IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
    } else {
        IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    IconButton(
        onClick = onClick,
        enabled = enabled,
        colors = colors
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}