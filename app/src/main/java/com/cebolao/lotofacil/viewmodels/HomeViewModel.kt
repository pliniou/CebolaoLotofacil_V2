package com.cebolao.lotofacil.viewmodels

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.Grid4x4
import androidx.compose.material.icons.outlined.LooksTwo
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.StatisticsReport
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.repository.SyncStatus
import com.cebolao.lotofacil.domain.service.StatisticsAnalyzer
import com.cebolao.lotofacil.domain.usecase.GetHomeScreenDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class StatisticPattern(val title: String, val icon: ImageVector) {
    SUM("Soma", Icons.Outlined.Functions),
    EVENS("Pares", Icons.Outlined.LooksTwo),
    PRIMES("Primos", Icons.Outlined.Star),
    FRAME("Moldura", Icons.Outlined.Grid4x4),
    PORTRAIT("Miolo", Icons.Outlined.CropSquare),
    FIBONACCI("Fibonacci", Icons.Outlined.Timeline),
    MULTIPLES_OF_3("Múltiplos 3", Icons.Outlined.FormatListNumbered)
}

@Stable
@Immutable
data class LastDrawStats(
    val contest: Int,
    val numbers: ImmutableSet<Int>,
    val sum: Int,
    val evens: Int,
    val odds: Int,
    val primes: Int,
    val frame: Int,
    val portrait: Int,
    val fibonacci: Int,
    val multiplesOf3: Int
)

@Stable
data class HomeUiState(
    val isScreenLoading: Boolean = true,
    val isStatsLoading: Boolean = false,
    val isSyncing: Boolean = false,
    @StringRes
    val errorMessageResId: Int? = null,
    val lastDrawStats: LastDrawStats? = null,
    val statistics: StatisticsReport? = null,
    val selectedPattern: StatisticPattern = StatisticPattern.SUM,
    val selectedTimeWindow: Int = 0,
    val showSyncFailedMessage: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeScreenDataUseCase: GetHomeScreenDataUseCase,
    private val historyRepository: HistoryRepository,
    private val statisticsAnalyzer: StatisticsAnalyzer,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    // Armazena todo o histórico para permitir a seleção de janelas sem recarregar o repositório.
    private var fullHistory: List<HistoricalDraw> = emptyList()
    private var analysisJob: Job? = null

    init {
        observeSyncStatus()
        loadInitialData()
    }

    /** Observa o status de sincronização do repositório e reage. */
    private fun observeSyncStatus() {
        // Coleta no viewModelScope (Dispatcher.Main/Unconfined) para evitar trocas desnecessárias de contexto.
        viewModelScope.launch {
            historyRepository.syncStatus.collect { status ->
                _uiState.update { it.copy(isSyncing = status is SyncStatus.Syncing) }
                when (status) {
                    is SyncStatus.Failed -> {
                        _uiState.update { it.copy(showSyncFailedMessage = true) }
                    }
                    is SyncStatus.Success -> {
                        // Recarrega os dados após sincronização bem-sucedida para atualizar a UI.
                        refreshDataAfterSync()
                    }
                    else -> {}
                }
            }
        }
    }

    fun onSyncMessageShown() {
        _uiState.update { it.copy(showSyncFailedMessage = false) }
    }

    fun retryInitialLoad() = loadInitialData()

    fun forceSync() {
        if (_uiState.value.isSyncing) return
        // Inicia a sincronização, o resultado será observado em observeSyncStatus
        historyRepository.syncHistory()
    }

    /** Carrega os dados iniciais da tela (último sorteio e estatísticas iniciais). */
    private fun loadInitialData() = viewModelScope.launch(dispatcher) {
        _uiState.update { it.copy(isScreenLoading = true, errorMessageResId = null) }
        getHomeScreenDataUseCase().collect { result ->
            result.onSuccess { data ->
                // O histórico já foi obtido dentro do UseCase, mas precisamos da cópia completa aqui para uso futuro
                fullHistory = historyRepository.getHistory()
                _uiState.update {
                    it.copy(
                        isScreenLoading = false,
                        lastDrawStats = data.lastDrawStats,
                        statistics = data.initialStats
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isScreenLoading = false,
                        errorMessageResId = R.string.error_load_data_failed
                    )
                }
            }
        }
    }

    /** Recarrega apenas as estatísticas após uma sincronização. */
    private fun refreshDataAfterSync() = viewModelScope.launch(dispatcher) {
        val newHistory = historyRepository.getHistory()
        val newLastDraw = newHistory.firstOrNull()

        if (newHistory.isEmpty()) {
            _uiState.update { it.copy(errorMessageResId = R.string.error_load_data_failed) }
            return@launch
        }

        fullHistory = newHistory
        
        // Recalcula o LastDrawStats e as estatísticas para a janela atual
        val newLastDrawStats = if (newLastDraw != null) calculateLastDrawStats(newLastDraw) else null
        
        // Mantém a janela de tempo selecionada e recalcula as estatísticas
        val drawsToAnalyze = if (_uiState.value.selectedTimeWindow > 0) newHistory.take(_uiState.value.selectedTimeWindow) else newHistory
        val newStats = statisticsAnalyzer.analyze(drawsToAnalyze)

        _uiState.update { 
            it.copy(
                lastDrawStats = newLastDrawStats,
                statistics = newStats
            )
        }
    }

    /** Calcula as estatísticas simples para o último sorteio. Extraída do UseCase para reutilização. */
    private fun calculateLastDrawStats(lastDraw: HistoricalDraw): LastDrawStats {
        return LastDrawStats(
            contest = lastDraw.contestNumber,
            numbers = lastDraw.numbers.toImmutableSet(),
            sum = lastDraw.sum,
            evens = lastDraw.evens,
            odds = lastDraw.odds,
            primes = lastDraw.primes,
            frame = lastDraw.frame,
            portrait = lastDraw.portrait,
            fibonacci = lastDraw.fibonacci,
            multiplesOf3 = lastDraw.multiplesOf3
        )
    }

    fun onTimeWindowSelected(window: Int) {
        if (_uiState.value.selectedTimeWindow == window || fullHistory.isEmpty()) return
        
        // Cancela qualquer análise em andamento
        analysisJob?.cancel()
        
        analysisJob = viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isStatsLoading = true, selectedTimeWindow = window) }
            
            val drawsToAnalyze = if (window > 0) fullHistory.take(window) else fullHistory
            
            // Reutiliza o StatisticsAnalyzer
            val newStats = statisticsAnalyzer.analyze(drawsToAnalyze)
            
            _uiState.update { it.copy(statistics = newStats, isStatsLoading = false) }
        }
    }

    fun onPatternSelected(pattern: StatisticPattern) {
        _uiState.update { it.copy(selectedPattern = pattern) }
    }
}