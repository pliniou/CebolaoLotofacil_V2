package com.cebolao.lotofacil.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.navigation.bottomNavItems
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.viewmodels.MainViewModel

@Composable
fun MainScreen(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val startDestination by mainViewModel.startDestination.collectAsStateWithLifecycle()
    val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
    val accentPaletteName by mainViewModel.accentPalette.collectAsStateWithLifecycle()
    val accentPalette = remember(accentPaletteName) {
        AccentPalette.entries.find { it.name == accentPaletteName } ?: AccentPalette.DEFAULT
    }

    val bottomBarVisible by remember(currentDestination) {
        derivedStateOf {
            bottomNavItems.any { it.baseRoute == currentDestination?.route?.substringBefore('?') }
        }
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = bottomBarVisible,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route?.substringBefore('?') == screen.baseRoute
                        } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                val icon = when {
                                    selected -> screen.selectedIcon
                                    else -> screen.unselectedIcon
                                }
                                if (icon != null) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = {
                                if (screen.title != null) {
                                    Text(
                                        text = screen.title,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (!uiState.isReady) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            NavHost(
                navController = navController,
                startDestination = startDestination.destination,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(onOnboardingComplete = {
                        mainViewModel.onOnboardingComplete()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    })
                }
                composable(Screen.Home.route) { HomeScreen() }
                composable(Screen.Filters.route) { FiltersScreen(navController) }
                composable(Screen.GeneratedGames.route) {
                    GeneratedGamesScreen(navController = navController)
                }
                composable(
                    route = Screen.Checker.route,
                    arguments = Screen.Checker.arguments
                ) {
                    CheckerScreen()
                }
                composable(Screen.About.route) {
                    AboutScreen(
                        currentTheme = themeMode,
                        currentPalette = accentPalette,
                        onThemeChange = mainViewModel::setThemeMode,
                        onPaletteChange = mainViewModel::setAccentPalette
                    )
                }
            }
        }
    }
}