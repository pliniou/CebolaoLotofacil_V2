package com.cebolao.lotofacil.ui.theme

/**
 * Centraliza constantes de configuração da UI e animações para todo o aplicativo.
 */
object AppConfig {

    /**
     * Durações padrão para animações na UI, em milissegundos.
     */
    object Animation {
        const val ShortDuration = 250
        const val MediumDuration = 400
        const val LongDuration = 600
        const val SplashExitDuration = 400
    }

    /**
     * Constantes de configuração para componentes específicos.
     */
    object UI {
        const val BarChartGridLines = 4
        const val FilterPanelOutlineAlpha = 0.1f
        const val FilterPanelChipBgAlpha = 0.12f
        const val TimeWindowChipBorderAlpha = 0.3f
        const val NumberBallFontSizeFactor = 2.5f
    }
}