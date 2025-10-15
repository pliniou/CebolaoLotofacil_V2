package com.cebolao.lotofacil.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class AccentPalette(val paletteName: String) {
    DEFAULT("Padrão"),
    FOREST("Floresta"),
    OCEAN("Oceano"),
    SUNSET("Poente")
}

fun lightColorSchemeFor(palette: AccentPalette): ColorScheme {
    val base = lightColorScheme(
        primary = LightPrimary,
        onPrimary = LightOnPrimary,
        primaryContainer = LightPrimaryContainer,
        onPrimaryContainer = LightOnPrimaryContainer,
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
        AccentPalette.DEFAULT -> base.copy(
            secondary = LightSecondary,
            onSecondary = LightOnSecondary,
            secondaryContainer = LightSecondaryContainer,
            onSecondaryContainer = LightOnSecondaryContainer,
            tertiary = LightTertiary,
            onTertiary = LightOnTertiary,
            tertiaryContainer = LightTertiaryContainer,
            onTertiaryContainer = LightOnTertiaryContainer,
        )

        AccentPalette.FOREST -> base.copy(
            secondary = Color(0xFF4C662B),
            secondaryContainer = Color(0xFFCDECA2),
            tertiary = Color(0xFF386668),
            tertiaryContainer = Color(0xFFBBECEF),
        )

        AccentPalette.OCEAN -> base.copy(
            secondary = Color(0xFF00658E),
            secondaryContainer = Color(0xFFC7E7FF),
            tertiary = Color(0xFF00677F),
            tertiaryContainer = Color(0xFFB6EBFF),
        )

        AccentPalette.SUNSET -> base.copy(
            secondary = Color(0xFF8F4C00),
            secondaryContainer = Color(0xFFFFDCC2),
            tertiary = Color(0xFF795900),
            tertiaryContainer = Color(0xFFFFE086),
        )
    }
}

fun darkColorSchemeFor(palette: AccentPalette): ColorScheme {
    val base = darkColorScheme(
        primary = DarkPrimary,
        onPrimary = DarkOnPrimary,
        primaryContainer = DarkPrimaryContainer,
        onPrimaryContainer = DarkOnPrimaryContainer,
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
        AccentPalette.DEFAULT -> base.copy(
            secondary = DarkSecondary,
            onSecondary = DarkOnSecondary,
            secondaryContainer = DarkSecondaryContainer,
            onSecondaryContainer = DarkOnSecondaryContainer,
            tertiary = DarkTertiary,
            onTertiary = DarkOnTertiary,
            tertiaryContainer = DarkTertiaryContainer,
            onTertiaryContainer = DarkOnTertiaryContainer,
        )

        AccentPalette.FOREST -> base.copy(
            secondary = Color(0xFFB2D088),
            secondaryContainer = Color(0xFF354E16),
            tertiary = Color(0xFFA0CFD2),
            tertiaryContainer = Color(0xFF1E4E50),
        )

        AccentPalette.OCEAN -> base.copy(
            secondary = Color(0xFF85CFFF),
            secondaryContainer = Color(0xFF004C6C),
            tertiary = Color(0xFF5AD5FA),
            tertiaryContainer = Color(0xFF004D63),
        )

        AccentPalette.SUNSET -> base.copy(
            secondary = Color(0xFFFFB77C),
            secondaryContainer = Color(0xFF6B3900),
            tertiary = Color(0xFFF2C048),
            tertiaryContainer = Color(0xFF5B4400),
        )
    }
}