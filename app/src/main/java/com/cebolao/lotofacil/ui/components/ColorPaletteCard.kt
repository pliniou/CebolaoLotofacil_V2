package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Shapes
import com.cebolao.lotofacil.ui.theme.Typography
import com.cebolao.lotofacil.ui.theme.darkColorSchemeFor
import com.cebolao.lotofacil.ui.theme.lightColorSchemeFor

private const val COLOR_SWATCH_BORDER_ALPHA = 0.5f

@Composable
fun ColorPaletteCard(
    currentPalette: AccentPalette,
    onPaletteChange: (AccentPalette) -> Unit,
    modifier: Modifier = Modifier
) {
    val palettes = AccentPalette.entries
    val isDarkTheme = isSystemInDarkTheme()

    SectionCard(modifier = modifier) {
        TitleWithIcon(text = "Paleta de Cores", icon = Icons.Default.Palette)
        Column(verticalArrangement = Arrangement.spacedBy(Dimen.ExtraSmallPadding)) {
            for (palette in palettes) {
                PaletteRowItem(
                    palette = palette,
                    colorScheme = if (isDarkTheme) darkColorSchemeFor(palette) else lightColorSchemeFor(
                        palette
                    ),
                    isSelected = currentPalette == palette,
                    onClick = { onPaletteChange(palette) }
                )
            }
        }
    }
}

@Composable
private fun PaletteRowItem(
    palette: AccentPalette,
    colorScheme: ColorScheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                    role = Role.RadioButton
                )
                .padding(vertical = Dimen.ExtraSmallPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)
        ) {
            RadioButton(selected = isSelected, onClick = onClick)
            ColorSwatch(colorScheme.primary)
            Text(
                text = palette.paletteName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(Dimen.MediumIcon)
            .background(color, CircleShape)
            .border(
                width = Dimen.Border.Default,
                color = MaterialTheme.colorScheme.outline.copy(alpha = COLOR_SWATCH_BORDER_ALPHA),
                shape = CircleShape
            )
    )
}