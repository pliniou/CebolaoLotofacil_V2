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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.ui.screens.MainScreen
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

        var isAppReady by mutableStateOf(false)

        splash.setKeepOnScreenCondition { !isAppReady }

        // CORREÇÃO: Adiciona uma animação de saída customizada para a splash screen.
        splash.setOnExitAnimationListener { splashScreenViewProvider ->
            val splashView = splashScreenViewProvider.view
            val iconView = splashScreenViewProvider.iconView

            // Animação de fade-out para a tela inteira
            val alphaAnimator = ObjectAnimator.ofFloat(
                splashView,
                View.ALPHA,
                1f,
                0f
            ).apply {
                interpolator = AnticipateInterpolator()
                duration = 400L
                doOnEnd { splashScreenViewProvider.remove() }
            }

            // Animação de escala para o ícone
            val scaleXAnimator = ObjectAnimator.ofFloat(iconView, View.SCALE_X, 1f, 0.5f).apply {
                interpolator = AnticipateInterpolator()
                duration = 400L
            }
            val scaleYAnimator = ObjectAnimator.ofFloat(iconView, View.SCALE_Y, 1f, 0.5f).apply {
                interpolator = AnticipateInterpolator()
                duration = 400L
            }

            // Inicia as animações
            scaleXAnimator.start()
            scaleYAnimator.start()
            alphaAnimator.start()
        }

        setContent {
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
            val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
            isAppReady = uiState.isReady

            val useDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            CebolaoLotofacilTheme(
                darkTheme = useDarkTheme,
                dynamicColor = true
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