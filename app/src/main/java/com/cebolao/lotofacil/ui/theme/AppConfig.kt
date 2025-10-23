package com.cebolao.lotofacil.ui.theme

/**
 * Centraliza constantes de configuração da UI e animações para todo o aplicativo.
 */
object AppConfig {

    /**
     * Durações padrão para animações na UI, em milisseundos.
     */
    object Animation {
        const val ShortDuration = 200
        const val MediumDuration = 300
        const val LongDuration = 500
        const val SplashExitDuration = 400

        // HomeScreen Delays
        const val HomeScreenNextDrawDelay = 0L
        const val HomeScreenLastDrawDelay = 50L
        const val HomeScreenStatsPanelDelay = 100L
        const val HomeScreenChartsDelay = 150L
        const val HomeScreenExplanationDelay = 200L
        // CheckerScreen Delays
        const val CheckerResultEntryDelay = 100
    }

    /**
     * Constantes de configuração para componentes específicos.
     */
    object UI {
        // AnimateOnEntry
        const val AnimateOnEntryOffsetYDivisor = 12

        // BarChart
        const val BarChartGridLines = 4
        const val BarChartDashInterval = 4f
        const val BarChartDashPhase = 0f
        const val BarChartGridLineAlpha = 0.5f
        const val BarChartLabelRotation = -45f
        const val SumChartMinRange = 120
        const val SumChartMaxRange = 270
        const val SumChartStep = 10

        // CheckerScreen
        const val CheckerChartMinMaxValue = 10
        const val CheckerChartSuffixLength = 4

        // FilterPanel
        const val FilterPanelOutlineAlpha = 0.1f
        const val FilterPanelChipBgAlpha = 0.12f
        const val FilterPanelTrackAlpha = 0.2f
        const val FilterPanelProbLow = 0.1f
        const val FilterPanelProbMedium = 0.4f

        // FilterCard
        const val FilterCardBorderAlpha = 0.5f
        const val FilterCardContainerAlpha = 0.2f
        const val FilterCardDisabledBorderAlpha = 0f

        // GameCard
        const val GameCardPinnedContainerAlpha = 0.2f
        const val GameCardDisabledBorderAlpha = 0f

        // TimeWindowChip
        const val TimeWindowChipBorderAlpha = 0.3f

        // NumberBall
        const val NumberBallFontSizeFactor = 2.5f
        const val NumberBallShadowSpotAlpha = 0.25f
        const val NumberBallGradientAlpha = 0.85f
        const val NumberBallBorderAlphaDisabled = 0.3f
        const val NumberBallDisabledAlpha = 0.6f
        const val NumberBallBorderAlphaHighlighted = 0.7f
        const val NumberBallBorderAlphaDefault = 0.2f
        const val NumberBallBorderAlphaSelected = 0.3f

        // NumberGrid
        const val NumberGridItemsPerRow = 5

        // StatInfoChip
        const val StatInfoChipDisabledAlpha = 0.35f

        // StatisticsPanel
        const val StatsPanelLoadingOverlayAlpha = 0.7f

        // OnboardingScreen
        const val OnboardingImageFraction = 0.7f

        // ColorPaletteCard
        const val ColorSwatchBorderAlpha = 0.5f
    }
}