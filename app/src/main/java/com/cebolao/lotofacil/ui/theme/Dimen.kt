package com.cebolao.lotofacil.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Centraliza todas as dimensões da UI, incluindo espaçamentos, tamanhos,
 * elevações e bordas para garantir consistência visual em todo o app.
 */
object Dimen {

    //region Espaçamentos (Padding)
    val ScreenPadding = 20.dp
    val LargePadding = 24.dp
    val CardPadding = 16.dp
    val MediumPadding = 12.dp
    val SmallPadding = 8.dp
    val ExtraSmallPadding = 4.dp
    val BottomBarOffset = 120.dp
    //endregion

    //region Tamanhos (Sizes)
    val LargeButtonHeight = 52.dp
    val NumberBall = 40.dp
    val NumberBallSmall = 36.dp
    val NumberBallDialog = 32.dp

    val SmallIcon = 20.dp
    val MediumIcon = 24.dp
    val LargeIcon = 36.dp

    val Logo = 64.dp
    val ProgressBarHeight = 6.dp
    val ProgressBarStroke = 2.5.dp
    val BarChartHeight = 180.dp
    val BarChartYAxisLabelWidth = 36.dp
    val BarChartXAxisLabelHeight = 36.dp
    val BarChartTooltipWidth = 60.dp
    val BarChartTooltipHeight = 24.dp
    //endregion

    //region Elevações (Elevation)
    object Elevation {
        val Level0 = 0.dp
        val Level1 = 2.dp
        val Level2 = 4.dp
        val Level3 = 6.dp
        val Level4 = 8.dp
    }
    //endregion

    //region Bordas (Border)
    object Border {
        val Default = 1.dp
        val Thick = 1.5.dp
    }
    //endregion

    //region Onboarding
    val ActiveIndicatorWidth = 24.dp
    val IndicatorHeight = 8.dp
    val IndicatorSpacing = 4.dp
    //endregion
}