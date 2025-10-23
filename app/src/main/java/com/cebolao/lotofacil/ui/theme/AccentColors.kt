package com.cebolao.lotofacil.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

enum class AccentPalette(val paletteName: String) {
    DEFAULT("Lotofácil"),
    VIVID("Vívida"),
    FOREST("Floresta"),
    OCEAN("Oceano")
}

/**
 * Gera um ColorScheme claro com base na paleta selecionada.
 * As cores base (Background, Surface, Error) são compartilhadas.
 */
fun lightColorSchemeFor(palette: AccentPalette): ColorScheme {
    val baseScheme = lightColorScheme(
        error = LightError,
        onError = LightOnError,
        errorContainer = LightErrorContainer,
        onErrorContainer = LightOnErrorContainer,
        background = LightBackground,
        onBackground = LightOnBackground,
        surface = LightSurface,
        onSurface = LightOnSurface,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = LightOnSurfaceVariant,
        outline = LightOutline,
        outlineVariant = LightOutlineVariant,
    )

    return when (palette) {
        AccentPalette.DEFAULT -> baseScheme.copy(
            primary = LightPrimary,
            onPrimary = LightOnPrimary,
            primaryContainer = LightPrimaryContainer,
            onPrimaryContainer = LightOnPrimaryContainer,
            secondary = LightSecondary,
            onSecondary = LightOnSecondary,
            secondaryContainer = LightSecondaryContainer,
            onSecondaryContainer = LightOnSecondaryContainer,
            tertiary = LightTertiary,
            onTertiary = LightOnTertiary,
            tertiaryContainer = LightTertiaryContainer,
            onTertiaryContainer = LightOnTertiaryContainer,
        )

        AccentPalette.VIVID -> baseScheme.copy(
            primary = VividOrange,
            onPrimary = VividOrange_OnPrimary,
            primaryContainer = VividRed,
            onPrimaryContainer = VividRed_OnPrimary,
            secondary = VividYellow,
            onSecondary = VividYellow_OnSecondary,
            secondaryContainer = VividYellow.copy(alpha = 0.3f),
            onSecondaryContainer = VividYellow_OnSecondary,
            tertiary = VividRed,
            onTertiary = VividRed_OnPrimary,
            tertiaryContainer = VividRed.copy(alpha = 0.3f),
            onTertiaryContainer = VividYellow_OnSecondary,
        )

        AccentPalette.FOREST -> baseScheme.copy(
            primary = ForestGreen,
            onPrimary = ForestGreen_OnPrimary,
            primaryContainer = ForestGreen.copy(alpha = 0.3f),
            onPrimaryContainer = ForestLime_OnSecondary,
            secondary = ForestLime,
            onSecondary = ForestLime_OnSecondary,
            secondaryContainer = ForestLime.copy(alpha = 0.3f),
            onSecondaryContainer = ForestLime_OnSecondary,
            tertiary = ForestBrown,
            onTertiary = ForestBrown_OnTertiary,
            tertiaryContainer = ForestBrown.copy(alpha = 0.3f),
            onTertiaryContainer = ForestLime_OnSecondary,
        )

        AccentPalette.OCEAN -> baseScheme.copy(
            primary = OceanBlue,
            onPrimary = OceanBlue_OnPrimary,
            primaryContainer = OceanBlue.copy(alpha = 0.3f),
            onPrimaryContainer = OceanCyan_OnSecondary,
            secondary = OceanCyan,
            onSecondary = OceanCyan_OnSecondary,
            secondaryContainer = OceanCyan.copy(alpha = 0.3f),
            onSecondaryContainer = OceanCyan_OnSecondary,
            tertiary = OceanSand,
            onTertiary = OceanSand_OnTertiary,
            tertiaryContainer = OceanSand.copy(alpha = 0.3f),
            onTertiaryContainer = OceanSand_OnTertiary,
        )
    }
}

/**
 * Gera um ColorScheme escuro com base na paleta selecionada.
 * As cores base (Background, Surface, Error) são compartilhadas.
 */
fun darkColorSchemeFor(palette: AccentPalette): ColorScheme {
    val baseScheme = darkColorScheme(
        error = DarkError,
        onError = DarkOnError,
        errorContainer = DarkErrorContainer,
        onErrorContainer = DarkOnErrorContainer,
        background = DarkBackground,
        onBackground = DarkOnBackground,
        surface = DarkSurface,
        onSurface = DarkOnSurface,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = DarkOnSurfaceVariant,
        outline = DarkOutline,
        outlineVariant = DarkOutlineVariant,
    )

    return when (palette) {
        AccentPalette.DEFAULT -> baseScheme.copy(
            primary = DarkPrimary,
            onPrimary = DarkOnPrimary,
            primaryContainer = DarkPrimaryContainer,
            onPrimaryContainer = DarkOnPrimaryContainer,
            secondary = DarkSecondary,
            onSecondary = DarkOnSecondary,
            secondaryContainer = DarkSecondaryContainer,
            onSecondaryContainer = DarkOnSecondaryContainer,
            tertiary = DarkTertiary,
            onTertiary = DarkOnTertiary,
            tertiaryContainer = DarkTertiaryContainer,
            onTertiaryContainer = DarkOnTertiaryContainer,
        )

        AccentPalette.VIVID -> baseScheme.copy(
            primary = VividOrangeDark,
            onPrimary = VividOrangeDark_OnPrimary,
            primaryContainer = VividOrange,
            onPrimaryContainer = VividOrange_OnPrimary,
            secondary = VividYellowDark,
            onSecondary = VividYellowDark_OnSecondary,
            secondaryContainer = VividYellow,
            onSecondaryContainer = VividYellow_OnSecondary,
            tertiary = VividRedDark,
            onTertiary = VividRedDark_OnTertiary,
            tertiaryContainer = VividRed,
            onTertiaryContainer = VividRed_OnPrimary,
        )

        AccentPalette.FOREST -> baseScheme.copy(
            primary = ForestGreenDark,
            onPrimary = ForestGreenDark_OnPrimary,
            primaryContainer = ForestGreen,
            onPrimaryContainer = ForestGreen_OnPrimary,
            secondary = ForestLimeDark,
            onSecondary = ForestLimeDark_OnSecondary,
            secondaryContainer = ForestLime,
            onSecondaryContainer = ForestLime_OnSecondary,
            tertiary = ForestBrownDark,
            onTertiary = ForestBrownDark_OnTertiary,
            tertiaryContainer = ForestBrown,
            onTertiaryContainer = ForestBrown_OnTertiary,
        )

        AccentPalette.OCEAN -> baseScheme.copy(
            primary = OceanBlueDark,
            onPrimary = OceanBlueDark_OnPrimary,
            primaryContainer = OceanBlue,
            onPrimaryContainer = OceanBlue_OnPrimary,
            secondary = OceanCyanDark,
            onSecondary = OceanCyanDark_OnSecondary,
            secondaryContainer = OceanCyan,
            onSecondaryContainer = OceanCyan_OnSecondary,
            tertiary = OceanSandDark,
            onTertiary = OceanSandDark_OnTertiary,
            tertiaryContainer = OceanSand,
            onTertiaryContainer = OceanSand_OnTertiary,
        )
    }
}