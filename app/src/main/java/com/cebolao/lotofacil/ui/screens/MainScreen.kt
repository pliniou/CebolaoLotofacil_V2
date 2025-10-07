package com.cebolao.lotofacil.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.cebolao.lotofacil.viewmodels.MainViewModel

@Composable
fun MainScreen(mainViewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val haptic = LocalHapticFeedback.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val startDestination by mainViewModel.startDestination.collectAsStateWithLifecycle()

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
                        val isSelected = currentDestination?.hierarchy?.any { it.route?.startsWith(screen.baseRoute!!) == true } == true
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon!! else screen.unselectedIcon!!,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    screen.title!!,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors()
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
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
            composable(Screen.Filters.route) {
                FiltersScreen {
                    navController.navigate(Screen.GeneratedGames.route) {
                        popUpTo(navController.graph.findStartDestination().id)
                    }
                }
            }
            composable(Screen.GeneratedGames.route) {
                GeneratedGamesScreen(
                    onNavigateToCheckScreen = { numbers ->
                        navController.navigate(Screen.Checker.createRoute(numbers))
                    }
                )
            }
            composable(
                route = Screen.Checker.route,
                arguments = Screen.Checker.arguments
            ) { CheckerScreen() }
            composable(Screen.About.route) { AboutScreen() }
        }
    }
}