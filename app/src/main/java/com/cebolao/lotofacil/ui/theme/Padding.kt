package com.cebolao.lotofacil.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Objeto para centralizar as dimensões de espaçamento usadas no aplicativo,
 * garantindo consistência visual e facilitando a manutenção.
 */
object Padding {
    /** Espaçamento padrão para o conteúdo principal da tela em relação às bordas. */
    val Screen = 20.dp

    /** Espaçamento grande, usado para separar seções importantes. */
    val Large = 24.dp

    /** Espaçamento padrão dentro de cards e entre componentes. */
    val Card = 16.dp

    /** Espaçamento médio, para agrupar elementos relacionados. */
    val Medium = 12.dp

    /** Espaçamento pequeno, entre itens como texto e ícone. */
    val Small = 8.dp

    /** Espaçamento mínimo, para ajustes finos. */
    val ExtraSmall = 4.dp

    // NOVOS VALORES
    /** Espaçamento para evitar sobreposição de conteúdo pela barra de ações inferior. */
    val BottomBarOffset = 120.dp

    /** Altura padrão para indicadores de página ou progresso. */
    val IndicatorHeight = 8.dp

    /** Espaçamento entre itens de um indicador. */
    val IndicatorSpacing = 4.dp
}