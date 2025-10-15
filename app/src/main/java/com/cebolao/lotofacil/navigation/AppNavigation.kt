package com.cebolao.lotofacil.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.navArgument

@Stable
sealed class Screen(
    val route: String,
    val title: String? = null,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null,
    val isBottomNavItem: Boolean = false,
    val baseRoute: String = route.substringBefore('?')
) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home", "Início", Icons.Filled.Home, Icons.Outlined.Home, true)
    data object Filters : Screen("filters", "Gerador", Icons.Filled.Tune, Icons.Outlined.Tune, true)
    data object GeneratedGames : Screen(
        "generated_games",
        "Jogos",
        Icons.AutoMirrored.Filled.ListAlt,
        Icons.AutoMirrored.Filled.ListAlt,
        true
    )

    data object Checker : Screen(
        "checker?numbers={numbers}",
        "Conferidor",
        Icons.Filled.Analytics,
        Icons.Outlined.Analytics,
        true
    ) {
        const val CHECKER_NUMBERS_ARG = "numbers"

        val arguments = listOf(
            navArgument(CHECKER_NUMBERS_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )

        fun createRoute(numbers: Set<Int>): String {
            val numbersArg = numbers.joinToString(",")
            return "checker?$CHECKER_NUMBERS_ARG=$numbersArg"
        }
    }

    data object About : Screen("about", "Sobre", Icons.Filled.Info, Icons.Outlined.Info, true)
}

// A lista agora é gerada dinamicamente, tornando-a mais fácil de manter.
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Filters,
    Screen.GeneratedGames,
    Screen.Checker,
    Screen.About
)
