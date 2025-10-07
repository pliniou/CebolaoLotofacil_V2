package com.cebolao.lotofacil

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splash.setOnExitAnimationListener { splashView ->
                val slideUp = ObjectAnimator.ofFloat(
                    splashView.view,
                    View.TRANSLATION_Y,
                    0f,
                    -splashView.view.height.toFloat()
                )
                val fadeOut = ObjectAnimator.ofFloat(
                    splashView.view,
                    View.ALPHA,
                    1f,
                    0f
                )
                AnimatorSet().apply {
                    interpolator = AccelerateInterpolator()
                    duration = 400L
                    playTogether(slideUp, fadeOut)
                    doOnEnd { splashView.remove() }
                    start()
                }
            }
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
                        // A lógica de navegação agora é gerenciada dentro do MainScreen
                        // com um startDestination dinâmico.
                        MainScreen(mainViewModel = mainViewModel)
                    }
                }
            }
        }
    }
}