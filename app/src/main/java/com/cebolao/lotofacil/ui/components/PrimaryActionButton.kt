package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun PrimaryActionButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    onClick: () -> Unit,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Button(
        modifier = modifier,
        enabled = enabled && !loading,
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = colors
    ) {
        Row(modifier = Modifier.padding(horizontal = Dimen.ExtraSmallPadding)) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimen.MediumIcon),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = Dimen.ProgressBarStroke
                )
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            } else if (leading != null) {
                leading()
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            }
            content()
            if (!loading && trailing != null) {
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                trailing()
            }
        }
    }
}