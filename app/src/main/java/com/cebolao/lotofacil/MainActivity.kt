package com.cebolao.lotofacil

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.ui.screens.MainScreen
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.CebolaoLotofacilTheme
import com.cebolao.lotofacil.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        splash.setOnExitAnimationListener { splashScreenViewProvider ->
            val splashView = splashScreenViewProvider.view
            val iconView = splashScreenViewProvider.iconView

            val alphaAnimator = ObjectAnimator.ofFloat(
                splashView, View.ALPHA, 1f, 0f
            ).apply {
                interpolator = AnticipateInterpolator()
                duration = AppConfig.Animation.SplashExitDuration.toLong()
                doOnEnd { splashScreenViewProvider.remove() }
            }

            val scaleXAnimator = ObjectAnimator.ofFloat(iconView, View.SCALE_X, 1f, 0.5f).apply {
                interpolator = AnticipateInterpolator()
                duration = AppConfig.Animation.SplashExitDuration.toLong()
            }
            val scaleYAnimator = ObjectAnimator.ofFloat(iconView, View.SCALE_Y, 1f, 0.5f).apply {
                interpolator = AnticipateInterpolator()
                duration = AppConfig.Animation.SplashExitDuration.toLong()
            }

            scaleXAnimator.start()
            scaleYAnimator.start()
            alphaAnimator.start()
        }

        setContent {
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
            val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
            val accentPaletteName by mainViewModel.accentPalette.collectAsStateWithLifecycle()
            val accentPalette = remember(accentPaletteName) {
                AccentPalette.entries.find { it.name == accentPaletteName } ?: AccentPalette.DEFAULT
            }

            splash.setKeepOnScreenCondition { !uiState.isReady }

            val useDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            CebolaoLotofacilTheme(
                darkTheme = useDarkTheme,
                dynamicColor = false, // Garante que a paleta selecionada seja usada.
                accentPalette = accentPalette
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (uiState.isReady) {
                        MainScreen(mainViewModel = mainViewModel)
                    }
                }
            }
        }
    }
}