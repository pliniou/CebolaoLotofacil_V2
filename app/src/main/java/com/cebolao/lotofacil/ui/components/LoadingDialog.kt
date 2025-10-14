package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cebolao.lotofacil.ui.theme.Padding

@Composable
fun LoadingDialog(
    title: String,
    message: String,
    onDismissRequest: () -> Unit,
    isCancelable: Boolean = false
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = isCancelable,
            dismissOnClickOutside = isCancelable,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Padding.Large, vertical = Padding.Large),
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation()
        ) {
            Column(
                modifier = Modifier.padding(Padding.Large),
                verticalArrangement = Arrangement.spacedBy(Padding.Card),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Padding.Medium)
                ) {
                    CircularProgressIndicator()
                    Text(text = message, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(Padding.Small))
            }
        }
    }
}