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
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.StatisticsReport
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.model.LastDrawStats
import com.cebolao.lotofacil.domain.model.NextDrawInfo
import com.cebolao.lotofacil.domain.model.WinnerData
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.repository.SyncStatus
import com.cebolao.lotofacil.domain.service.StatisticsAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
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
data class HomeUiState(
    val isScreenLoading: Boolean = true,
    val isStatsLoading: Boolean = false,
    val isSyncing: Boolean = false,
    @StringRes
    val errorMessageResId: Int? = null,
    val lastDrawStats: LastDrawStats? = null,
    val nextDrawInfo: NextDrawInfo? = null,
    val winnerData: List<WinnerData> = emptyList(),
    val statistics: StatisticsReport? = null,
    val selectedPattern: StatisticPattern = StatisticPattern.SUM,
    val selectedTimeWindow: Int = 0,
    val showSyncFailedMessage: Boolean = false,
    val showSyncSuccessMessage: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val statisticsAnalyzer: StatisticsAnalyzer,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private var fullHistory: List<HistoricalDraw> = emptyList()
    private var analysisJob: Job? = null

    init {
        observeSyncStatus()
        loadInitialData()
    }

    private fun observeSyncStatus() {
        viewModelScope.launch {
            historyRepository.syncStatus.collect { status ->
                _uiState.update { it.copy(isSyncing = status is SyncStatus.Syncing) }
                when (status) {
                    is SyncStatus.Failed -> {
                        _uiState.update { it.copy(showSyncFailedMessage = true) }
                    }
                    is SyncStatus.Success -> {
                        _uiState.update { it.copy(showSyncSuccessMessage = true) }
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

    fun onSyncSuccessMessageShown() {
        _uiState.update { it.copy(showSyncSuccessMessage = false) }
    }


    fun retryInitialLoad() = loadInitialData()

    fun forceSync() {
        if (_uiState.value.isSyncing) return
        historyRepository.syncHistory()
    }

    private fun loadInitialData() = viewModelScope.launch(dispatcher) {
        _uiState.update { it.copy(isScreenLoading = true, errorMessageResId = null) }
        try {
            fullHistory = historyRepository.getHistory()
            val latestApiResult = historyRepository.getLatestContestDetails()

            if (fullHistory.isEmpty() && latestApiResult == null) {
                _uiState.update { it.copy(isScreenLoading = false, errorMessageResId = R.string.error_load_data_failed) }
                return@launch
            }

            val stats = statisticsAnalyzer.analyze(fullHistory)
            val processedState = processApiResult(latestApiResult, fullHistory.firstOrNull())

            _uiState.update {
                it.copy(
                    isScreenLoading = false,
                    lastDrawStats = processedState.lastDrawStats,
                    nextDrawInfo = processedState.nextDrawInfo,
                    winnerData = processedState.winnerData,
                    statistics = stats
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(isScreenLoading = false, errorMessageResId = R.string.error_load_data_failed)
            }
        }
    }

    private suspend fun refreshDataAfterSync() = viewModelScope.launch(dispatcher) {
        fullHistory = historyRepository.getHistory()
        val latestApiResult = historyRepository.getLatestContestDetails()

        if (fullHistory.isEmpty()) {
            _uiState.update { it.copy(errorMessageResId = R.string.error_load_data_failed) }
            return@launch
        }

        val drawsToAnalyze = if (_uiState.value.selectedTimeWindow > 0) fullHistory.take(_uiState.value.selectedTimeWindow) else fullHistory
        val stats = statisticsAnalyzer.analyze(drawsToAnalyze)
        val processedState = processApiResult(latestApiResult, fullHistory.firstOrNull())

        _uiState.update {
            it.copy(
                lastDrawStats = processedState.lastDrawStats,
                nextDrawInfo = processedState.nextDrawInfo,
                winnerData = processedState.winnerData,
                statistics = stats
            )
        }
    }

    private fun processApiResult(apiResult: LotofacilApiResult?, lastDraw: HistoricalDraw?): HomeUiState {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val lastDrawStats = lastDraw?.let {
            LastDrawStats(
                contest = it.contestNumber,
                numbers = it.numbers.toImmutableSet(),
                sum = it.sum, evens = it.evens, odds = it.odds, primes = it.primes,
                frame = it.frame, portrait = it.portrait, fibonacci = it.fibonacci,
                multiplesOf3 = it.multiplesOf3
            )
        }

        val nextDrawInfo = if (apiResult?.dataProximoConcurso != null && apiResult.valorEstimadoProximoConcurso > 0) {
            NextDrawInfo(
                formattedDate = apiResult.dataProximoConcurso,
                formattedPrize = currencyFormat.format(apiResult.valorEstimadoProximoConcurso)
            )
        } else null

        val winnerData = apiResult?.listaRateioPremio?.map {
            WinnerData(
                description = it.descricaoFaixa,
                winnerCount = it.numeroDeGanhadores,
                prize = it.valorPremio
            )
        } ?: emptyList()

        return HomeUiState(
            lastDrawStats = lastDrawStats,
            nextDrawInfo = nextDrawInfo,
            winnerData = winnerData
        )
    }

    fun onTimeWindowSelected(window: Int) {
        if (_uiState.value.selectedTimeWindow == window || fullHistory.isEmpty()) return

        analysisJob?.cancel()

        analysisJob = viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isStatsLoading = true, selectedTimeWindow = window) }

            val drawsToAnalyze = if (window > 0) fullHistory.take(window) else fullHistory

            val newStats = statisticsAnalyzer.analyze(drawsToAnalyze)

            _uiState.update { it.copy(statistics = newStats, isStatsLoading = false) }
        }
    }

    fun onPatternSelected(pattern: StatisticPattern) {
        _uiState.update { it.copy(selectedPattern = pattern) }
    }
}