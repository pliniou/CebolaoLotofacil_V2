package com.cebolao.lotofacil.ui.theme

/**
 * Centraliza constantes de configuração da UI e animações para todo o aplicativo.
 * Refatorado para animações mais fluidas e elegantes.
 */
object AppConfig {

    /**
     * Durações padrão para animações na UI, em milisseundos.
     */
    object Animation {
        const val ShortDuration = 250
        const val MediumDuration = 400
        const val LongDuration = 600
        const val SplashExitDuration = 500

        // HomeScreen Delays
        const val HomeScreenNextDrawDelay = 0L
        const val HomeScreenLastDrawDelay = 75L
        const val HomeScreenStatsPanelDelay = 150L
        const val HomeScreenChartsDelay = 225L
        const val HomeScreenExplanationDelay = 300L
        // CheckerScreen Delays
        const val CheckerResultEntryDelay = 150
    }

    /**
     * Constantes de configuração para componentes específicos.
     */
    object UI {
        // AnimateOnEntry
        const val AnimateOnEntryOffsetYDivisor = 10

        // BarChart
        const val BarChartGridLines = 4
        const val BarChartDashInterval = 5f
        const val BarChartDashPhase = 0f
        const val BarChartGridLineAlpha = 0.3f
        const val BarChartLabelRotation = -45f
        const val SumChartMinRange = 120
        const val SumChartMaxRange = 270
        const val SumChartStep = 10

        // CheckerScreen
        const val CheckerChartMinMaxValue = 10
        const val CheckerChartSuffixLength = 4

        // FilterPanel
        const val FilterPanelOutlineAlpha = 0.1f
        const val FilterPanelChipBgAlpha = 0.15f
        const val FilterPanelTrackAlpha = 0.25f
        const val FilterPanelProbLow = 0.15f
        const val FilterPanelProbMedium = 0.45f

        // FilterCard
        const val FilterCardBorderAlpha = 0.4f
        const val FilterCardContainerAlpha = 0.1f
        const val FilterCardDisabledBorderAlpha = 0f

        // GameCard
        const val GameCardPinnedContainerAlpha = 0.15f
        const val GameCardDisabledBorderAlpha = 0f

        // TimeWindowChip
        const val TimeWindowChipBorderAlpha = 0.4f

        // NumberBall
        const val NumberBallFontSizeFactor = 2.6f
        const val NumberBallShadowSpotAlpha = 0.2f
        const val NumberBallGradientAlpha = 0.9f
        const val NumberBallBorderAlphaDisabled = 0.3f
        const val NumberBallDisabledAlpha = 0.5f
        const val NumberBallBorderAlphaHighlighted = 0.6f
        const val NumberBallBorderAlphaDefault = 0.25f
        const val NumberBallBorderAlphaSelected = 0.3f

        // NumberGrid
        const val NumberGridItemsPerRow = 5

        // StatInfoChip
        const val StatInfoChipDisabledAlpha = 0.4f

        // StatisticsPanel
        const val StatsPanelLoadingOverlayAlpha = 0.8f

        // OnboardingScreen
        const val OnboardingImageFraction = 0.65f

        // ColorPaletteCard
        const val ColorSwatchBorderAlpha = 0.4f
    }
}