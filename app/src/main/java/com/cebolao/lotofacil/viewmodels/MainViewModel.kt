package com.cebolao.lotofacil.viewmodels

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.domain.repository.UserPreferencesRepository
import com.cebolao.lotofacil.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
data class MainUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val errorMessage: String? = null,
) {
    val isReady: Boolean get() = !isLoading
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val startDestination: StateFlow<String> = userPreferencesRepository.hasCompletedOnboarding
        .map { hasCompleted ->
            if (hasCompleted) Screen.Home.route else Screen.Onboarding.route
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Screen.Home.route // Valor temporário
        )

    val themeMode: StateFlow<String> = userPreferencesRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "auto"
        )

    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch {
            // A lógica de startDestination agora lida com o onboarding
            // Apenas precisamos garantir que o app está 'pronto'
            _uiState.update { it.copy(isLoading = false) }
        }
    }

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
}