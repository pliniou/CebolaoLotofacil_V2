package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.darkColorSchemeFor
import com.cebolao.lotofacil.ui.theme.lightColorSchemeFor

@Composable
fun ColorPaletteCard(
    currentPalette: AccentPalette,
    onPaletteChange: (AccentPalette) -> Unit,
    modifier: Modifier = Modifier
) {
    val palettes = AccentPalette.entries
    val isDarkTheme = isSystemInDarkTheme()

    val previewColorSchemes = remember(isDarkTheme, palettes) {
        palettes.associateWith {
            if (isDarkTheme) darkColorSchemeFor(it) else lightColorSchemeFor(it)
        }
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding),
        contentPadding = PaddingValues(horizontal = Dimen.SmallPadding)
    ) {
        items(palettes) { palette ->
            val colorScheme = previewColorSchemes[palette]!!
            PalettePreviewCard(
                colorScheme = colorScheme,
                name = palette.paletteName,
                isSelected = currentPalette == palette,
                onClick = { onPaletteChange(palette) }
            )
        }
    }
}

@Composable
private fun PalettePreviewCard(
    colorScheme: ColorScheme,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(Dimen.PaletteCardWidth)
            .height(Dimen.PaletteCardHeight)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant
        ),
        border = if (isSelected) {
            BorderStroke(Dimen.Border.Thick, colorScheme.primary)
        } else {
            BorderStroke(Dimen.Border.Default, MaterialTheme.colorScheme.outline)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.MediumPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)
            ) {
                ColorSwatch(colorScheme.primary, modifier = Modifier.weight(1f))
                ColorSwatch(colorScheme.secondary, modifier = Modifier.weight(1f))
                ColorSwatch(colorScheme.tertiary, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(Dimen.MediumPadding))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = colorScheme.onSurfaceVariant
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = stringResource(R.string.general_selected),
                        tint = colorScheme.primary,
                        modifier = Modifier.size(Dimen.SmallIcon)
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(Dimen.LargeIcon)
            .clip(MaterialTheme.shapes.small)
            .background(color)
            .border(
                width = Dimen.Border.Default,
                color = MaterialTheme.colorScheme.outline.copy(alpha = AppConfig.UI.ColorSwatchBorderAlpha),
                shape = MaterialTheme.shapes.small
            )
    )
}