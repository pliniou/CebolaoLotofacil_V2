package com.cebolao.lotofacil.viewmodels

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.domain.repository.UserPreferencesRepository
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.ui.theme.AccentPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STATE_IN_TIMEOUT_MS = 5_000L
private const val DEFAULT_THEME_MODE = "auto"

@Stable
data class MainUiState(
    val isReady: Boolean = false
)

data class StartDestinationState(
    val destination: String = Screen.Onboarding.route,
    val isLoading: Boolean = true
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val startDestination: StateFlow<StartDestinationState> = userPreferencesRepository.hasCompletedOnboarding
        .map { hasCompleted ->
            val route = if (hasCompleted) Screen.Home.route else Screen.Onboarding.route
            StartDestinationState(destination = route, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
            initialValue = StartDestinationState()
        )
        
    val uiState: StateFlow<MainUiState> = startDestination
        .map { MainUiState(isReady = !it.isLoading) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
            initialValue = MainUiState()
        )

    val themeMode: StateFlow<String> = userPreferencesRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
            initialValue = DEFAULT_THEME_MODE
        )

    val accentPalette: StateFlow<String> = userPreferencesRepository.accentPalette
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
            initialValue = AccentPalette.DEFAULT.name
        )

    fun onOnboardingComplete() {
        viewModelScope.launch {
            userPreferencesRepository.setHasCompletedOnboarding(true)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
        }
    }

    fun setAccentPalette(palette: AccentPalette) {
        viewModelScope.launch {
            userPreferencesRepository.setAccentPalette(palette.name)
        }
    }
}