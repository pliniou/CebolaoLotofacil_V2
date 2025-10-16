package com.cebolao.lotofacil.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.cebolao.lotofacil.R

@Composable
fun InfoDialog(
    dialogTitle: String,
    icon: ImageVector,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(R.string.general_info_icon_description),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = dialogTitle, style = MaterialTheme.typography.titleLarge)
        },
        text = { content() },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.general_close))
            }
        }
    )
}