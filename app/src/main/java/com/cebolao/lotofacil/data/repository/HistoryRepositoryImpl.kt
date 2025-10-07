package com.cebolao.lotofacil.data.repository

import android.util.Log
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.datasource.HistoryLocalDataSource
import com.cebolao.lotofacil.data.datasource.HistoryRemoteDataSource
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.di.ApplicationScope
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.repository.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val localDataSource: HistoryLocalDataSource,
    private val remoteDataSource: HistoryRemoteDataSource,
    @ApplicationScope private val applicationScope: CoroutineScope
) : HistoryRepository {

    companion object {
        private const val TAG = "HistoryRepositoryImpl"
    }

    private val cacheMutex = Mutex()
    private val historyCache = mutableMapOf<Int, HistoricalDraw>()
    private var latestApiResultCache: LotofacilApiResult? = null

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    init {
        applicationScope.launch {
            try {
                val local = localDataSource.getLocalHistory()
                cacheMutex.withLock {
                    historyCache.clear()
                    historyCache.putAll(local.associateBy { it.contestNumber })
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao inicializar histórico local", e)
            }
            syncHistory()
        }
    }

    override suspend fun getHistory(): List<HistoricalDraw> {
        cacheMutex.withLock {
            if (historyCache.isNotEmpty()) {
                return historyCache.values.sortedByDescending { it.contestNumber }
            }
        }
        return try {
            val local = localDataSource.getLocalHistory()
            cacheMutex.withLock {
                historyCache.clear()
                historyCache.putAll(local.associateBy { it.contestNumber })
            }
            historyCache.values.sortedByDescending { it.contestNumber }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter histórico", e)
            emptyList()
        }
    }

    override suspend fun getLastDraw(): HistoricalDraw? {
        return try {
            val remoteResult = remoteDataSource.getLatestDraw()
            if (remoteResult != null) {
                latestApiResultCache = remoteResult
                val historicalDraw = parseApiResultToHistoricalDraw(remoteResult)
                if (historicalDraw != null) {
                    cacheMutex.withLock {
                        historyCache[historicalDraw.contestNumber] = historicalDraw
                    }
                    localDataSource.saveNewContests(listOf(historicalDraw))
                    historicalDraw
                } else {
                    getHistory().firstOrNull()
                }
            } else {
                getHistory().firstOrNull()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Não foi possível obter último concurso remoto", e)
            getHistory().firstOrNull()
        }
    }

    override fun syncHistory(): Job = applicationScope.launch {
        if (_syncStatus.value is SyncStatus.Syncing) return@launch
        _syncStatus.value = SyncStatus.Syncing
        try {
            val localHistory = localDataSource.getLocalHistory()
            val latestLocal = localHistory.maxByOrNull { it.contestNumber }?.contestNumber ?: 0
            val latestRemote = remoteDataSource.getLatestDraw()

            if (latestRemote != null) {
                latestApiResultCache = latestRemote // Cache the full result
                if (latestRemote.numero > latestLocal) {
                    val rangeToFetch = (latestLocal + 1)..latestRemote.numero
                    val newDraws = remoteDataSource.getDrawsInRange(rangeToFetch)
                    if (newDraws.isNotEmpty()) {
                        cacheMutex.withLock {
                            newDraws.forEach { historyCache[it.contestNumber] = it }
                        }
                        localDataSource.saveNewContests(newDraws)
                    }
                }
            }

            _syncStatus.value = SyncStatus.Success
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao sincronizar histórico", e)
            _syncStatus.value = SyncStatus.Failed("Falha ao sincronizar histórico: ${e.message ?: "erro desconhecido"}")
        }
    }

    override suspend fun getLatestContestDetails(): LotofacilApiResult? {
        // Return the cached full result, or fetch if null
        return latestApiResultCache ?: remoteDataSource.getLatestDraw()?.also {
            latestApiResultCache = it
        }
    }

    private fun parseApiResultToHistoricalDraw(apiResult: LotofacilApiResult): HistoricalDraw? {
        val contest = apiResult.numero
        val numbers = apiResult.listaDezenas.mapNotNull { it.toIntOrNull() }.toSet()
        return if (contest > 0 && numbers.size >= 15) {
            HistoricalDraw(
                contestNumber = contest,
                numbers = numbers,
                date = apiResult.dataApuracao
            )
        } else {
            null
        }
    }
}