package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = Dimen.Elevation.Level1),
    border: BorderStroke? = BorderStroke(Dimen.Border.Default, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
    contentSpacing: Dp = Dimen.CardPadding,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
    ) {
        Column(
            modifier = Modifier.padding(contentSpacing),
            verticalArrangement = Arrangement.spacedBy(contentSpacing)
        ) {
            content()
        }
    }
}