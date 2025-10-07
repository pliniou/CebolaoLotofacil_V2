package com.cebolao.lotofacil.data.repository

import android.util.Log
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.datasource.HistoryLocalDataSource
import com.cebolao.lotofacil.data.datasource.HistoryRemoteDataSource
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
            // Tenta sincronizar de forma assíncrona
            syncHistory()
        }
    }

    override suspend fun getHistory(): List<HistoricalDraw> {
        cacheMutex.withLock {
            if (historyCache.isNotEmpty()) {
                return historyCache.values.sortedByDescending { it.contestNumber }
            }
        }
        // Fallback: carregar do local datasource se cache estiver vazio
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
        // Tenta obter do remoto primeiro
        return try {
            val remote = remoteDataSource.getLatestDraw()
            if (remote != null) {
                cacheMutex.withLock {
                    historyCache[remote.contestNumber] = remote
                }
                localDataSource.saveNewContests(listOf(remote))
                remote
            } else {
                // Fallback para cache/local
                getHistory().firstOrNull()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Não foi possível obter último concurso remoto", e)
            // Fallback para cache/local em caso de erro de rede
            getHistory().firstOrNull()
        }
    }

    override fun syncHistory(): Job = applicationScope.launch {
        if (_syncStatus.value is SyncStatus.Syncing) return@launch
        _syncStatus.value = SyncStatus.Syncing
        try {
            val localHistory = localDataSource.getLocalHistory()
            val latestLocal = localHistory.maxByOrNull { it.contestNumber }?.contestNumber ?: 0
            val latestRemote = try {
                remoteDataSource.getLatestDraw()
            } catch (_: Exception) {
                null
            }

            if (latestRemote != null && latestRemote.contestNumber > latestLocal) {
                val rangeToFetch = (latestLocal + 1)..latestRemote.contestNumber
                val newDraws = remoteDataSource.getDrawsInRange(rangeToFetch)
                if (newDraws.isNotEmpty()) {
                    cacheMutex.withLock {
                        newDraws.forEach { historyCache[it.contestNumber] = it }
                    }
                    localDataSource.saveNewContests(newDraws)
                }
            } else {
                // garante que cache contém localHistory
                cacheMutex.withLock {
                    historyCache.clear()
                    historyCache.putAll(localHistory.associateBy { it.contestNumber })
                }
            }

            _syncStatus.value = SyncStatus.Success
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao sincronizar histórico", e)
            _syncStatus.value = SyncStatus.Failed("Falha ao sincronizar histórico: ${e.message ?: "erro desconhecido"}")
        }
    }
}