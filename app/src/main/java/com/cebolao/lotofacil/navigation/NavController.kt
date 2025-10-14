package com.cebolao.lotofacil.navigation

import androidx.navigation.NavController

/**
 * Função de extensão para simplificar a navegação para a CheckerScreen.
 * Abstrai a lógica de formatação do argumento de números, tornando a chamada
 * mais limpa e segura.
 *
 * @param numbers O conjunto de números do jogo a ser verificado.
 */
fun NavController.navigateToChecker(numbers: Set<Int>) {
    val route = Screen.Checker.createRoute(numbers)
    this.navigate(route)
}